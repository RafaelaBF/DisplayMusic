package com.example.displaymusic.uteis

import android.content.Context
import java.io.IOException

class Jsons {

    fun lerArquivoJSON(context: Context): String? {
        val json: String?
        try {
            val inputStream = context.assets.open("musicas.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }
}