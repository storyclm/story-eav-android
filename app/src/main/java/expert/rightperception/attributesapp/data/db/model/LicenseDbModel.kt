package expert.rightperception.attributesapp.data.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "license")
data class LicenseDbModel(

    @PrimaryKey
    val id: String,

    val externalId: String?,

    val paymentId: String?,

    val start: String,

    val finish: String,

    val status: String,

    val createdAt: String?,

    val createdBy: String?,

    val modifiedAt: String?,

    val modifiedBy: String?
)