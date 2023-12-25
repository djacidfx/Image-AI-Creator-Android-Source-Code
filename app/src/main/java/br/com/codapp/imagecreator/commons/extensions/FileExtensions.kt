package br.com.codapp.imagecreator.commons.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Environment
import br.com.codapp.imagecreator.feature.home.mapper.toImageEntity
import com.aallam.openai.api.image.ImageURL
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*

fun String.saveImageUrlToExternal(): String {
    return runCatching {
        val url = URL(this)
        val image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

        val path: File =
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES+"/ImageCreator"
            )
        path.mkdirs()

        val imageFile = File(path, "ic_${Date().time}.png")
        val out = FileOutputStream(imageFile)

        image.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
        out.close()
        imageFile.absolutePath
    }.getOrDefault("")
}

fun List<ImageURL>.toSavedImageEntityList() =
    map {
        val path = it.url.saveImageUrlToExternal()

        it.toImageEntity(path = path)
    }

fun File.scanImages(context: Context) {
    MediaScannerConnection.scanFile(
        context, arrayOf(this.absolutePath), arrayOf("image/png")
    ) { path, uri ->

    }
}

