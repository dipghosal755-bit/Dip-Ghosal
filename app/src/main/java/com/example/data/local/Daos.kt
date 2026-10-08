package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ContactEntity
import com.example.data.model.EmergencyEntity
import com.example.data.model.LocationHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyDao {
    @Query("SELECT * FROM emergencies ORDER BY timestamp DESC")
    fun getAllEmergencies(): Flow<List<EmergencyEntity>>

    @Query("SELECT * FROM emergencies WHERE status = 'ACTIVE' ORDER BY timestamp DESC LIMIT 1")
    fun getActiveEmergency(): Flow<EmergencyEntity?>

    @Query("SELECT * FROM emergencies WHERE status = 'ACTIVE' ORDER BY timestamp DESC LIMIT 1")
    suspend fun getActiveEmergencySync(): EmergencyEntity?

    @Query("SELECT * FROM emergencies WHERE emergencyId = :id")
    suspend fun getEmergencyById(id: String): EmergencyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergency(emergency: EmergencyEntity)

    @Update
    suspend fun updateEmergency(emergency: EmergencyEntity)

    @Query("UPDATE emergencies SET status = :status, lastUpdated = :now WHERE emergencyId = :emergencyId")
    suspend fun updateStatus(emergencyId: String, status: String, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM emergencies")
    suspend fun clearAll()
}

@Dao
interface LocationDao {
    @Query("SELECT * FROM location_history WHERE emergencyId = :emergencyId ORDER BY timestamp ASC")
    fun getLocationHistory(emergencyId: String): Flow<List<LocationHistoryEntity>>

    @Query("SELECT * FROM location_history WHERE emergencyId = :emergencyId ORDER BY timestamp ASC")
    suspend fun getLocationHistorySync(emergencyId: String): List<LocationHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationHistoryEntity)

    @Query("DELETE FROM location_history WHERE emergencyId = :emergencyId")
    suspend fun clearLocationsForEmergency(emergencyId: String)
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY isPrimary DESC, name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts")
    suspend fun getAllContactsSync(): List<ContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteContact(id: Long)

    @Query("SELECT COUNT(*) FROM contacts")
    suspend fun getCount(): Int
}
