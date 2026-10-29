package com.akay.blocker

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.LinearLayout
import android.widget.TextView

class BlockService : AccessibilityService() {

    private val h = Handler(Looper.getMainLooper())
    private var wm: WindowManager? = null
    private var overlay: View? = null
    private var coolUntil = 0L
    private var queued = false
    private var browsers: Set<String> = emptySet()
    private var browsersAt = 0L

    private val scanRun = Runnable {
        queued = false
        scan()
    }

    // typing in these apps is never checked
    private val skipTyped = setOf(
        "com.whatsapp", "com.whatsapp.w4b", "org.telegram.messenger", "com.google.android.apps.messaging",
        "com.android.mms", "com.samsung.android.messaging", "com.truecaller", "com.google.android.gm"
    )

    // view ids that hold the web address of a browser
    private val urlIds = arrayOf(
        "url_bar", "url_field", "urlbar", "toolbar_url", "url_view", "location_bar", "omnibar",
        "address_bar", "addressbar", "search_box_text", "search_edit_text", "edit_url", "url_edit"
    )

    override fun onCreate() {
        super.onCreate()
        Store.init(applicationContext)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Store.init(applicationContext)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        h.removeCallbacksAndMessages(null)
        val v = overlay
        overlay = null
        if (v != null) {
            try {
                win().removeView(v)
            } catch (t: Throwable) {
            }
        }
        super.onDestroy()
    }

    private fun win(): WindowManager {
        val w = wm
        if (w != null) return w
        val n = getSystemService(WINDOW_SERVICE) as WindowManager
        wm = n
        return n
    }

    override fun onAccessibilityEvent(e: AccessibilityEvent?) {
        if (e == null) return
        val pkg = e.packageName?.toString() ?: return
        if (pkg == packageName || overlay != null) return
        when (e.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> typed(e, pkg)
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> later(60, true)
            else -> later(250, false)
        }
    }

    private fun later(ms: Long, force: Boolean) {
        if (queued && !force) return
        h.removeCallbacks(scanRun)
        queued = true
        h.postDelayed(scanRun, ms)
    }

    // ---------------------------------------------------------------- checks

    private fun typed(e: AccessibilityEvent, pkg: String) {
        if (e.isPassword || skipTyped.contains(pkg)) return
        val sb = StringBuilder()
        for (t in e.text) sb.append(t).append(' ')
        val s = sb.toString()
        if (s.length < 3) return
        if (Matcher.hit(s)) block(true)
    }

    private fun scan() {
        if (overlay != null || SystemClock.uptimeMillis() < coolUntil) return
        val root = rootInActiveWindow ?: return
        val pkg = root.packageName?.toString() ?: return
        if (pkg == packageName) return

        if (Store.blockedApps().contains(pkg)) {
            block(true)
            return
        }
        if (Store.on("newapps") && isNewApp(pkg)) {
            block(true)
            return
        }
        val browser = Builtin.BROWSERS.contains(pkg)
        if (!browser && Store.on("browsers") && browserPkgs().contains(pkg)) {
            block(true)
            return
        }
        if (Store.on("uninstall") && guard(root, pkg)) {
            block(true)
            return
        }
        when (pkg) {
            "com.google.android.youtube" ->
                if (Store.on("ytshorts") && ytShorts(root)) {
                    block(false)
                    return
                }
            "com.instagram.android" ->
                if (Store.on("igreels") && igReels(root)) {
                    block(false)
                    return
                }
            "com.snapchat.android" ->
                if (Store.on("snap") && snapScreen(root)) {
                    block(false)
                    return
                }
            "com.whatsapp", "com.whatsapp.w4b" ->
                if (Store.on("wachan") && waChannels(root)) {
                    block(false)
                    return
                }
        }
        if (browser) scanBrowser(root)
    }

