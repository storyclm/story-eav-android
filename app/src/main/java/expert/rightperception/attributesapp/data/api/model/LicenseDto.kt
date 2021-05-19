package expert.rightperception.attributesapp.data.api.model

data class LicenseDto(

    val id: String = "",

    val externalId: String? = null,

    val paymentId: String? = null,

    val start: String = "",

    val finish: String = "",

    val status: String = "",

    val createdAt: String? = null,

    val createdBy: String? = null,

    val modifiedAt: String? = null,

    val modifiedBy: String? = null

)