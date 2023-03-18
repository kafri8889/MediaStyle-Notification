package com.anafthdev.mediastylenotification

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.anafthdev.mediastylenotification.data.SongController
import com.anafthdev.mediastylenotification.model.MusicState
import com.anafthdev.mediastylenotification.service.MediaPlayerService
import com.anafthdev.mediastylenotification.ui.theme.ForBloggingTheme
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import timber.log.Timber


class MainActivity : ComponentActivity(), ServiceConnection {
	
	private lateinit var exoPlayer: ExoPlayer
	
	private var songRunnable: Runnable = Runnable {}
	private var songHandler: Handler = Handler(Looper.getMainLooper())
	
	private var currentMusicState = MusicState() // default MusicState
	
	private var mediaPlayerService: MediaPlayerService? = null
	private val songController = object : SongController {
		override fun play() {
			exoPlayer.play()
		}
		
		override fun pause() {
			exoPlayer.pause()
		}
		
		override fun next() {
			// TODO: Next
		}
		
		override fun previous() {
			// TODO: Previous
		}
		
		override fun stop() {
			exoPlayer.stop()
		}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		// Initialize ExoPlayer
		exoPlayer = ExoPlayer.Builder(this)
			.setLooper(Looper.getMainLooper())
			.build()
		
		exoPlayer.apply {
			// Load music from assets
			addMediaItem(
				MediaItem.fromUri("file:///android_asset/le_sserafim_no_celestial.mp3")
			)
			
			// Prepare audio
			prepare()
		
			// Init state
			currentMusicState = MusicState(
				isPlaying = exoPlayer.isPlaying,
				title = "No Celestial",
				artist = "LE SSERAFIM",
				album = "ANTIFRAGILE",
				albumArt = BitmapFactory.decodeStream(assets.open("no_celestial_album_art.jpg")),
				duration = exoPlayer.duration
			)
		}
		
		songRunnable = Runnable {
			// Update state every 1 seconds
			currentMusicState = currentMusicState.copy(
				isPlaying = exoPlayer.isPlaying,
				currentDuration = exoPlayer.currentPosition,
				duration = exoPlayer.duration
			)
			
			startOrUpdateService()
			
			songHandler.postDelayed(songRunnable, 1000)
		}
		
		songHandler.post(songRunnable)
		
		// Bind service
		bindService(
			startOrUpdateService(),
			this,
			BIND_AUTO_CREATE
		)
	
		setContent {
			ForBloggingTheme {
				Surface(
					color = MaterialTheme.colorScheme.background,
					modifier = Modifier
						.fillMaxSize()
				) {
					MainScreen()
				}
			}
		}
	}
	
	private fun startOrUpdateService(): Intent {
		// Start service
		val serviceIntent = Intent(this, MediaPlayerService::class.java).apply {
			putExtra("musicState", currentMusicState)
		}
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForegroundService(serviceIntent)
		} else startService(serviceIntent)
		
		return serviceIntent
	}
	
	override fun onDestroy() {
		super.onDestroy()
		
		try {
			unbindService(this)
		} catch (e: IllegalArgumentException) {
			Timber.e(e, "Service not registered")
		}
	}
	
	override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
		val binder = p1 as MediaPlayerService.MediaPlayerServiceBinder
		
		mediaPlayerService = binder.getService()
		mediaPlayerService!!.setSongController(songController)
	}
	
	override fun onServiceDisconnected(p0: ComponentName?) {
		mediaPlayerService = null
	}
	
	@Composable
	fun MainScreen() {
		
		Column {
			Button(
				onClick = {
					if (exoPlayer.isPlaying) songController.pause()
					else songController.play()
				}
			) {
				Text("Play/Pause")
			}
		}
		
	}
}
