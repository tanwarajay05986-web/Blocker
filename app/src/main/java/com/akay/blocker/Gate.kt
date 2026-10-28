package com.akay.blocker

import android.app.Activity
import android.app.AlertDialog
import android.text.InputType
import android.widget.EditText
import android.widget.FrameLayout

// Asks for the special code before something is turned off.
// While Config.CODE_ENFORCED is false the code is never asked.
object Gate {
    fun ask(a: Activity, ok: () -> Unit) {
        val code = Store.code()
        if (!Config.CODE_ENFORCED || code.isEmpty()) {
            ok()
            return
        }
        val et = EditText(a)
        et.hint = "Special code"
        et.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        val box = FrameLayout(a)
        box.setPadding(a.dp(22), a.dp(10), a.dp(22), 0)
        box.addView(et, FrameLayout.LayoutParams(-1, -2))
        AlertDialog.Builder(a)
            .setTitle("Enter special code")
            .setView(box)
            .setPositiveButton("OK") { _, _ ->
                if (et.text.toString() == code) ok() else a.toast("Wrong code")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
