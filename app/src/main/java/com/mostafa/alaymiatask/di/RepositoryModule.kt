package com.mostafa.alaymiatask.di


import com.mostafa.alaymiatask.data.repository.MainRepositoryImpl
import com.mostafa.alaymiatask.domain.repository.MainRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindMainRepository(repository: MainRepositoryImpl): MainRepository


}