    private fun scanBrowser(root: AccessibilityNodeInfo) {
        val url = StringBuilder()
        val fields = StringBuilder()
        val page = StringBuilder()
        find(root) { n ->
            val tx = n.text?.toString()
            if (tx != null && tx.isNotEmpty()) {
                val id = n.viewIdResourceName
                val isUrl = id != null && urlIds.any { id.contains(it) }
                if (isUrl) {
                    url.append(tx).append(' ')
                } else if (n.isEditable && !n.isPassword) {
                    fields.append(tx).append(' ')
                } else if (page.length < 20000) {
                    page.append(tx).append(' ')
                }
            }
            false
        }
        val u = url.toString()
        if (u.isNotBlank() && Matcher.isWhiteUrl(u)) return
        if (Matcher.hit(u + " " + fields.toString())) {
            block(true)
            return
        }
        if (Store.on("imgvid") && Matcher.mediaHit(u)) {
            block(true)
            return
        }
        if (Matcher.pageHit(page.toString())) block(true)
    }

    // settings and uninstall screens of this app
    private fun guard(root: AccessibilityNodeInfo, pkg: String): Boolean {
        if (!(pkg.contains("settings") || pkg.contains("packageinstaller") || pkg.contains("permissioncontroller"))) return false
        val me = getString(R.string.app_name).lowercase()
        val sb = StringBuilder()
        find(root) { n ->
            n.text?.let { sb.append(it.toString().lowercase()).append('|') }
            n.contentDescription?.let { sb.append(it.toString().lowercase()).append('|') }
            false
        }
        val t = sb.toString()
        if (!t.contains(me)) return false
        for (k in arrayOf("uninstall", "deactivate", "force stop", "clear data", "clear storage", "turn off")) {
            if (t.contains(k)) return true
        }
        return false
    }

    private fun ytShorts(root: AccessibilityNodeInfo): Boolean =
        find(root) { n ->
            val id = n.viewIdResourceName
            if (id != null && (id.contains("reel_recycler") || id.contains("reel_player") ||
                    id.contains("reel_watch_fragment") || id.contains("shorts_player"))
            ) {
                true
            } else if (n.isSelected) {
                val t = (n.contentDescription ?: n.text ?: "").toString().lowercase()
                t == "shorts" || t.startsWith("shorts,")
            } else {
                false
            }
        }

    private fun igReels(root: AccessibilityNodeInfo): Boolean =
        find(root) { n ->
            val id = n.viewIdResourceName
            if (id != null && (id.contains("clips_viewer") || id.contains("clips_swipe_refresh") ||
                    id.contains("root_clips_layout"))
            ) {
                true
            } else if (n.isSelected) {
                val t = (n.contentDescription ?: n.text ?: "").toString().lowercase()
                (id != null && id.contains("clips_tab")) || t == "reels" || t.startsWith("reels,")
            } else {
                false
            }
        }

    private fun snapScreen(root: AccessibilityNodeInfo): Boolean =
        find(root) { n ->
            val id = n.viewIdResourceName
            if (id != null && id.contains("spotlight")) {
                true
            } else if (n.isSelected) {
                val t = (n.contentDescription ?: n.text ?: "").toString().lowercase()
                t.contains("spotlight") || t.contains("stories")
            } else {
                false
            }
        }

    private fun waChannels(root: AccessibilityNodeInfo): Boolean =
        find(root) { n ->
            val id = n.viewIdResourceName
            if (id != null && id.contains("newsletter")) {
                true
            } else {
                val t = n.text?.toString()?.lowercase()
                t == "find channels" || t == "explore channels"
            }
        }

    private fun hasId(root: AccessibilityNodeInfo, vararg parts: String): Boolean =
        find(root) { n ->
            val id = n.viewIdResourceName
            id != null && parts.any { id.contains(it) }
        }

    private fun walk(n: AccessibilityNodeInfo?, d: Int, cnt: IntArray, f: (AccessibilityNodeInfo) -> Boolean): Boolean {
        if (n == null || d > 45 || cnt[0] > 3000) return false
        cnt[0]++
        if (f(n)) return true
        for (i in 0 until n.childCount) {
            if (walk(n.getChild(i), d + 1, cnt, f)) return true
        }
        return false
    }

    private fun find(root: AccessibilityNodeInfo, f: (AccessibilityNodeInfo) -> Boolean): Boolean =
        walk(root, 0, IntArray(1), f)

