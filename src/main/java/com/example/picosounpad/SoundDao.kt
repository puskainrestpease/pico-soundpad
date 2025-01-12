package com.example.picosoundpad

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SoundDao {

    @Insert
    suspend fun insert(sound: Sound)

    @Query("SELECT * FROM sounds")
    suspend fun getAllSounds(): List<Sound>

    @Query("DELETE FROM sounds WHERE id = :id")
    suspend fun deleteById(id: Int)
}
