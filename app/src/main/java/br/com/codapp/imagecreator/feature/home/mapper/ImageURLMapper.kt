package br.com.codapp.imagecreator.feature.home.mapper

import br.com.codapp.imagecreator.data.local.entity.ImageEntity
import com.aallam.openai.api.image.ImageURL
import java.util.*

fun ImageURL.toImageEntity(path: String) =
    ImageEntity(
        url = this.url,
        createdAt = Date(),
        path = path
    )

fun List<ImageURL>.toImageEntityList(path: String) =
    map {
        ImageEntity(
            url = it.url,
            createdAt = Date(),
            path = path
        )
    }