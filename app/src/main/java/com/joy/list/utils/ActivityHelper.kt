package com.joy.list.utils

import android.app.Activity
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.gson.Gson

import com.joy.list.R
import com.joy.list.activities.NotesActivity
import com.joy.list.network.ReminderReceiver

import java.util.*

/**
 * Implements methods commonly used across activities.
 */
class ActivityHelper : Application() {
    /**
     * Stores the constant, static components of the helper, so to speak.
     */
    companion object {
        const val EXTRA_LIST: String = "com.joy.utils.ActivityHelper.EXTRA_LIST"
        const val EXTRA_CONTENT: String = "com.joy.utils.ActivityHelper.EXTRA_CONTENT"
        const val EXTRA_CONTENT_TYPE: String = "com.joy.utils.ActivityHelper.EXTRA_CONTENT_TYPE"

        /**
         * Dismisses the reminder for an item.
         */
        fun dismissReminder(context: Context?, item: Item) {
            val alarmManager: AlarmManager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val broadcastIntent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = item.getReminderId()?.let { PendingIntent.getBroadcast(context, it, broadcastIntent, 0) }

            alarmManager.cancel(pendingIntent)
        }

        /**
         * Sets a reminder for an item.
         */
        fun setReminder(context: Context?, list: List, item: Item) {
            val alarmManager: AlarmManager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val broadcastIntent = Intent(context, ReminderReceiver::class.java)
            broadcastIntent.putExtra(EXTRA_LIST, Gson().toJson(list))
            broadcastIntent.putExtra(EXTRA_CONTENT, Gson().toJson(item))
            broadcastIntent.putExtra(EXTRA_CONTENT_TYPE, Gson().toJson(0))

            val pendingIntent = item.getReminderId()?.let { PendingIntent.getBroadcast(context, it, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT) }

            alarmManager.setExact(AlarmManager.RTC, getReminderInMillis(item), pendingIntent)
        }

        /**
         * Dismisses the reminder for a note.
         */
        fun dismissReminder(context: Context?, note: Note) {
            val alarmManager: AlarmManager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val broadcastIntent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = note.getReminderId()?.let { PendingIntent.getBroadcast(context, it, broadcastIntent, 0) }

            alarmManager.cancel(pendingIntent)
        }

        /**
         * Sets a reminder for a note.
         */
        fun setReminder(context: Context?, note: Note) {
            val list = List(NotesActivity.NOTES_TITLE, NotesActivity.NOTES_COLOR)

            val alarmManager: AlarmManager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val broadcastIntent = Intent(context, ReminderReceiver::class.java)
            broadcastIntent.putExtra(EXTRA_LIST, Gson().toJson(list))
            broadcastIntent.putExtra(EXTRA_CONTENT, Gson().toJson(note))
            broadcastIntent.putExtra(EXTRA_CONTENT_TYPE, Gson().toJson(1))

            val pendingIntent = note.getReminderId()?.let { PendingIntent.getBroadcast(context, it, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT) }

            alarmManager.setExact(AlarmManager.RTC, getReminderInMillis(note), pendingIntent)
        }

        /**
         * Hides the keyboard.
         */
        fun hideKeyboard(context: Context?, view: View?) {
            val inputMethodManager = context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
        }

        /**
         * Shows the keyboard.
         */
        fun showKeyboard(context: Context?, showFlags: Int) {
            val inputMethodManager = context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInput(showFlags, 0)
        }

        /**
         * Sets the status bar color of an activity.
         */
        fun setStatusBarColor(activity: AppCompatActivity, color: Int) {
            val window: Window = activity.window
            window.navigationBarColor = ContextCompat.getColor(activity, color)
            window.statusBarColor = ContextCompat.getColor(activity, color)
        }

        /**
         * Sets support action bar of an activity.
         */
        fun setSupportActionBar(activity: AppCompatActivity) {
            val toolbar: Toolbar = activity.findViewById(R.id.toolbar)
            activity.setSupportActionBar(toolbar)
            activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            activity.supportActionBar?.setDisplayShowHomeEnabled(true)
            activity.supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        /**
         * Sets the toolbar color of an activity.
         */
        fun setToolbarColor(activity: AppCompatActivity, color: Int) {
            val toolbar: Toolbar = activity.findViewById(R.id.toolbar)
            toolbar.setBackgroundColor(ContextCompat.getColor(activity, color))
        }

        /**
         * Gets the reminder of an item in milliseconds.
         */
        private fun getReminderInMillis(item: Item): Long {
            val calendar = Calendar.getInstance();

            if (item.getDueDateInMillis() != 12345678987654321L) {
                val currentHourInMillis: Long = item.getReminderHour()!!.times(3600000L)
                val currentMinuteInMillis: Long = item.getReminderMinute()!!.times(60000L);
                val currentTimeInMillis: Long = item.getDueDateInMillis()!!.plus(currentHourInMillis).plus(currentMinuteInMillis)

                calendar.timeInMillis = currentTimeInMillis
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, item.getReminderHour()!!)
                calendar.set(Calendar.MINUTE, item.getReminderMinute()!!)
                calendar.set(Calendar.SECOND, 0)

                if (calendar.before(Calendar.getInstance()))
                    calendar.add(Calendar.DATE, 1)
            }

            return calendar.timeInMillis
        }

        /**
         * Gets the reminder of a note in milliseconds.
         */
        private fun getReminderInMillis(note: Note): Long {
            val calendar = Calendar.getInstance();

            if (note.getDueDateInMillis() != 12345678987654321L) {
                val currentHourInMillis: Long = note.getReminderHour()!!.times(3600000L)
                val currentMinuteInMillis: Long = note.getReminderMinute()!!.times(60000L);
                val currentTimeInMillis: Long = note.getDueDateInMillis()!!.plus(currentHourInMillis).plus(currentMinuteInMillis)

                calendar.timeInMillis = currentTimeInMillis
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, note.getReminderHour()!!)
                calendar.set(Calendar.MINUTE, note.getReminderMinute()!!)
                calendar.set(Calendar.SECOND, 0)

                if (calendar.before(Calendar.getInstance()))
                    calendar.add(Calendar.DATE, 1)
            }

            return calendar.timeInMillis
        }
    }
}