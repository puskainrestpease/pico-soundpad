package com.example.picosounpad  
  
import android.content.Intent  
import android.media.MediaPlayer  
import android.net.Uri  
import android.os.Bundle  
import android.provider.OpenableColumns  
import android.util.Log  
import android.widget.Button  
import android.widget.SeekBar  
import android.widget.Toast  
import androidx.appcompat.app.AppCompatActivity  
import androidx.lifecycle.lifecycleScope  
import kotlinx.coroutines.Dispatchers  
import kotlinx.coroutines.launch  
import java.io.File  
import java.io.FileOutputStream  
import java.io.IOException  
  
class MainActivity : AppCompatActivity() {  
  
    private lateinit var database: SoundDatabase  
    private lateinit var soundDao: SoundDao  
    private var mediaPlayer: MediaPlayer? = null  
  
    companion object {  
        private const val REQUEST_CODE = 123  
    }  
  
    override fun onCreate(savedInstanceState: Bundle?) {  
        super.onCreate(savedInstanceState)  
        setContentView(R.layout.activity_main)  
  
        database = SoundDatabase.getDatabase(this)  
        soundDao = database.soundDao()  
  
        // Кнопки для работы с базой данных  
        findViewById<Button>(R.id.button_save_sound).setOnClickListener {  
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)  
            intent.type = "audio/*"  
            startActivityForResult(intent, REQUEST_CODE)  
        }  
  
        findViewById<Button>(R.id.button_play_sound).setOnClickListener {  
            lifecycleScope.launch(Dispatchers.IO) {  
                val sounds = soundDao.getAllSounds()  
                if (sounds.isNotEmpty()) {  
                    val sound = sounds[0]  
                    runOnUiThread {  
                        playSound(sound.filePath)  
                    }  
                } else {  
                    runOnUiThread {  
                        Toast.makeText(this@MainActivity, "No sounds available", Toast.LENGTH_SHORT).show()  
                    }  
                }  
            }  
        }  
  
        findViewById<Button>(R.id.button_play_local_sound).setOnClickListener {  
            // Проигрывание локально сохраненного звука  
            playLocalSound("saved_sound.mp3")  
        }  
  
        // волайм кантрол 
        findViewById<SeekBar>(R.id.seekBar_volume).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {  
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {  
                if (fromUser) {  
                    val volume = progress / 100f  
                    mediaPlayer?.setVolume(volume, volume)  
                }  
            }  
  
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}  
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}  
        })  
  
        // Перемотка вперед  
        findViewById<Button>(R.id.button_forward).setOnClickListener {  
            mediaPlayer?.apply {  
                val currentPosition = currentPosition // Fixed: was player.currentPosition  
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
    }  
  
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {  
        super.onActivityResult(requestCode, resultCode, data)  
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {  
            data?.data?.let { uri ->  
                val fileName = getFileNameFromUri(uri)  
                val internalFilePath = copyFileToInternalStorage(uri, fileName)  
                if (internalFilePath != null) {  
                    saveSound(internalFilePath, fileName)  
                }  
            }  
        }  
    }  
  
    private fun getFileNameFromUri(uri: Uri): String {  
        var fileName = "sound_file"  
        val cursor = contentResolver.query(uri, null, null, null, null)  
        cursor?.use {  
            if (it.moveToFirst()) {  
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)  
                if (nameIndex != -1) {  
                    fileName = it.getString(nameIndex)  
                }  
            }  
        }  
        return fileName  
    }  
  
    private fun copyFileToInternalStorage(uri: Uri, fileName: String): String? {  
        val inputStream = contentResolver.openInputStream(uri)  
        val file = File(filesDir, fileName)  
          
        try {  
            inputStream?.use { input ->  
                FileOutputStream(file).use { output ->  
                    val buffer = ByteArray(4 * 1024) // 4k buffer  
                    var read: Int  
                    while (input.read(buffer).also { read = it } != -1) {  
                        output.write(buffer, 0, read)  
                    }  
                    output.flush()  
                }  
            }  
            return file.absolutePath  
        } catch (e: IOException) {  
            e.printStackTrace()  
            Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show()  
            return null  
        }  
    }  
  
    private fun saveSound(filePath: String, name: String) {  
        lifecycleScope.launch(Dispatchers.IO) {  
            val sound = Sound(name = name, filePath = filePath)  
            soundDao.insert(sound)  
            runOnUiThread {  
                Toast.makeText(this@MainActivity, "Sound saved: $name", Toast.LENGTH_SHORT).show()  
            }  
        }  
    }  
  
    private fun playSound(filePath: String) {  
        try {  
            mediaPlayer?.release()  
            mediaPlayer = MediaPlayer().apply {  
                setDataSource(filePath)  
                prepare()  
                start()  
                setOnCompletionListener {  
                    Toast.makeText(this@MainActivity, "Playback completed", Toast.LENGTH_SHORT).show()  
                }  
            }  
        } catch (e: IOException) {  
            Log.e("MainActivity", "Error playing sound", e)  
            Toast.makeText(this, "Error playing sound: ${e.message}", Toast.LENGTH_SHORT).show()  
        }  
    }  
  
    // Локальное прослушивание аудиофайла из хранилища  
    private fun playLocalSound(fileName: String) {  
        val filePath = "${filesDir.absolutePath}/$fileName"  
        val file = File(filePath)  
          
        if (file.exists()) {  
            try {  
                mediaPlayer?.release()  
                mediaPlayer = MediaPlayer().apply {  
                    setDataSource(filePath)  
                    prepare()  
                    start()  
                    setOnCompletionListener {  
                        Toast.makeText(this@MainActivity, "Playback completed", Toast.LENGTH_SHORT).show()  
                    }  
                }  
            } catch (e: IOException) {  
                Log.e("MainActivity", "Error playing local sound", e)  
                Toast.makeText(this, "Error playing sound: ${e.message}", Toast.LENGTH_SHORT).show()  
            }  
        } else {  
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()  
        }  
    }  
  
    override fun onStop() {  
        super.onStop()  
        mediaPlayer?.release()  
        mediaPlayer = null  
    }  
}
