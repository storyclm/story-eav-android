package expert.rightperception.attributesapp.data.repository.license

import expert.rightperception.attributesapp.data.api.ExpertApi
import expert.rightperception.attributesapp.data.api.model.LicenseDto
import expert.rightperception.attributesapp.data.db.LicenceDao
import expert.rightperception.attributesapp.data.repository.license.mapper.LicenseMapper
import expert.rightperception.attributesapp.domain.model.LicenseModel
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LicenseRepository @Inject constructor(
    private val expertApi: ExpertApi,
    private val licenceDao: LicenceDao,
    private val licenseMapper: LicenseMapper
) {

    suspend fun getLicense(): LicenseModel? {
        return try {
            updateLicense(expertApi.getLicense())
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is HttpException && e.code() == 404) {
                try {
                    updateLicense(expertApi.putLicense(LicenseDto()))
                } catch (e: Exception) {
                    e.printStackTrace()
                    getLicenseFromDb()
                }
            } else {
                getLicenseFromDb()
            }
        }
    }

    private suspend fun updateLicense(licenseDto: LicenseDto): LicenseModel? {
        licenceDao.insert(licenseMapper.map(licenseDto))
        return getLicenseFromDb()
    }

    private suspend fun getLicenseFromDb(): LicenseModel? {
        return licenceDao.get()?.let { licenseMapper.map(it) }
    }
}