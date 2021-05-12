package expert.rightperception.attributesapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import expert.rightperception.attributesapp.data.db.model.LicenseDbModel

@Database(
    entities = [
        LicenseDbModel::class
    ], version = 1
)
abstract class AttributesDatabase : RoomDatabase() {

    abstract fun licenceDao(): LicenceDao

    companion object {
        fun build(context: Context): AttributesDatabase {
            return Room.databaseBuilder(context, AttributesDatabase::class.java, DATABASE_NAME)
                .build()
        }

        private const val DATABASE_NAME = "attributes_database.db"
    }
}