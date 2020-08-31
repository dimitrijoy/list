package com.joy.list.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.joy.list.utils.*
import com.joy.list.utils.List


/**
 * Pushes and dismisses reminders.
 */
class ReminderReceiver: BroadcastReceiver() {

    /**
     * Pushes/dismisses a reminder.
     */
    override fun onReceive(p0: Context?, p1: Intent?) {
        val list: List = Gson().fromJson<List>(p1?.getStringExtra(ActivityHelper.EXTRA_LIST), List::class.java)
        val contentType: Int = Gson().fromJson<Int>(p1?.getStringExtra(ActivityHelper.EXTRA_CONTENT_TYPE), Int::class.java)

        if (contentType == 0) {
            val item: Item = Gson().fromJson<Item>(p1?.getStringExtra(ActivityHelper.EXTRA_CONTENT), Item::class.java)

            val reminderHelper = ReminderHelper(p0, list, item)
            val builder: NotificationCompat.Builder = reminderHelper.getChannelNotification(ReminderHelper.ITEM_NOTIFICATION_CHANNEL_ID)

            reminderHelper.getNotificationManager()?.notify(item.getReminderId()!!, builder.build())
        } else {
            val note: Note = Gson().fromJson<Note>(p1?.getStringExtra(ActivityHelper.EXTRA_CONTENT), Note::class.java)

            val reminderHelper = ReminderHelper(p0, list, note)
            val builder: NotificationCompat.Builder = reminderHelper.getChannelNotification(ReminderHelper.NOTE_NOTIFICATION_CHANNEL_ID)

            reminderHelper.getNotificationManager()?.notify(note.getReminderId()!!, builder.build())
        }
    }
}