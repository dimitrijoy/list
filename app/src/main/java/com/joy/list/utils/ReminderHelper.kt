package com.joy.list.utils

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.joy.list.R
import com.joy.list.activities.*

/**
 * Handles reminders.
 */
class ReminderHelper<T>(context: Context?, private val list: List, private val content: T) : ContextWrapper(context) {
    private var notificationManager: NotificationManager? = null

    /**
     * Stores the constant, static variables of the class, so to speak.
     */
    companion object {
        const val ITEM_NOTIFICATION_CHANNEL_ID = "channel1"
        const val ITEM_NOTIFICATION_CHANNEL_NAME = "Item"
        const val NOTE_NOTIFICATION_CHANNEL_ID = "channel2"
        const val NOTE_NOTIFICATION_CHANNEL_NAME = "Note"
    }

    /**
     * Initializes the reminder.
     */
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            initializeChannels()
    }

    /**
     * Gets a particular reminder.
     */
    fun getChannelNotification(channelId: String): NotificationCompat.Builder {
        var contentTitle: String? = null
        var contentText: String? = null
        var subText: String? = null
        val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(applicationContext)
        var pendingIntent: PendingIntent? = null

        when (content) {
            is Item -> {
                contentTitle = list.getTitle()
                contentText = content.getTitle()
                stackBuilder.addNextIntent(Intent(applicationContext, MainActivity::class.java))
                stackBuilder.addNextIntent(MainActivity.getStartIntent(applicationContext, ListActivity::class.java, list))
                stackBuilder.addNextIntent(ListActivity.getStartIntent(applicationContext, ItemActivity::class.java, list, content, true))
                pendingIntent = content.getReminderId()?.let { stackBuilder.getPendingIntent(it, PendingIntent.FLAG_UPDATE_CURRENT) }
            }
            is Note -> {
                if (!content.getTitle().isNullOrEmpty())
                    contentTitle = content.getTitle()
                contentText = content.getText()
                subText = list.getTitle()
                stackBuilder.addNextIntent(Intent(applicationContext, MainActivity::class.java))
                stackBuilder.addNextIntent(MainActivity.getStartIntent(this, NotesActivity::class.java))
                stackBuilder.addNextIntent(NotesActivity.getStartIntent(applicationContext, NoteActivity::class.java, content,
                    insertNote = false,
                    fromReminder = true
                ))
                pendingIntent = content.getReminderId()?.let { stackBuilder.getPendingIntent(it, PendingIntent.FLAG_UPDATE_CURRENT) }
            }
        }

        return NotificationCompat.Builder(applicationContext, channelId)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSubText(subText)
            .setSmallIcon(R.drawable.ic_logo)
    }

    /**
     * Gets the notification manger.
     */
    fun getNotificationManager(): NotificationManager? {
        if (notificationManager == null)
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager
    }

    /**
     * Initializes the channels for handling item and note reminders, respectively.
     */
    @TargetApi(Build.VERSION_CODES.O)
    private fun initializeChannels() {
        val itemChannel = NotificationChannel(ITEM_NOTIFICATION_CHANNEL_ID, ITEM_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        val noteChannel = NotificationChannel(NOTE_NOTIFICATION_CHANNEL_ID, NOTE_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)

        getNotificationManager()!!.createNotificationChannel(itemChannel)
        getNotificationManager()!!.createNotificationChannel(noteChannel)
    }
}