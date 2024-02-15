package imageService

import API_URL
import fuel.Fuel
import fuel.post
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
data class ImageOptions(val rotate: Int, val keepAspectRatio: Boolean, val customHeight: Float)

@Serializable
data class ProcessRequest(val uuid: String, val options: ImageOptions)

@Serializable
data class ProcessResponse(val chunksBase64: List<String>, val width: Int, val height: Int, val pixels: List<List<Boolean>>)

@Serializable
data class UploadRequest(val imageBase64: String);

@Serializable
data class UploadResponse(val uuid: String);


val json = Json { encodeDefaults = true }
val headers = mapOf("Content-Type" to "application/json")

@OptIn(ExperimentalEncodingApi::class)
suspend fun uploadImage(file: ByteArray): String {
    val request = UploadRequest(Base64.encode(file))
    val body = json.encodeToString(request)
    val response = Fuel.post("$API_URL/upload", body = body, headers = headers).body
    val jsonResponse = Json.decodeFromString<UploadResponse>(response)
    return jsonResponse.uuid
}

@OptIn(ExperimentalEncodingApi::class)
suspend fun processImage(uuid: String, options: ImageOptions): ProcessResponse {
    val request = ProcessRequest(uuid, options)
    val body = json.encodeToString(request)
    val response = Fuel.post("$API_URL/process", body = body, headers = headers)
    if (response.statusCode == 401) {
        throw Exception("invalid session")
    }

    return Json.decodeFromString<ProcessResponse>(response.body)
}