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
