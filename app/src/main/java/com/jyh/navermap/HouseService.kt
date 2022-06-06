package com.jyh.navermap

import retrofit2.Call
import retrofit2.http.GET

interface HouseService {
    @GET("/v3/3ee74756-1b9e-4531-ab3e-fa0559eba05e")
    fun getHouseList(): Call<HouseDto>
}