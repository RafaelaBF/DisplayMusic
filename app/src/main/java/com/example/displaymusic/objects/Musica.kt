package com.example.displaymusic.objects

import android.content.Context
import com.example.displaymusic.uteis.Jsons
import org.json.JSONArray

class Musica {

    constructor()

    var titulo: String = ""
    var autor: String = ""
    var perfilAutor: String = ""
    var musicaArquivo: Int = 0
    var capaArquivo: Int = 0

    constructor(titulo: String, autor: String, perfilAutor: String, musicaArquivo: Int, capaArquivo: Int){
        this.titulo = titulo
        this.autor = autor
        this.perfilAutor = perfilAutor
        this.musicaArquivo = musicaArquivo
        this.capaArquivo = capaArquivo
    }

    fun jsonForMusica(context: Context): MutableList<Musica> {
        val songs = mutableListOf<Musica>()

        val jsonString = Jsons().lerArquivoJSON(context)
        jsonString?.let {
            val jsonArray = JSONArray(it)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val titulo = jsonObject.getString("titulo")
                val autor = jsonObject.getString("autor")
                val perfilAutor = jsonObject.getString("perfil_autor")
                val musica = context.resources.getIdentifier(
                    jsonObject.getString("musica").removeSuffix(".mp3"),
                    "raw",
                    context.packageName
                )
                val capaArquivo = context.resources.getIdentifier(
                    jsonObject.getString("capa").removeSuffix(".png"),
                    "drawable",
                    context.packageName
                )
                songs.add(Musica(titulo, autor, perfilAutor, musica, capaArquivo))
            }
        }
        return songs
    }

    override fun toString(): String {
        return "Musica(titulo='$titulo', autor='$autor', perfilAutor='$perfilAutor', musicaArquivo=$musicaArquivo, capaArquivo=$capaArquivo)"
    }
}