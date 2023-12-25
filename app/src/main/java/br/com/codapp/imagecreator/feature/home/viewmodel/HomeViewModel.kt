package br.com.codapp.imagecreator.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.codapp.imagecreator.data.Resource
import br.com.codapp.imagecreator.data.local.entity.ImageEntity
import br.com.codapp.imagecreator.feature.home.repository.ImageAiRepository
import br.com.codapp.imagecreator.feature.home.state.HomeIntent
import br.com.codapp.imagecreator.feature.home.state.HomeUiState
import com.aallam.openai.api.image.ImageSize
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val imageAiRepository: ImageAiRepository
) : ViewModel() {

    private val intentChannel = Channel<HomeIntent>(Channel.UNLIMITED)

    private val _homeState = MutableStateFlow(HomeUiState(loading = false))
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    init {
        handleIntents()
    }

    fun sendIntent(intent: HomeIntent) {
        viewModelScope.launch {
            intentChannel.send(intent)
        }
    }

    private fun handleIntents() {
        intentChannel
            .consumeAsFlow()
            .onEach { intent ->
                when(intent) {
                    is HomeIntent.GenerateImagesByPrompt -> {
                        generateImagesByPrompt(
                            prompt = intent.prompt,
                            qt = intent.qt,
                            size = intent.size
                        )
                    }
                    is HomeIntent.GetAllCachedImages -> {
                        getAllCachedImages()
                    }
                    is HomeIntent.RemoveCachedImage -> {
                        removeCachedImage(
                            imageEntity = intent.imageEntity
                        )
                    }
                }
            }.launchIn(viewModelScope)
    }

    private fun generateImagesByPrompt(prompt: String, qt: Int, size: ImageSize) {
        viewModelScope.launch {
            imageAiRepository.generatePromptImages(
                prompt = prompt,
                qt = qt,
                size = size
            ).collect { res ->
                when(res) {
                    is Resource.Success -> {
                        _homeState.update {
                            it.copy(generatedImageList = res.data ?: emptyList())
                        }
                    }
                    is Resource.Error -> {
                        _homeState.update {
                            it.copy(error = res.error)
                        }
                    }
                    is Resource.Loading -> {
                        _homeState.update {
                            it.copy(loading = res.isLoading)
                        }
                    }
                }
            }
        }
    }

    private fun removeCachedImage(imageEntity: ImageEntity) {
        viewModelScope.launch {
            imageAiRepository.removeCachedImage(imageEntity)
            getAllCachedImages()
        }
    }

    private fun getAllCachedImages() {
        viewModelScope.launch {
            _homeState.update {
                it.copy(imageEntityList = imageAiRepository.getCachedImages())
            }
        }
    }
}