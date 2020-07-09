package com.esmaeel.moviesapp.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class API_KEY

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PROFILE_IMAGE_BASE_URL
