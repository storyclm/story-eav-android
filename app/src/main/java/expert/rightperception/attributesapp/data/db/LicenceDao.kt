package expert.rightperception.attributesapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import expert.rightperception.attributesapp.data.db.model.LicenseDbModel
import kotlinx.coroutines.flow.Flow

@Dao
abstract class LicenceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(license: LicenseDbModel)

    @Query("DELETE FROM license")
    abstract suspend fun clear()

    @Query("SELECT * FROM license")
    abstract suspend fun get(): LicenseDbModel?

    @Query("SELECT * FROM license")
    abstract fun observe(): Flow<LicenseDbModel?>
}