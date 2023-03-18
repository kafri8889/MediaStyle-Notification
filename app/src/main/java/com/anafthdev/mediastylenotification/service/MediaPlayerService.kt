package com.anafthdev.mediastylenotification.service

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Binder
import android.os.IBinder
import android.view.KeyEvent
import com.anafthdev.mediastylenotification.data.SongAction
import com.anafthdev.mediastylenotification.data.SongController
import com.anafthdev.mediastylenotification.model.MusicState
import com.anafthdev.mediastylenotification.util.NotificationUtil
import kotlin.system.exitProcess

class MediaPlayerService: Service() {
	
	private lateinit var mediaSession: MediaSession
	private lateinit var mediaStyle: Notification.MediaStyle
	private lateinit var notificationManager: NotificationManager
	
	private val binder: IBinder = MediaPlayerServiceBinder()
	
	private var songController: SongController? = null
	private var isForegroundService = false
	
	override fun onCreate() {
		super.onCreate()
		
		notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
		mediaSession = MediaSession(this, "MediaPlayerSessionService")
		mediaStyle = Notification.MediaStyle().setMediaSession(mediaSession.sessionToken)
		
		mediaSession.setCallback(object : MediaSession.Callback() {
			override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
				if (Intent.ACTION_MEDIA_BUTTON == mediaButtonIntent.action) {
					val event = mediaButtonIntent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
					
					event?.let {
						when (it.keyCode) {
							KeyEvent.KEYCODE_MEDIA_PLAY -> songController?.play()
							KeyEvent.KEYCODE_MEDIA_PAUSE -> songController?.pause()
							KeyEvent.KEYCODE_MEDIA_NEXT -> songController?.next()
							KeyEvent.KEYCODE_MEDIA_PREVIOUS -> songController?.previous()
							else -> {}
						}
					}
				}
				
				return true
			}
		})
		
		// Start foreground notification
		startForeground(1, NotificationUtil.foregroundNotification(this)).also {
			isForegroundService = true
		}
	}
	
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		// action passed from [MediaPlayerReceiver]
		when (SongAction.values()[intent?.action?.toInt() ?: SongAction.Nothing.ordinal]) {
			SongAction.Pause -> songController?.pause()
			SongAction.Resume -> songController?.play()
			SongAction.Stop -> songController?.stop()
			SongAction.Next -> songController?.next()
			SongAction.Previous -> songController?.previous()
			SongAction.Nothing -> {}
		}
		
		val musicState = intent?.getParcelableExtra<MusicState>("musicState")
		
		musicState?.let { newState ->
			if (isForegroundService) {
				mediaSession.setPlaybackState(
					PlaybackState.Builder()
						.setState(
							if (newState.isPlaying) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED,
							newState.currentDuration,
							1f
						)
						.setActions(PlaybackState.ACTION_PLAY_PAUSE)
						.build()
				)
				
				mediaSession.setMetadata(
					MediaMetadata.Builder()
						.putString(MediaMetadata.METADATA_KEY_TITLE, newState.title)
						.putString(MediaMetadata.METADATA_KEY_ALBUM, newState.album)
						.putString(MediaMetadata.METADATA_KEY_ARTIST, newState.artist)
						.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, newState.albumArt)
						.putLong(MediaMetadata.METADATA_KEY_DURATION, newState.duration)
						.build()
				)
				
				notificationManager.notify(
					0,
					NotificationUtil.notificationMediaPlayer(
						this,
						Notification.MediaStyle().setMediaSession(mediaSession.sessionToken),
						newState
					)
				)
			}
		}
		
		return START_NOT_STICKY
	}
	
	override fun onBind(intent: Intent?): IBinder {
		return binder
	}
	
	override fun onDestroy() {
		isForegroundService = false
		songController?.stop()
		mediaSession.release()
		stopSelf()
		stopForeground(STOP_FOREGROUND_REMOVE)
		
		super.onDestroy()
	}
	
	override fun onTaskRemoved(rootIntent: Intent?) {
		super.onTaskRemoved(rootIntent)
		exitProcess(0)
	}
	
	fun setSongController(controller: SongController) {
		songController = controller
	}
	
	inner class MediaPlayerServiceBinder: Binder() {
		fun getService(): MediaPlayerService {
			return this@MediaPlayerService
		}
	}
	
}