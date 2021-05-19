package expert.rightperception.attributesapp.data.api

import expert.rightperception.attributesapp.BuildConfig
import expert.rightperception.attributesapp.data.api.model.LicenseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface ExpertApi {

    companion object {
        const val BASE_URL = BuildConfig.EXPERT_API_ENDPOINT
    }

    @GET("api/license")
    suspend fun getLicense(): LicenseDto

    @PUT("api/license")
    suspend fun putLicense(@Body license: LicenseDto): LicenseDto
}