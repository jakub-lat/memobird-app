import base64
import io
from uuid import uuid4
import atexit
import time

from flask import Flask, request, session
from PIL import Image, ImageOps, ImageFilter
from apscheduler.schedulers.background import BackgroundScheduler

app = Flask(__name__)

PROCESSING_SIZE = 1000
MAX_SIZE = 384
SESSION_DURATION = 60 * 60

sessions = {}


@app.post('/upload')
def upload():
    content = request.json
    img_data = base64.b64decode(content['imageBase64'])
    img = Image.open(io.BytesIO(img_data))
    img.thumbnail((PROCESSING_SIZE, PROCESSING_SIZE))
    img = img.rotate(90, expand=True)

    uuid = str(uuid4())
    sessions[uuid] = (img, time.time())

    return {'uuid': uuid}


@app.post('/process')
def process_image():
    content = request.json

    options = content['options']
    uuid = content['uuid']

    rotate, keep_aspect_ratio, custom_height, img_filter, invert = options['rotate'], options['keepAspectRatio'], options['customHeight'], options['filter'], options['invert']

    if uuid not in sessions:
        return 'invalid session', 401

    img, _ = sessions[uuid]

    if rotate != 0:
        img = img.rotate(rotate, expand=True)

    # img = img.transpose(Image.FLIP_TOP_BOTTOM)
    
    img = apply_filter(img, img_filter)

    if keep_aspect_ratio:
        img.thumbnail((MAX_SIZE, MAX_SIZE))
    else:
        custom_height = int(custom_height)
        if custom_height == 0:
            custom_height = MAX_SIZE

        img = img.resize((MAX_SIZE, custom_height))

    img = img.convert(mode='L')  # Ensure ImageOps will handle the data
    # img = ImageOps.invert(img) # Was needed before BMP header support
    img = ImageOps.flip(img)  # Fixes BMP format columns order
    img = img.convert(mode='1')  # Memobird needs 1-bit monochrome

    if invert:
        img = ImageOps.invert(img)

    width, height = img.size
    pixels = list(img.getdata())

    chunks_b64 = split_into_chunks(img, MAX_SIZE)

    return {
        'chunksBase64': chunks_b64,
        'width': width,
        'height': height,
        'pixels': [[x == 0 for x in pixels[i * width:(i + 1) * width]] for i in range(height)]
    }


def change_contrast(img, level):
    factor = (259 * (level + 255)) / (255 * (259 - level))

    def contrast(c):
        return 128 + factor * (c - 128)

    return img.point(contrast)


def apply_filter(img, img_filter):
    match img_filter:
        case 'contrast':
            img = change_contrast(img, 100)
        case 'contour':
            img = img.filter(ImageFilter.CONTOUR)
        case 'emboss':
            img = img.filter(ImageFilter.EMBOSS)
        case 'smooth':
            img = img.filter(ImageFilter.SMOOTH)
        case 'smooth more':
            img = img.filter(ImageFilter.SMOOTH_MORE)
        case 'edge enhance':
            img = img.filter(ImageFilter.EDGE_ENHANCE)
        case 'edge enhance more':
            img = img.filter(ImageFilter.EDGE_ENHANCE_MORE)
        case 'find edges':
            img = img.filter(ImageFilter.FIND_EDGES)
        case 'sharpen':
            img = img.filter(ImageFilter.SHARPEN)

    return img

def img_to_b64(img):
    byte_arr = io.BytesIO()
    img.save(byte_arr, format='bmp')
    result = byte_arr.getvalue()
    return base64.b64encode(result).decode()


def split_into_chunks(img, chunk_height):
    if img.height <= chunk_height:
        return [img_to_b64(img)]

    res = []
    for y in range(0, img.height, chunk_height):
        chunk = img.crop((0, y, img.width, min(y + chunk_height, img.height)))
        res.append(img_to_b64(chunk))

    return res


def cleanup():
    now = time.time()
    copy = dict(sessions)
    for uuid, (_, creation_time) in copy.items():
        diff = now - creation_time
        if diff > SESSION_DURATION:
            print(f'session {uuid} expired')
            del sessions[uuid]


scheduler = BackgroundScheduler()
scheduler.add_job(func=cleanup, trigger="interval", seconds=10)
scheduler.start()

atexit.register(lambda: scheduler.shutdown())

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=3000)
