package com.example.picosounpad  
  
import androidx.room.Entity  
import androidx.room.PrimaryKey  
  
@Entity(tableName = "sounds")  
data class Sound(  
    @PrimaryKey(autoGenerate = true)  
    val id: Int = 0,  
    val name: String,  
    val filePath: String  
)
