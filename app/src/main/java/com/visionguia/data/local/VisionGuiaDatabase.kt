package com.visionguia.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val isPrimary: Boolean = false,
)

@Entity(tableName = "saved_places")
data class SavedPlaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
)


@Dao
interface VisionGuiaDao {
    @Query("SELECT * FROM emergency_contacts ORDER BY isPrimary DESC, name") fun contacts(): Flow<List<EmergencyContactEntity>>
    @Query("SELECT * FROM emergency_contacts WHERE isPrimary = 1 LIMIT 1") suspend fun primaryContact(): EmergencyContactEntity?
    @Upsert suspend fun saveContact(contact: EmergencyContactEntity)
    @Query("UPDATE emergency_contacts SET isPrimary = 0") suspend fun clearPrimaryContacts()
    @Transaction suspend fun savePrimaryContact(contact: EmergencyContactEntity) {
        clearPrimaryContacts()
        saveContact(contact.copy(isPrimary = true))
    }
    @Delete suspend fun deleteContact(contact: EmergencyContactEntity)
    @Query("DELETE FROM saved_places") suspend fun clearPlaces()
}

@Database(entities = [EmergencyContactEntity::class, SavedPlaceEntity::class], version = 2, exportSchema = true)
abstract class VisionGuiaDatabase : RoomDatabase() { abstract fun dao(): VisionGuiaDao }