    // apps installed in the last 24 hours
    private fun isNewApp(pkg: String): Boolean {
        try {
            val pm = packageManager
            val ai = pm.getApplicationInfo(pkg, 0)
            if (ai.flags and ApplicationInfo.FLAG_SYSTEM != 0) return false
            if (pm.getLaunchIntentForPackage(pkg) == null) return false
            val pi = pm.getPackageInfo(pkg, 0)
            return System.currentTimeMillis() - pi.firstInstallTime < 24L * 60L * 60L * 1000L
        } catch (t: Throwable) {
            return false
        }
    }

    // every browser app installed on the phone
    private fun browserPkgs(): Set<String> {
        val now = SystemClock.uptimeMillis()
        if (browsers.isNotEmpty() && now - browsersAt < 60000L) return browsers
        val s = HashSet<String>()
        try {
            val pm = packageManager
            val v = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.example.com"))
            v.addCategory(Intent.CATEGORY_BROWSABLE)
            for (r in pm.queryIntentActivities(v, PackageManager.MATCH_ALL)) s.add(r.activityInfo.packageName)
            val m = Intent(Intent.ACTION_MAIN)
            m.addCategory(Intent.CATEGORY_APP_BROWSER)
            for (r in pm.queryIntentActivities(m, 0)) s.add(r.activityInfo.packageName)
        } catch (t: Throwable) {
        }
        browsers = s
        browsersAt = now
        return s
    }

    // ---------------------------------------------------------------- blocked screen

    private fun block(home: Boolean) {
        if (overlay != null || SystemClock.uptimeMillis() < coolUntil) return
        val secs = Store.countdown()
        val white = 0xFFFFFFFF.toInt()

        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.gravity = Gravity.CENTER
        col.setBackgroundColor(C.coral)
        col.setPadding(dp(28), dp(28), dp(28), dp(28))
        col.isClickable = true
        col.isFocusableInTouchMode = true

        col.addView(Glyph(this, G.BLOCK, white), LinearLayout.LayoutParams(dp(84), dp(84)))

        val msg = TextView(this)
        msg.text = Store.message()
        msg.textSize = 24f
        msg.typeface = Typeface.DEFAULT_BOLD
        msg.setTextColor(white)
        msg.gravity = Gravity.CENTER
        msg.setPadding(0, dp(24), 0, dp(12))
        col.addView(msg, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        val cd = TextView(this)
        cd.text = "Closing in $secs"
        cd.textSize = 16f
        cd.setTextColor(0xCCFFFFFF.toInt())
        cd.gravity = Gravity.CENTER
        col.addView(cd, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        val type = if (Perm.overlayOk(this)) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        }
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.OPAQUE
        )
        lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        try {
            win().addView(col, lp)
        } catch (t: Throwable) {
            coolUntil = SystemClock.uptimeMillis() + 1200L
            act(home)
            return
        }
        overlay = col
        col.requestFocus()
        col.setOnKeyListener { _, _, _ -> true }

        var left = secs
        val tick = object : Runnable {
            override fun run() {
                left--
                if (left <= 0) {
                    done(home)
                } else {
                    cd.text = "Closing in $left"
                    h.postDelayed(this, 1000L)
                }
            }
        }
        h.postDelayed(tick, 1000L)
    }

    private fun done(home: Boolean) {
        val v = overlay
        overlay = null
        if (v != null) {
            try {
                win().removeView(v)
            } catch (t: Throwable) {
            }
        }
        coolUntil = SystemClock.uptimeMillis() + 1200L
        act(home)
        h.postDelayed({ openRedirect() }, 400L)
    }

    private fun act(home: Boolean) {
        performGlobalAction(if (home) GLOBAL_ACTION_HOME else GLOBAL_ACTION_BACK)
    }

    private fun openRedirect() {
        var u = Store.redirect().trim()
        if (u.isEmpty()) return
        if (!u.contains("://")) u = "https://$u"
        try {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(u))
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
        } catch (t: Throwable) {
        }
    }
}
