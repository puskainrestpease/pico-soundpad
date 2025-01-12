package com.example.picosoundpad

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var database: SoundDatabase
    private lateinit var soundDao: SoundDao
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = SoundDatabase.getDatabase(this)
        soundDao = database.soundDao()

        // Кнопки для работы с базой данных
        findViewById<Button>(R.id.button_save_sound).setOnClickListener {
            saveSound("/path/to/sound/file.mp3", "Sound 1")
        }

        findViewById<Button>(R.id.button_play_sound).setOnClickListener {
            playSound("/path/to/sound/file.mp3")
        }

        findViewById<Button>(R.id.button_play_local_sound).setOnClickListener {
            // Проигрывание локально сохраненного звука
            playLocalSound("saved_sound.mp3")
        }

        // Перемотка вперед
        findViewById<Button>(R.id.button_forward).setOnClickListener {
            mediaPlayer?.apply {
                val currentPosition = player.currentPosition
                val newPosition = currentPosition + 5000 // Перемотка вперед на 5 секунд
                 if (newPosition < duration) {
                        seekTo(newPosition)
                } else {
                        seekTo(duration)
                }
            }
        }

        // Перемотка назад
        findViewById<Button>(R.id.button_backward).setOnClickListener {
            mediaPlayer?.apply {
                val currentPosition = currentPosition
                val newPosition = currentPosition - 5000 // Перемотка назад на 5 секунд
                if (newPosition > 0) {
                    seekTo(newPosition)
                } else {
                    seekTo(0)
                }
            }
        }

    private fun saveSound(filePath: String, name: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val sound = Sound(name = name, filePath = filePath)
            soundDao.insert(sound)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "Sound saved", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun playSound(filePath: String) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(filePath)
            prepare()
            start()
            setOnCompletionListener {
                Toast.makeText(this@MainActivity, "Playback completed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Локальное прослушивание аудиофайла из хранилища
    private fun playLocalSound(fileName: String) {
        val filePath = "${filesDir.absolutePath}/$fileName"
        val file = File(filePath)
        
        if (file.exists()) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
                setOnCompletionListener {
                    Toast.makeText(this@MainActivity, "Playback completed", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
    }
}
