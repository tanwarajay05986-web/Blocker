package com.blocker.app

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 36, 36, 36)
        }
        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)

        if (BlocklistManager.password(this).isNotEmpty()) askPassword() else buildUi()
    }

    override fun onResume() {
        super.onResume()
        if (::root.isInitialized && root.childCount > 0) buildUi()
    }

    // ---------- Password gate ----------
    private fun askPassword() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "Password"
        }
        AlertDialog.Builder(this)
            .setTitle("🔒 Blocker locked")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Open") { _, _ ->
                if (input.text.toString() == BlocklistManager.password(this)) buildUi()
                else { toast("Galat password"); finish() }
            }
            .setNegativeButton("Exit") { _, _ -> finish() }
            .show()
    }

    // ---------- Poora UI ----------
    private fun buildUi() {
        root.removeAllViews()

        root.addView(TextView(this).apply {
            text = "🛡 Blocker"
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
        })

        val accOn = isMyAccessibilityEnabled()
        val ovOn = Settings.canDrawOverlays(this)
        root.addView(TextView(this).apply {
            text = buildString {
                append("Accessibility service: ").append(if (accOn) "✅ ON" else "❌ OFF")
                append('\n')
                append("Popup permission: ").append(if (ovOn) "✅ ON" else "❌ OFF")
            }
            textSize = 14f
            setPadding(0, 14, 0, 14)
        })

        if (!accOn) root.addView(bigButton("Step 1: Accessibility ON karo") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            toast("Installed services me 'Blocker' dhundo → ON karo")
        })
        if (!ovOn) root.addView(bigButton("Step 2: Popup permission do") {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        })

        val sw = Switch(this).apply {
            text = "Protection ON"
            textSize = 16f
            isChecked = BlocklistManager.isProtectionOn(this@MainActivity)
            setPadding(0, 20, 0, 8)
        }
        sw.setOnCheckedChangeListener { _, on ->
            if (!on) {
                val saved = BlocklistManager.password(this)
                if (saved.isNotEmpty()) {
                    val input = EditText(this).apply {
                        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        hint = "Password"
                    }
                    AlertDialog.Builder(this)
                        .setTitle("Password daalo (OFF karne ke liye)")
                        .setView(input)
                        .setCancelable(false)
                        .setPositiveButton("OK") { _, _ ->
                            if (input.text.toString() == saved) {
                                BlocklistManager.setProtection(this, false)
                                toast("Protection OFF ho gaya")
                            } else {
                                toast("Galat password")
                                sw.isChecked = true
                            }
                        }
                        .setNegativeButton("Cancel") { _, _ -> sw.isChecked = true }
                        .show()
                } else {
                    BlocklistManager.setProtection(this, false)
                    toast("Protection OFF ho gaya")
                }
            } else {
                BlocklistManager.setProtection(this, true)
                toast("Protection ON ✅")
            }
        }
        root.addView(sw)

        // Blocked keywords & websites
        addSection(
            "🔑 Blocked keywords & websites",
            BlocklistManager.blockedKeywords(this)
                .plus(BlocklistManager.blockedSites(this))
                .sorted()
        ) { item ->
            if (item.contains('.')) BlocklistManager.removeSite(this, item)
            else BlocklistManager.removeKeyword(this, item)
            buildUi()
        }
        addInputRow("Keyword ya website add karo") { text ->
            val v = text.trim().lowercase()
            if (v.isNotEmpty()) {
                if (v.contains('.')) BlocklistManager.addSite(this, v)
                else BlocklistManager.addKeyword(this, v)
                buildUi()
            }
        }

        // Blocked apps
        addSection(
            "📱 Blocked apps",
            BlocklistManager.blockedApps(this).map { appLabel(it) }.sorted()
        ) { lbl ->
            val pkg = BlocklistManager.blockedApps(this).firstOrNull { appLabel(it) == lbl }
            if (pkg != null) { BlocklistManager.removeApp(this, pkg); buildUi() }
        }
        root.addView(bigButton("＋ App block karo") { showAppPicker() })

        // Whitelist
        addSection("🕊 Whitelist (kabhi block nahi hogi)", BlocklistManager.whitelist(this).toList().sorted()) { item ->
            BlocklistManager.removeWhitelist(this, item)
            buildUi()
        }
        addInputRow("Safe site add karo") { text ->
            BlocklistManager.addWhitelist(this, text.trim().lowercase())
            buildUi()
        }

        // 🔒 Privacy dashboard
        root.addView(bigButton("🔒 Privacy — apps & permissions") {
            startActivity(Intent(this, PrivacyActivity::class.java))
        })

        // Settings
        sectionTitle("⚙️ Settings")

        val msg = EditText(this).apply { setText(BlocklistManager.blockMessage(this@MainActivity)) }
        root.addView(label("Block popup message:"))
        root.addView(msg)
        root.addView(bigButton("Message save") {
            BlocklistManager.setBlockMessage(this, msg.text.toString())
            toast("Saved ✅")
        })

        val pass = EditText(this).apply {
            hint = "Naya password (khali = no lock)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        root.addView(label("Password lock:"))
        root.addView(pass)
        root.addView(bigButton("Password save") {
            BlocklistManager.setPassword(this, pass.text.toString())
            toast("Saved ✅")
        })

        root.addView(TextView(this).apply {
            text = "\n💡 Websites domain se add karo, jaise: badsite.com\n💡 Keywords me koi word likho, jaise: xxx"
            textSize = 13f
            setPadding(0, 20, 0, 0)
        })
    }

    // ---------- Helpers ----------
    private fun label(t: String) = TextView(this).apply {
        text = t; textSize = 14f; setPadding(0, 14, 0, 4)
    }

    private fun bigButton(t: String, onClick: () -> Unit) = Button(this).apply {
        text = t
        setOnClickListener { onClick() }
    }

    private fun sectionTitle(t: String) {
        root.addView(TextView(this).apply {
            text = t; textSize = 18f; typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 26, 0, 10)
        })
    }

    private fun addSection(title: String, items: List<String>, onDelete: (String) -> Unit) {
        sectionTitle(title)
        if (items.isEmpty()) {
            root.addView(TextView(this).apply { text = "(khaali hai)" })
            return
        }
        for (item in items) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            val tv = TextView(this).apply {
                text = item; textSize = 15f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val del = Button(this).apply {
                text = "✕"
                setOnClickListener { onDelete(item) }
            }
            row.addView(tv); row.addView(del)
            root.addView(row)
        }
    }

    private fun addInputRow(hint: String, onAdd: (String) -> Unit) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val input = EditText(this).apply {
            this.hint = hint
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val add = Button(this).apply {
            text = "Add"
            setOnClickListener {
                val t = input.text.toString().trim()
                if (t.isNotEmpty()) onAdd(t)
            }
        }
        row.addView(input); row.addView(add)
        root.addView(row)
    }

    private fun showAppPicker() {
        val pm = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.packageName != packageName && pm.getLaunchIntentForPackage(it.packageName) != null }
            .sortedBy { it.loadLabel(pm).toString().lowercase() }
        val labels = apps.map { it.loadLabel(pm).toString() }
        AlertDialog.Builder(this)
            .setTitle("Kaunsa app block karein?")
            .setItems(labels.toTypedArray()) { _, which ->
                BlocklistManager.addApp(this, apps[which].packageName)
                toast("${labels[which]} blocked ✅")
                buildUi()
            }
            .show()
    }

    private fun appLabel(pkg: String): String {
        return try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
        } catch (e: Exception) { pkg }
    }

    private fun isMyAccessibilityEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.split(':').any {
            it.equals("com.blocker.app/com.blocker.app.BlockerAccessibilityService", ignoreCase = true)
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
