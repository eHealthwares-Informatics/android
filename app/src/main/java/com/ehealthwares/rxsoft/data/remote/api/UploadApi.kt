package com.rxsoft.mobile.data.remote.api

import com.rxsoft.mobile.data.remote.dto.UploadImageResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadApi {
    @Multipart
    @POST("upload/image")
    suspend fun uploadImage(@Part file: MultipartBody.Part): UploadImageResponse
}
