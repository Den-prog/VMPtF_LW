package com.example.lw_3

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
data class LoginResponse(
    val message: String,
    val user: User,
)
interface VideoApi {
    // GET-запит для отримання списку всіх відео
    @GET("videos")
    fun getVideos(): Call<List<Video>>

    // POST-запит для завантаження нового відео на сервер
    @POST("videos/upload")
    fun addVideo(@Body video: Video): Call<Video>

    @POST("videos/{videoId}/comments")
    fun addComment(
        @Path("videoId") videoId: Int,
        @Body body: @JvmSuppressWildcards Map<String, Any>
    ): Call<Video>

    @GET("videos/users")
    fun getUsers(): Call<List<User>>

    @POST("videos/login")
    fun login(@Body credentials: Map<String, String>): Call<LoginResponse>

    @POST("videos/register")
    fun register(@Body credentials: Map<String, String>): Call<LoginResponse>


    @POST("videos/{videoId}/share")
    fun shareVideo(
        @Path("videoId") videoId: Int,
        @Body body: @JvmSuppressWildcards Map<String, Any>
    ): Call<Map<String, Any>>

    @PATCH("videos/{id}/like")
    fun toggleLike(
        @Path("id") id: Int,
        @Body body: @JvmSuppressWildcards Map<String, String>
    ): Call<Video>

    @Multipart
    @POST("videos/upload")
    fun uploadVideo(
        @Part videoFile: MultipartBody.Part,
        @Part("title") title: RequestBody
    ): Call<Video>

    @DELETE("videos/{id}")
    fun deleteVideo(@Path("id") id: Int): Call<Void>

    @DELETE("videos/{videoId}/comments")
    fun clearComments(@Path("videoId") videoId: Int): Call<Video>


}