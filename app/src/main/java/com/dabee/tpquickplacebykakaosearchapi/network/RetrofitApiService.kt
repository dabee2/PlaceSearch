package com.dabee.tpquickplacebykakaosearchapi.network

import com.dabee.tpquickplacebykakaosearchapi.model.KakaoSearchPlaceResponse
import com.dabee.tpquickplacebykakaosearchapi.model.NidUserInfoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface RetrofitApiService {

    //네아로 사용자정보 API
    @GET("/v1/nid/me")
    fun getNidUserInfo(@Header("Authorization") authorization:String): Call<NidUserInfoResponse>

    //카카오 키워드 장소검색 API
    @Headers("Authorization: KakaoAK f7ac9bc4580951b04cc1003c6cebf058")
    @GET("/v2/local/search/keyword.json")
    fun searchPlaces(@Query("query") query: String, @Query("x") longitude:String, @Query("y") latitude:String): Call<KakaoSearchPlaceResponse>

}
