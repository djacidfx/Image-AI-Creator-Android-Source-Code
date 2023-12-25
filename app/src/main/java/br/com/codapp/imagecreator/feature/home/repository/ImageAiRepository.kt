package br.com.codapp.imagecreator.feature.home.repository

import br.com.codapp.imagecreator.commons.extensions.toSavedImageEntityList
import br.com.codapp.imagecreator.data.Resource
import br.com.codapp.imagecreator.data.local.dao.ImageDao
import br.com.codapp.imagecreator.data.local.entity.ImageEntity
import br.com.codapp.imagecreator.data.remote.ErrorType
import com.aallam.openai.api.ExperimentalOpenAI
import com.aallam.openai.api.exception.OpenAIAPIException
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.*
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeoutException

@OptIn(ExperimentalOpenAI::class)
class ImageAiRepository(
    private val openAI: OpenAI,
    private val imageDao: ImageDao
) {

    fun generatePromptImages(
        prompt: String,
        qt: Int,
        size: ImageSize
    ) = flow {

        withTimeout(60000) {
            val images = openAI.imageURL(
                creation = ImageCreation(
                    prompt = prompt,
                    n = qt,
                    size = size
                )
            )

            emit(Resource.Success(data = images))
        }

        emit(Resource.Loading(isLoading = false))
    }.flowOn(Dispatchers.Default)
        .onStart {
            emit(Resource.Loading(isLoading = true))
        }
        .onEach {
            it.data?.let { imageList ->
                if (it is Resource.Success && imageList.isNotEmpty()) {
                    val imageEntityList = imageList.toSavedImageEntityList()
                    imageDao.insertImageList(imageEntityList)
                }
            }
        }.flowOn(Dispatchers.IO)
        .catch {
            when (it.cause) {
                is TimeoutException -> {
                    emit(Resource.Error(error = ErrorType.TIMEOUT))
                }
                is CancellationException -> {
                    emit(Resource.Error(error = ErrorType.TIMEOUT))
                }
                is OpenAIAPIException -> {
                    emit(Resource.Error(error = ErrorType.IMAGE_POLICY))
                }
                else -> {
                    emit(Resource.Error(error = ErrorType.GENERIC))
                }
            }

            emit(Resource.Loading(isLoading = false))
        }

    suspend fun getCachedImages() =
        withContext(Dispatchers.IO) {
            imageDao.getAllImages()
        }

    suspend fun removeCachedImage(imageEntity: ImageEntity) {
        withContext(Dispatchers.IO) {
            imageDao.deleteImage(imageEntity)
        }
    }
}