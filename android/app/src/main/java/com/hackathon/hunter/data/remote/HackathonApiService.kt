package com.hackathon.hunter.data.remote

import com.hackathon.hunter.data.remote.dto.HackathonDto
import retrofit2.http.*

interface HackathonApiService {

    @GET("hackathons")
    suspend fun getHackathons(
        @Query("is_vietnam_eligible") isVietnamEligible: Boolean? = null,
        @Query("is_online") isOnline: Boolean? = null,
        @Query("prize_type") prizeType: String? = null,
        @Query("min_prize_value") minPrizeValue: Double? = null,
        @Query("platforms") platforms: List<String>? = null,
        @Query("query") query: String? = null,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 50
    ): List<HackathonDto>

    @POST("hackathons/{id}/report")
    suspend fun reportHackathon(
        @Path("id") id: Int
    ): HackathonDto
}
