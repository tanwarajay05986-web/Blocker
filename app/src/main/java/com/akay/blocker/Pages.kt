package com.akay.blocker

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.widget.TextView

// Stage 2 will replace these two screens (Blocklist and Settings).
object Pages {

    fun blocklist(a: Activity): View = soon(a, "Blocklist")

    fun settings(a: Activity): View = soon(a, "Settings")

    private fun soon(a: Activity, name: String): View {
        val t = TextView(a)
        t.text = name + "\nComing in Stage 2"
        t.gravity = Gravity.CENTER
        t.textSize = 16f
        t.setTextColor(C.sub)
        return t
    }
}
