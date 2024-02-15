FROM python:3.12

ADD requirements.txt .

RUN pip install -r requirements.txt

ADD . .

CMD [ "python3", "-m" , "flask", "run", "--host=0.0.0.0"]