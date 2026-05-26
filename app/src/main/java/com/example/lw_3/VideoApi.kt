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
    //отримання списку всіх відео
    @GET("videos")
    fun getVideos(): Call<List<Video>>

    //запит для завантаження нового відео на сервер
    @POST("videos/upload")
    fun addVideo(@Body video: Video): Call<Video>

    //запит на додавання коментарів під конкретне відео
    @POST("videos/{videoId}/comments")
    fun addComment(
        @Path("videoId") videoId: Int,
        @Body body: @JvmSuppressWildcards Map<String, Any>
    ): Call<Video>

    //отримання користувача
    @GET("videos/users")
    fun getUsers(): Call<List<User>>

    //запит на логін
    @POST("videos/login")
    fun login(@Body credentials: Map<String, String>): Call<LoginResponse>

    //запит на регістрацію
    @POST("videos/register")
    fun register(@Body credentials: Map<String, String>): Call<LoginResponse>

    //запит на шерінг відео
    @POST("videos/{videoId}/share")
    fun shareVideo(
        @Path("videoId") videoId: Int,
        @Body body: @JvmSuppressWildcards Map<String, Any>
    ): Call<Map<String, Any>>

    //щоб ставити лайки
    @PATCH("videos/{id}/like")
    fun toggleLike(
        @Path("id") id: Int,
        @Body body: @JvmSuppressWildcards Map<String, String>
    ): Call<Video>

    //завантажувати відео
    @Multipart
    @POST("videos/upload")
    fun uploadVideo(
        @Part videoFile: MultipartBody.Part,
        @Part("title") title: RequestBody
    ): Call<Video>

    //видалення відео
    @DELETE("videos/{id}")
    fun deleteVideo(@Path("id") id: Int): Call<Void>

    //видалення коментарів
    @DELETE("videos/{videoId}/comments")
    fun clearComments(@Path("videoId") videoId: Int): Call<Video>


}