package com.anafthdev.mediastylenotification

import android.app.Application
import android.os.Build
import com.anafthdev.mediastylenotification.util.NotificationUtil

class MusicApplication: Application() {
	
	override fun onCreate() {
		super.onCreate()
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationUtil.createChannel(this)
		}
	}
}