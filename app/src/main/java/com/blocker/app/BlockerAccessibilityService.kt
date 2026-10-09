package com.blocker.app

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.TextView

class BlockerAccessibilityService : AccessibilityService() {

    private var overlay: View? = null
    private var lastCheck = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!BlocklistManager.isProtectionOn(this)) return

        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return

        // 1) Blocked app khula? Turant overlay chadhao
        if (pkg in BlocklistManager.blockedApps(this)) {
            showOverlay()
            return
        }

        // 2) Uninstall protection
        if (pkg == "com.android.packageinstaller" ||
            pkg == "com.google.android.packageinstaller"
        ) {
            val txt = grabText()
            if (txt.contains("blocker") &&
                (txt.contains("uninstall") || txt.contains("अनइंस्टॉल") || txt.contains("हटा"))
            ) {
                showOverlay()
                performGlobalAction(GLOBAL_ACTION_BACK)
                return
            }
        }

        // 3) Keyword / website detection (500ms throttle)
        val now = System.currentTimeMillis()
        if (now - lastCheck < 500) return
        lastCheck = now

        val text = grabText()
        if (BlocklistManager.isTextBlocked(this, text)) {
            showOverlay()
        }
    }

    private fun grabText(): String {
        val root = rootInActiveWindow ?: return ""
        val sb = StringBuilder()
        try {
            collect(root, sb, 0)
        } finally {
            root.recycle()
        }
        return sb.toString()
    }

    private fun collect(node: AccessibilityNodeInfo, sb: StringBuilder, depth: Int) {
        if (depth > 30 || sb.length > 4000) return
        node.text?.let { sb.append(it).append(' ') }
        node.contentDescription?.let { sb.append(it).append(' ') }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { collect(it, sb, depth + 1) }
        }
    }

    private fun showOverlay() {
        if (overlay != null) return
        if (!Settings.canDrawOverlays(this)) return
        try {
            val wm = getSystemService(WINDOW_SERVICE) as WindowManager
            val view = LayoutInflater.from(this).inflate(R.layout.overlay_block, null)
            view.findViewById<TextView>(R.id.overlay_message).text =
                BlocklistManager.blockMessage(this)
            view.findViewById<Button>(R.id.overlay_close).setOnClickListener {
                removeOverlay()
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
            val type = if (android.os.Build.VERSION.SDK_INT >= 26)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            val lp = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            wm.addView(view, lp)
            overlay = view
        } catch (e: Exception) {
            overlay = null
        }
    }

    private fun removeOverlay() {
        overlay?.let {
            try {
                (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it)
            } catch (e: Exception) { }
        }
        overlay = null
    }

    override fun onInterrupt() {
        removeOverlay()
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }
}
