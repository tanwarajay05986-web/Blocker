package com.akay.blocker

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

private const val MP = -1
private const val WC = -2
private val WHITE = 0xFFFFFFFF.toInt()

class MainActivity : ComponentActivity() {

    private lateinit var root: LinearLayout
    private lateinit var spacer: View
    private var insetB = 0
    private var tab = 0
    private var adminAsked = false

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        Store.init(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val ic = WindowCompat.getInsetsController(window, window.decorView)
        ic.isAppearanceLightStatusBars = true
        ic.isAppearanceLightNavigationBars = true

        root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(C.bg)
        spacer = View(this)
        setContentView(root)

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, wi ->
            val i = wi.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            v.setPadding(i.left, i.top, i.right, 0)
            insetB = i.bottom
            val lp = spacer.layoutParams
            if (lp != null) {
                lp.height = insetB
                spacer.layoutParams = lp
            }
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onResume() {
        super.onResume()
        if (adminAsked) {
            adminAsked = false
            Store.set("uninstall", Perm.adminActive(this))
        } else if (Store.on("uninstall") && !Perm.adminActive(this)) {
            Store.set("uninstall", false)
        }
        render()
    }

    private fun render() {
        root.removeAllViews()
        if (!Store.discSeen()) {
            root.addView(disclosure(), LinearLayout.LayoutParams(MP, 0, 1f))
            spacer.setBackgroundColor(C.bg)
        } else {
            val body = when (tab) {
                1 -> Pages.blocklist(this)
                2 -> Pages.settings(this)
                else -> blocking()
            }
            root.addView(body, LinearLayout.LayoutParams(MP, 0, 1f))
            root.addView(nav(), LinearLayout.LayoutParams(MP, WC))
            spacer.setBackgroundColor(C.nav)
        }
        spacer.layoutParams = LinearLayout.LayoutParams(MP, insetB)
        root.addView(spacer)
    }

    // ---------------------------------------------------------------- first screen

    private fun disclosure(): View {
        val page = LinearLayout(this)
        page.orientation = LinearLayout.VERTICAL

        val sv = ScrollView(this)
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.gravity = Gravity.CENTER_HORIZONTAL
        col.setPadding(dp(22), dp(48), dp(22), dp(16))

        col.addView(Glyph(this, G.ACCESS, C.green), LinearLayout.LayoutParams(dp(56), dp(64)))

        val title = TextView(this)
        title.text = "Accessibility Service"
        title.textSize = 30f
        title.gravity = Gravity.CENTER
        title.setTextColor(C.text)
        title.setPadding(0, dp(12), 0, 0)
        col.addView(title, LinearLayout.LayoutParams(MP, WC))

        val use = TextView(this)
        use.text = "Usage"
        use.textSize = 24f
        use.typeface = Typeface.DEFAULT_BOLD
        use.setTextColor(C.text)
        col.addView(use, LinearLayout.LayoutParams(MP, WC).also { it.topMargin = dp(28) })

        val name = getString(R.string.app_name)
        val t1 = TextView(this)
        t1.text = "\u25CF $name uses the accessibility service to block adult content and other unwanted websites and apps on your device. It also lets you add your own sites, words and apps to the blocklist."
        t1.textSize = 18f
        t1.setTextColor(C.text)
        col.addView(t1, LinearLayout.LayoutParams(MP, WC).also { it.topMargin = dp(14) })

        val t2 = TextView(this)
        t2.text = "\u25CF Your privacy stays safe: no data from your phone is collected or sent anywhere. Everything stays on your device."
        t2.textSize = 18f
        t2.setTextColor(C.text)
        col.addView(t2, LinearLayout.LayoutParams(MP, WC).also { it.topMargin = dp(22) })

        sv.addView(col, FrameLayout.LayoutParams(MP, WC))
        page.addView(sv, LinearLayout.LayoutParams(MP, 0, 1f))

        val btns = LinearLayout(this)
        btns.orientation = LinearLayout.HORIZONTAL
        btns.setPadding(dp(14), dp(10), dp(14), dp(14))
        val skip = pill("SKIP", false) {
            Store.setDiscSeen(true)
            render()
        }
        val on = pill("TURN ON", true) {
            Store.setDiscSeen(true)
            Perm.openAccessibility(this)
            render()
        }
        btns.addView(skip, LinearLayout.LayoutParams(0, dp(52), 1f).also { it.rightMargin = dp(14) })
        btns.addView(on, LinearLayout.LayoutParams(0, dp(52), 1.4f))
        page.addView(btns, LinearLayout.LayoutParams(MP, WC))
        return page
    }

    private fun pill(text: String, filled: Boolean, click: () -> Unit): TextView {
        val b = TextView(this)
        b.text = text
        b.textSize = 18f
        b.gravity = Gravity.CENTER
        b.setTextColor(if (filled) WHITE else C.coral)
        b.background = if (filled) shape(C.coral, 26) else shape(C.bg, 26, C.coral, 1)
        b.setOnClickListener { click() }
        return b
    }

    // ---------------------------------------------------------------- blocking screen

    private fun gap(top: Int): LinearLayout.LayoutParams {
        val lp = LinearLayout.LayoutParams(MP, WC)
        lp.topMargin = dp(top)
        return lp
    }

    private fun blocking(): View {
        val sv = ScrollView(this)
        sv.isVerticalScrollBarEnabled = false
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(dp(14), dp(14), dp(14), dp(24))

        val permsOk = Perm.accEnabled(this) && Perm.overlayOk(this) && Perm.batteryOk(this)
        if (!permsOk) col.addView(permCard(), gap(0))

        val browserList = Builtin.BROWSER_NAMES.joinToString(", ") { n -> n.replaceFirstChar { it.uppercase() } }

        col.addView(
            section(
                "Content Blocking", G.GLOBE,
                listOf(
                    toggleRow(G.ADULT, C.coral, "Block adult content", "adult",
                        "Blocks adult websites, adult search words and adult pages in your browser and other apps."),
                    toggleRow(G.SEARCH, C.icon, "Block image & video search", "imgvid",
                        "Blocks the image and video search pages of Google, Bing, Yahoo, Brave, Yandex and similar search engines."),
                    toggleRow(G.YT, C.icon, "Block YouTube Shorts", "ytshorts",
                        "Closes YouTube Shorts as soon as it opens. Normal YouTube videos keep working."),
                    toggleRow(G.IG, C.icon, "Block Instagram Reels", "igreels",
                        "Closes Instagram Reels as soon as it opens. Feed, stories and messages keep working."),
                    toggleRow(G.SNAP, C.icon, "Block Snapchat Snaps & Stories", "snap",
                        "Blocks the Snapchat Stories and Spotlight screens."),
                    toggleRow(G.WA, C.icon, "Block WhatsApp channels", "wachan",
                        "Blocks the channels inside WhatsApp. Chats and calls keep working.")
                )
            ),
            gap(14)
        )

        col.addView(
            section(
                "Advanced Blocking", G.SHIELD,
                listOf(
                    toggleRow(G.TRASH, C.icon, "Uninstall protection", "uninstall",
                        "Asks for device admin permission so the app cannot be uninstalled easily. It also blocks the uninstall and deactivate screens of this app."),
                    toggleRow(G.GLOBE, C.icon, "Block unsupported browsers", "browsers",
                        "Blocks browsers that this app cannot read, so adult content cannot be opened in them.\n\nSupported browsers: $browserList."),
                    toggleRow(G.DLBOX, C.icon, "Block newly installed apps", "newapps",
                        "Blocks every app that was installed in the last 24 hours.")
                )
            ),
            gap(14)
        )

        col.addView(
            section(
                "Customize Blocked Screen", G.MONITOR,
                listOf(
                    editRow(G.CHAT, "Blocked screen message", Store.message(),
                        "This text is shown on the blocked screen. Leave it empty to use the default message.") {
                        ask("Blocked screen message", Store.message(), "Message", InputType.TYPE_CLASS_TEXT) { v ->
                            Store.setMessage(v)
                        }
                    },
                    editRow(G.TIMER, "Blocked screen countdown", "${Store.countdown()} seconds",
                        "How many seconds the blocked screen stays before it closes. Any number from 1 to 60.") {
                        ask("Countdown (seconds)", Store.countdown().toString(), "1 - 60", InputType.TYPE_CLASS_NUMBER) { v ->
                            val n = v.toIntOrNull()
                            if (n == null || n < 1 || n > 60) toast("Enter a number from 1 to 60") else Store.setCountdown(n)
                        }
                    },
                    editRow(G.LINK, "Redirect after closing (any URL)",
                        if (Store.redirect().isBlank()) "Not set" else Store.redirect(),
                        "A web address that opens after the blocked screen closes. Leave it empty to just go back.") {
                        ask("Redirect URL", Store.redirect(), "https://", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI) { v ->
                            Store.setRedirect(v)
                        }
                    }
                )
            ),
            gap(14)
        )

        sv.addView(col, FrameLayout.LayoutParams(MP, WC))
        return sv
    }

    private fun section(title: String, icon: Int, rows: List<View>): View {
        val wrap = LinearLayout(this)
        wrap.orientation = LinearLayout.VERTICAL

        val tag = LinearLayout(this)
        tag.orientation = LinearLayout.HORIZONTAL
        tag.gravity = Gravity.CENTER_VERTICAL
        tag.background = TagShape(C.head)
        tag.setPadding(dp(14), dp(8), dp(34), dp(8))
        tag.addView(Glyph(this, icon, WHITE), LinearLayout.LayoutParams(dp(22), dp(22)))
        val tt = TextView(this)
        tt.text = title
        tt.textSize = 18f
        tt.typeface = Typeface.DEFAULT_BOLD
        tt.setTextColor(WHITE)
        tag.addView(tt, LinearLayout.LayoutParams(WC, WC).also { it.leftMargin = dp(10) })
        wrap.addView(tag, LinearLayout.LayoutParams(WC, WC))

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        val bg = shape(C.card, 14, C.edge)
        val r = dp(14).toFloat()
        bg.cornerRadii = floatArrayOf(0f, 0f, r, r, r, r, r, r)
        card.background = bg
        for ((i, row) in rows.withIndex()) {
            if (i > 0) {
                val d = View(this)
                d.setBackgroundColor(C.divider)
                card.addView(d, LinearLayout.LayoutParams(MP, dp(1)).also { it.leftMargin = dp(14); it.rightMargin = dp(14) })
            }
            card.addView(row, LinearLayout.LayoutParams(MP, WC))
        }
        wrap.addView(card, LinearLayout.LayoutParams(MP, WC))
        return wrap
    }

    // one line: icon, title, small text, something on the right and the info button
    private fun line(icon: Int, ic: Int, title: String, sub: String?, right: View?, info: String): LinearLayout {
        val r = LinearLayout(this)
        r.orientation = LinearLayout.HORIZONTAL
        r.gravity = Gravity.CENTER_VERTICAL
        r.minimumHeight = dp(60)
        r.setPadding(dp(14), dp(8), dp(4), dp(8))
        r.addView(Glyph(this, icon, ic), LinearLayout.LayoutParams(dp(30), dp(30)))

        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        val t = TextView(this)
        t.text = title
        t.textSize = 16f
        t.setTextColor(C.text)
        col.addView(t)
        if (sub != null) {
            val s = TextView(this)
            s.text = sub
            s.textSize = 13f
            s.setTextColor(C.sub)
            s.maxLines = 1
            s.ellipsize = TextUtils.TruncateAt.END
            col.addView(s)
        }
        r.addView(col, LinearLayout.LayoutParams(0, WC, 1f).also { it.leftMargin = dp(14); it.rightMargin = dp(8) })

        if (right != null) r.addView(right)

        val box = FrameLayout(this)
        box.addView(Glyph(this, G.INFO, C.sub), FrameLayout.LayoutParams(dp(24), dp(24), Gravity.CENTER))
        box.setOnClickListener { say(title, info) }
        r.addView(box, LinearLayout.LayoutParams(dp(46), dp(46)))
        return r
    }

    private fun toggleRow(icon: Int, ic: Int, title: String, key: String, info: String): View {
        val t = Toggle(this)
        t.on = Store.on(key)
        t.layoutParams = LinearLayout.LayoutParams(WC, WC).also { it.rightMargin = dp(6) }
        val r = line(icon, ic, title, null, t, info)
        r.setOnClickListener { flip(key, t) }
        return r
    }

    private fun editRow(icon: Int, title: String, sub: String, info: String, edit: () -> Unit): View {
        val pen = Glyph(this, G.PENCIL, C.icon)
        pen.layoutParams = LinearLayout.LayoutParams(dp(24), dp(24)).also { it.rightMargin = dp(6) }
        val r = line(icon, C.icon, title, sub, pen, info)
        r.setOnClickListener { edit() }
        return r
    }

    // turning a toggle on / off
    private fun flip(key: String, t: Toggle) {
        if (!Store.on(key)) {
            if (key == "uninstall") {
                if (Perm.adminActive(this)) {
                    Store.set(key, true)
                    t.on = true
                } else {
                    adminAsked = true
                    try {
                        startActivity(Perm.adminIntent(this))
                    } catch (e: Throwable) {
                        adminAsked = false
                        toast("Cannot open device admin screen")
                    }
                }
                return
            }
            Store.set(key, true)
            t.on = true
            if (!Perm.accEnabled(this)) toast("Turn on the Accessibility permission first")
        } else {
            Gate.ask(this) {
                if (key == "uninstall") Perm.removeAdmin(this)
                Store.set(key, false)
                t.on = false
            }
        }
    }

    // ---------------------------------------------------------------- permissions

    private fun permCard(): View {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.background = shape(C.card, 14, C.coral, 2)

        val head = TextView(this)
        head.text = "Permissions needed"
        head.textSize = 18f
        head.typeface = Typeface.DEFAULT_BOLD
        head.setTextColor(C.text)
        head.setPadding(dp(16), dp(14), dp(16), dp(4))
        card.addView(head, LinearLayout.LayoutParams(MP, WC))

        card.addView(permRow("Accessibility service", "Needed to find and block content", Perm.accEnabled(this)) {
            Perm.openAccessibility(this)
        }, LinearLayout.LayoutParams(MP, WC))
        card.addView(permRow("Display over other apps", "Needed to show the blocked screen", Perm.overlayOk(this)) {
            Perm.openOverlay(this)
        }, LinearLayout.LayoutParams(MP, WC))
        card.addView(permRow("Ignore battery optimization", "Keeps blocking alive in the background", Perm.batteryOk(this)) {
            Perm.openBattery(this)
        }, LinearLayout.LayoutParams(MP, WC))
        return card
    }

    private fun permRow(title: String, sub: String, ok: Boolean, click: () -> Unit): View {
        val r = LinearLayout(this)
        r.orientation = LinearLayout.HORIZONTAL
        r.gravity = Gravity.CENTER_VERTICAL
        r.setPadding(dp(16), dp(10), dp(16), dp(12))

        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        val t = TextView(this)
        t.text = title
        t.textSize = 16f
        t.setTextColor(C.text)
        col.addView(t)
        val s = TextView(this)
        s.text = sub
        s.textSize = 13f
        s.setTextColor(C.sub)
        col.addView(s)
        r.addView(col, LinearLayout.LayoutParams(0, WC, 1f))

        if (ok) {
            r.addView(Glyph(this, G.CHECK, C.green), LinearLayout.LayoutParams(dp(26), dp(26)))
        } else {
            val b = TextView(this)
            b.text = "Allow"
            b.textSize = 14f
            b.typeface = Typeface.DEFAULT_BOLD
            b.setTextColor(WHITE)
            b.gravity = Gravity.CENTER
            b.background = shape(C.coral, 16)
            b.setPadding(dp(16), dp(6), dp(16), dp(6))
            r.addView(b, LinearLayout.LayoutParams(WC, WC))
            r.setOnClickListener { click() }
        }
        return r
    }

    // ---------------------------------------------------------------- bottom bar and dialogs

    private fun nav(): View {
        val bar = LinearLayout(this)
        bar.orientation = LinearLayout.HORIZONTAL
        bar.setBackgroundColor(C.nav)
        bar.setPadding(0, dp(10), 0, dp(10))
        val names = arrayOf("Blocking", "Blocklist", "Settings")
        val icons = intArrayOf(G.BLOCK, G.LIST, G.GEAR)
        for (i in 0..2) {
            val sel = i == tab
            val item = LinearLayout(this)
            item.orientation = LinearLayout.VERTICAL
            item.gravity = Gravity.CENTER_HORIZONTAL

            val pillBox = FrameLayout(this)
            if (sel) pillBox.background = shape(C.coral, 16)
            pillBox.addView(
                Glyph(this, icons[i], if (sel) WHITE else C.icon),
                FrameLayout.LayoutParams(dp(24), dp(24), Gravity.CENTER)
            )
            item.addView(pillBox, LinearLayout.LayoutParams(dp(64), dp(32)))

            val tv = TextView(this)
            tv.text = names[i]
            tv.textSize = 14f
            tv.gravity = Gravity.CENTER
            tv.setTextColor(if (sel) C.coral else C.head)
            if (sel) tv.typeface = Typeface.DEFAULT_BOLD
            item.addView(tv, LinearLayout.LayoutParams(WC, WC).also { it.topMargin = dp(4) })

            item.setOnClickListener {
                tab = i
                render()
            }
            bar.addView(item, LinearLayout.LayoutParams(0, WC, 1f))
        }
        return bar
    }

    private fun say(title: String, msg: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun ask(title: String, cur: String, hint: String, type: Int, save: (String) -> Unit) {
        val et = EditText(this)
        et.inputType = type
        et.hint = hint
        et.setText(cur)
        et.setSelection(et.text.length)
        val box = FrameLayout(this)
        box.setPadding(dp(22), dp(10), dp(22), 0)
        box.addView(et, FrameLayout.LayoutParams(MP, WC))
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(box)
            .setPositiveButton("Save") { _, _ ->
                save(et.text.toString().trim())
                render()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
