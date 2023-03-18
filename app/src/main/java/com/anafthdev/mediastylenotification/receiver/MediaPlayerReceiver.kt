package com.anafthdev.mediastylenotification.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.anafthdev.mediastylenotification.data.SongAction
import com.anafthdev.mediastylenotification.service.MediaPlayerService

class MediaPlayerReceiver: BroadcastReceiver() {
	
	override fun onReceive(context: Context, intent: Intent) {
		val serviceIntent = Intent(context, MediaPlayerService::class.java)
		val action = SongAction.values()[intent.action?.toInt() ?: SongAction.Nothing.ordinal]
		
		serviceIntent.action = action.ordinal.toString()
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			context.startForegroundService(serviceIntent)
		} else context.startService(serviceIntent)
	}
	
}