package printService

import fuel.Fuel
import fuel.post
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import showToast

interface PrintService {
    suspend fun print(document: PrintDocument);
}

class HttpPrinter(private val url: String) : PrintService {
    override suspend fun print(document: PrintDocument) {
        println("PRINTING ==========")
        println(document.json)
        val res = Fuel.post("$url/sys/printer", body = document.json, headers = mapOf("Content-Type" to "application/json"))

        if (res.statusCode != 200) {
           throw Exception("Printing error! ${res.body}")
        }
    }
}