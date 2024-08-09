package com.example.displaymusic

import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import androidx.lifecycle.lifecycleScope
import com.example.displaymusic.databinding.ActivityMainBinding
import com.example.displaymusic.objects.Musica
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var mediaPlayer: MediaPlayer
    private var isPlaying = false

    // Lista de músicas
    private var songs = mutableListOf<Musica>()
    private var currentSongIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setSupportActionBar(binding.toolbar)

        // Carregar músicas do arquivo JSON
        songs = Musica().jsonForMusica(this)

        // Inicializa o MediaPlayer com a primeira música
        mediaPlayer = MediaPlayer.create(this, songs[currentSongIndex].musicaArquivo)

        // Configura o nome da música
        binding.songName.text = "${songs[currentSongIndex].titulo} - ${songs[currentSongIndex].autor}"

        // Ao carregar a música
        binding.coverImageView.setImageResource(songs[currentSongIndex].capaArquivo)

        // Perfil do autor
        binding.authorProfileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(songs[currentSongIndex].perfilAutor))
            startActivity(intent)

            mediaPlayer.pause()
            binding.playButton.setImageResource(R.drawable.baseline_play_circle_64)
            isPlaying = false
        }

        // Configura o botão de Play/Pause
        binding.playButton.setOnClickListener {
            if (isPlaying) {
                mediaPlayer.pause()
                binding.playButton.setImageResource(R.drawable.baseline_play_circle_64)
            } else {
                mediaPlayer.start()
                binding.playButton.setImageResource(R.drawable.baseline_pause_circle_64)
                startUpdatingSeekBar()
            }
            isPlaying = !isPlaying
        }

        // Configura o botão de Voltar
        binding.prevButton.setOnClickListener {
            if (currentSongIndex > 0) {
                currentSongIndex--
                changeSong()
            }
        }

        // Configura o botão de Avançar
        binding.nextButton.setOnClickListener {
            if (currentSongIndex < songs.size - 1) {
                currentSongIndex++
                changeSong()
            }
        }

        // Configura a SeekBar de progresso da música
        binding.musicSeekBar.max = mediaPlayer.duration

        binding.musicSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Configura o controle de volume
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        binding.volumeControl.max = maxVolume
        binding.volumeControl.progress = currentVolume

        // Atualiza o ícone de volume inicialmente
        updateVolumeIcon(currentVolume, maxVolume, binding.volumeIcon)

        binding.volumeControl.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                updateVolumeIcon(progress, maxVolume,  binding.volumeIcon)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Mostrar ou esconder a barra de volume ao clicar no ícone
        binding.volumeIcon.setOnClickListener {
            if (binding.volumeControl.visibility == View.GONE) {
                binding.volumeControl.visibility = View.VISIBLE
            } else {
                binding.volumeControl.visibility = View.GONE
            }
        }

        // Listener para detectar o fim da música
        mediaPlayer.setOnCompletionListener {
            resetPlayer(binding.playButton)
        }

        // Atualiza o estado dos botões
        updateButtonsState()
    }

    // Função para atualizar o ícone de volume
    private fun updateVolumeIcon(volume: Int, maxVolume: Int, volumeIcon: ImageView) {
        when {
            volume == 0 -> {
                volumeIcon.setImageResource(R.drawable.baseline_volume_mute_24)
            }
            volume <= maxVolume / 2 -> {
                volumeIcon.setImageResource(R.drawable.baseline_volume_down_24)
            }
            else -> {
                volumeIcon.setImageResource(R.drawable.baseline_volume_up_24)
            }
        }
    }

    private fun startUpdatingSeekBar() {
        lifecycleScope.launch {
            while (mediaPlayer.isPlaying) {
                binding.musicSeekBar.progress = mediaPlayer.currentPosition
                delay(1000)
            }
        }
    }

    // Pausar a música, redefinir a barra de progresso e o botão de play
    private fun resetPlayer(playButton: ImageButton) {
        mediaPlayer.seekTo(0)
        binding.musicSeekBar.progress = 0
        playButton.setImageResource(R.drawable.baseline_play_circle_64)
        isPlaying = false
    }

    private fun changeSong() {
        mediaPlayer.release() // Libera o MediaPlayer atual
        mediaPlayer = MediaPlayer.create(this, songs[currentSongIndex].musicaArquivo)
        mediaPlayer.setOnCompletionListener {
            resetPlayer(binding.playButton)
        }

        binding.songName.text = "${songs[currentSongIndex].titulo} - ${songs[currentSongIndex].autor}"
        binding.coverImageView.setImageResource(songs[currentSongIndex].capaArquivo)

        binding.authorProfileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(songs[currentSongIndex].perfilAutor))
            startActivity(intent)

            mediaPlayer.pause()
            binding.playButton.setImageResource(R.drawable.baseline_play_circle_64)
            isPlaying = false
        }

        binding.musicSeekBar.max = mediaPlayer.duration
        mediaPlayer.start()
        binding.playButton.setImageResource(R.drawable.baseline_pause_circle_64)
        isPlaying = true
        startUpdatingSeekBar()
        updateButtonsState()
    }

    private fun updateButtonsState() {
        binding.prevButton.isEnabled = currentSongIndex > 0
        binding.prevButton.alpha = if (currentSongIndex > 0) 1.0f else 0.5f

        binding.nextButton.isEnabled = currentSongIndex < songs.size - 1
        binding.nextButton.alpha = if (currentSongIndex < songs.size - 1) 1.0f else 0.5f
    }

    override fun onDestroy() {
        super.onDestroy()
        // Libera recursos ao destruir a atividade
        mediaPlayer.release()
    }
}