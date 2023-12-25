package br.com.codapp.imagecreator.feature.di

import br.com.codapp.imagecreator.feature.home.repository.ImageAiRepository
import br.com.codapp.imagecreator.feature.home.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    factory { ImageAiRepository(get(), get()) }

    viewModel { HomeViewModel(get()) }
}