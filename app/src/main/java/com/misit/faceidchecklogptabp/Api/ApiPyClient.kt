package com.misit.faceidchecklogptabp.Api

import android.content.Context
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiPyClient {
    private var BASE_URL= "http://abpjobsite.com:5000"
    private var retrofit : Retrofit? = null

    fun getClient(context: Context?):Retrofit?{
        if (retrofit==null){
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getHeader(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit
    }

    private fun getHeader(context: Context?): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(),
            SharedPrefsCookiePersistor(context)
        )
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .cookieJar(cookieJar)
            .build()
    }
}