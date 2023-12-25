package br.com.codapp.imagecreator.commons.extensions

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import java.io.File

fun shareFile(path: String, context: Context) {
    val file = File(path)
    file.scanImages(context = context)

    MediaScannerConnection.scanFile(
        context, arrayOf(file.absolutePath), arrayOf("image/png")
    ) { path, uri ->
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/png"
        }

        context.startActivity(Intent.createChooser(sendIntent, null))
    }
}