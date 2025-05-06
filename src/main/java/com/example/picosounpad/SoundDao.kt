package com.example.picosounpad  
  
import androidx.room.Dao  
import androidx.room.Insert  
import androidx.room.OnConflictStrategy  
import androidx.room.Query  
  
@Dao  
interface SoundDao {  
    @Insert(onConflict = OnConflictStrategy.REPLACE)  
    suspend fun insert(sound: Sound)  
      
    @Query("SELECT * FROM sounds")  
    suspend fun getAllSounds(): List<Sound>  
      
    @Query("SELECT * FROM sounds WHERE id = :id")  
    suspend fun getSoundById(id: Int): Sound?  
      
    @Query("DELETE FROM sounds WHERE id = :id")  
    suspend fun deleteSound(id: Int)  
}
