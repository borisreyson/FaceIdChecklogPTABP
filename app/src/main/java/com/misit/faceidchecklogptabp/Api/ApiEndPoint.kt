package com.misit.abpenergy.api

import com.misit.faceidchecklogptabp.Response.*
import com.misit.faceidchecklogptabp.Response.Absen.AllAbsenResponse
import com.misit.faceidchecklogptabp.Response.Absen.DirInfoResponse
import com.misit.faceidchecklogptabp.Response.MainResponse.FirstLoadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiEndPoint{

    @GET("refresh-csrf")
    fun getToken(@Query("csrf_token") tokenId:String): Call<CsrfTokenResponse>?
    @Multipart
    @POST("upload_image.php")
    fun uploadImage(@Part fileToUpload:MultipartBody.Part?,
                    @Part("nik") nik:RequestBody?,
                    @Part("tgl") tgl:RequestBody?,
                    @Part("jam") jam:RequestBody?,
                    @Part("status") status:RequestBody?,
                    @Part("id") id:RequestBody?,
                    @Part("lupa_absen") lupa_absen:RequestBody,
                    @Part("lat") lat:RequestBody,
                    @Part("lng") lng:RequestBody): Call<ImageResponse>
    @Multipart
    @POST("registrasi_wajah.php")
    fun daftarkanWajah(@Part fileToUpload:MultipartBody.Part?,
                    @Part("nik") nik:RequestBody?): Call<ImageResponse>
//    @FormUrlEncoded
//    @POST("login.php")
//    fun loginChecklogin(@Field("nik") nik:String?,
//                        @Field("password") password:String?): Call<UserResponse>
    @GET("api/android/app/cek/lokasi")
    fun cekLokasi(): Call<AbpResponse>?

    @FormUrlEncoded
    @POST("api/android/post/login-face-id")
    fun loginChecklogin(@Field("username") username:String?,
                        @Field("password") password:String?,
                        @Field("_token") csrf_token:String?,
                        @Field("android_token") android_token:String?,
                        @Field("app_version") app_version:String?,
                        @Field("app_name") app_name:String?,
                        @Field("imei") imei:String?): Call<LoginResponse>

    @FormUrlEncoded
    @POST("api/android/post/updatePasswordFace")
    fun updatePassword(@Field("username") username:String?,
                       @Field("password") password:String?,
                       @Field("newPassword") newPassword:String?,
                       @Field("_token") csrf_token:String?): Call<LoginResponse>


    @FormUrlEncoded
    @POST("api/android/post/kirim/masukan")
    fun kirimMasukan(@Field("nik") nik:String?,
                       @Field("nama") nama:String?,
                       @Field("masukan") masukan:String?,
                       @Field("_token") csrf_token:String?): Call<MasukanResponse>


    @GET("api/android/token/firebase/new")
    fun getAndroidToken(@Query("nik") nik:String?,
                        @Query("app") app:String?,
                        @Query("android_token") android_token:String?)
            : Call<FirstLoadResponse>?

    @GET("api/android/get/list/absen")
    fun getAbsen(@Query("nik") nik:String,
                 @Query("status") status:String,
                 @Query("page") page:Int
    ): Call<ListAbsenResponse>?

    @GET("/absen/get/lastAbsen")
    fun lastAbsen(@Query("nik") nik:String
    ): Call<LastAbsenResponse>?

    @GET("api/android/app/version")
    fun getAppVersion(@Query("app") app:String?)
            : Call<AppVersionResponse>?

    @GET("/absen/list/all")
    fun listAllAbsen(@Query("tanggal") tanggal:String?,
                     @Query("status") status:String?,
                     @Query("page") page:Int?)
            : Call<AllAbsenResponse>?


    @GET("absen/presentasi/pengguna")
    fun getPresentasiPengguna()
            : Call<PresentasiPenggunaResponse>?

    @GET("api/android/check/folder")
    fun getDirInfo(@Query("nik") nik:String?)
            : Call<DirInfoResponse>?
    @GET("/absen/apl/masukan")
    fun getListMasukan(@Query("page") page:Int?,
                       @Query("cari") cari:String?)
            : Call<com.misit.faceidchecklogptabp.Masukan.Response.MasukanResponse>?
}