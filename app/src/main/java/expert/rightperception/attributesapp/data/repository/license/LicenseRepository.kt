package expert.rightperception.attributesapp.data.repository.license

import expert.rightperception.attributesapp.data.api.ExpertApi
import expert.rightperception.attributesapp.data.db.LicenceDao

class LicenseRepository(
    private val expertApi: ExpertApi,
    private val licenceDao: LicenceDao
) {

//    suspend fun getLicense(): LicenseModel? {
//        try {
//            val license = expertApi.getLicense()
//        } catch (e: Exception) {
//            if (e is HttpException) {
//                if (e.code() == 404) {
//                    expertApi.putLicense(LicenseDto())
//                }
//            }
//        }
//    }
}