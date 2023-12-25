package br.com.codapp.imagecreator.feature.home.state

import br.com.codapp.imagecreator.data.local.entity.ImageEntity
import br.com.codapp.imagecreator.data.remote.ErrorType
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.image.ImageURL

data class HomeUiState (
    val loading: Boolean = false,
    var error: ErrorType? = null,
    val generatedImageList: List<ImageURL> = listOf(),
    val imageEntityList: List<ImageEntity> = listOf()
)

sealed class HomeIntent {
    class GenerateImagesByPrompt(val prompt: String, val qt: Int, val size: ImageSize) : HomeIntent()
    object GetAllCachedImages : HomeIntent()
    class RemoveCachedImage(val imageEntity: ImageEntity) : HomeIntent()
}