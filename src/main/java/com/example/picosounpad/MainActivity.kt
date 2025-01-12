package com.example.picosoundpad

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.AudioTrack
import android.media.AudioFormat
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
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

        findViewById<Button>(R.id.button_play_microphone).setOnClickListener {
            playSoundThroughMicrophone("/path/to/sound/file.mp3")
        }

        // Перемотка вперед
        findViewById<Button>(R.id.button_forward).setOnClickListener {
            mediaPlayer?.let { player ->
                val currentPosition = player.currentPosition
                val newPosition = currentPosition + 5000 // Перемотка вперед на 5 секунд
                if (newPosition < player.duration) {
                    player.seekTo(newPosition)
                } else {
                    player.seekTo(player.duration)
                }
            }
        }

        // Перемотка назад
        findViewById<Button>(R.id.button_backward).setOnClickListener {
            mediaPlayer?.let { player ->
                val currentPosition = player.currentPosition
                val newPosition = currentPosition - 5000 // Перемотка назад на 5 секунд
                if (newPosition > 0) {
                    player.seekTo(newPosition)
                } else {
                    player.seekTo(0)
                }
            }
        }

        // Управление громкостью с помощью SeekBar
        findViewById<SeekBar>(R.id.seekBar_volume).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mediaPlayer?.setVolume(progress / 100f, progress / 100f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
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

    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
    }
}
