package br.com.codapp.imagecreator.data.remote

import com.aallam.openai.api.image.ImageSize

object ImageAiConf {
    const val MAX_IMAGES_GENERATE = 3 // don't increase more than 10
    val DEFAULT_IMAGE_SIZE = ImageSize.is1024x1024

    val IMAGE_SIZE_LIST = listOf<ImageSize>(
        ImageSize.is1024x1024,
        ImageSize.is512x512,
        ImageSize.is256x256
    )
}