package expert.rightperception.attributesapp.data.repository.license.mapper

import expert.rightperception.attributesapp.data.api.model.LicenseDto
import expert.rightperception.attributesapp.data.db.model.LicenseDbModel
import expert.rightperception.attributesapp.domain.model.LicenseModel
import javax.inject.Inject

class LicenseMapper @Inject constructor() {

    fun map(licenseDto: LicenseDto): LicenseDbModel {
        return LicenseDbModel(
            id = licenseDto.id,
            externalId = licenseDto.externalId,
            paymentId = licenseDto.paymentId,
            start = licenseDto.start,
            finish = licenseDto.finish,
            status = licenseDto.status,
            createdAt = licenseDto.createdAt,
            createdBy = licenseDto.createdBy,
            modifiedAt = licenseDto.modifiedAt,
            modifiedBy = licenseDto.modifiedBy
        )
    }

    fun map(licenseDbModel: LicenseDbModel): LicenseModel {
        return LicenseModel(licenseDbModel.id)
    }
}