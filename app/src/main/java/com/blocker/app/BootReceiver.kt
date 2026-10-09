package com.blocker.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Phone restart ke baad Accessibility service system khud chalu karti hai.
        // Yahan kuch karna nahi hai.
    }
}
