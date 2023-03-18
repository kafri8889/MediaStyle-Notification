package com.anafthdev.mediastylenotification.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import com.anafthdev.mediastylenotification.MainActivity
import com.anafthdev.mediastylenotification.data.SongAction
import com.anafthdev.mediastylenotification.model.MusicState
import com.anafthdev.mediastylenotification.receiver.MediaPlayerReceiver
import com.anafthdev.mediastylenotification.R

object NotificationUtil {
	
	private const val channelID = "player_notification"
	private const val channelName = "Media Player"
	
	@RequiresApi(Build.VERSION_CODES.O)
	fun createChannel(context: Context) {
		val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		val channel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			channel.setAllowBubbles(false)
		}
		
		channel.setBypassDnd(true)
		
		notificationManager.createNotificationChannel(channel)
	}
	
	@Suppress("deprecation")
	fun foregroundNotification(context: Context): Notification {
		val pi = PendingIntent.getActivity(
			context,
			123,
			Intent(context, MainActivity::class.java),
			PendingIntent.FLAG_IMMUTABLE
		)
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			Notification.Builder(context, channelID)
				.setContentTitle("Music")
				.setContentText("Music running in the foreground")
				.setCategory(Notification.CATEGORY_SERVICE)
				.setContentIntent(pi)
				.build()
		} else {
			Notification.Builder(context)
				.setContentTitle("Music")
				.setContentText("Music running in the foreground")
				.setContentIntent(pi)
				.setCategory(Notification.CATEGORY_SERVICE)
				.build()
		}
	}
	
	@Suppress("deprecation")
	fun notificationMediaPlayer(
		context: Context,
		mediaStyle: Notification.MediaStyle,
		state: MusicState
	): Notification {
		val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			Notification.Builder(context, channelID)
		} else Notification.Builder(context)
		
		val playPauseIntent = Intent(context, MediaPlayerReceiver::class.java)
			.setAction(
				if (state.isPlaying) SongAction.Pause.ordinal.toString() else SongAction.Resume.ordinal.toString()
			)
		val playPausePI = PendingIntent.getBroadcast(
			context,
			1,
			playPauseIntent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
		val playPauseAction = Notification.Action.Builder(
			Icon.createWithResource(
				context,
				if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
			),
			"PlayPause",
			playPausePI
		).build()
		
		val previousIntent = Intent(context, MediaPlayerReceiver::class.java)
			.setAction(SongAction.Previous.ordinal.toString())
		val previousPI = PendingIntent.getBroadcast(
			context,
			2,
			previousIntent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
		val previousAction = Notification.Action.Builder(
			Icon.createWithResource(context, R.drawable.ic_previous),
			"Previous",
			previousPI
		).build()
		
		val nextIntent = Intent(context, MediaPlayerReceiver::class.java)
			.setAction(SongAction.Next.ordinal.toString())
		val nextPI = PendingIntent.getBroadcast(
			context,
			3,
			nextIntent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
		val nextAction = Notification.Action.Builder(
			Icon.createWithResource(context, R.drawable.ic_next),
			"Previous",
			nextPI
		).build()
		
		return builder
			.setStyle(mediaStyle)
			.setSmallIcon(R.drawable.ic_play)
			.setOnlyAlertOnce(true)
			.addAction(previousAction)
			.addAction(playPauseAction)
			.addAction(nextAction)
			.build()
	}
	
}