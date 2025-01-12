package com.example.picosoundpad

import android.media.MediaPlayer
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream

class MainActivity : AppCompatActivity() {

    private lateinit var database: SoundDatabase
    private lateinit var soundDao: SoundDao

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

        findViewById<Button>(R.id.button_play_microphone).setOnClickListener {
            playSoundThroughMicrophone("/path/to/sound/file.mp3")
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
        val mediaPlayer = MediaPlayer().apply {
            setDataSource(filePath)
            prepare()
            start()
        }
    }

    private fun playSoundThroughMicrophone(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            val fileInputStream = FileInputStream(file)
            val buffer = ByteArray(1024)
            val audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                44100,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                buffer.size,
                AudioTrack.MODE_STREAM
            )
            audioTrack.play()

            // Чтение файла и передача данных в AudioTrack
            var bytesRead: Int
            while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                audioTrack.write(buffer, 0, bytesRead)
            }
            fileInputStream.close()
        }
    }
}
