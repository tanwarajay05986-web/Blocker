package com.blocker.app

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PrivacyActivity : AppCompatActivity() {

    private val interesting = mapOf(
        "android.permission.CAMERA" to "Camera",
        "android.permission.RECORD_AUDIO" to "Mic",
        "android.permission.ACCESS_FINE_LOCATION" to "Location",
        "android.permission.ACCESS_COARSE_LOCATION" to "Location",
        "android.permission.READ_CONTACTS" to "Contacts",
        "android.permission.READ_SMS" to "SMS",
        "android.permission.CALL_PHONE" to "Phone",
        "android.permission.READ_EXTERNAL_STORAGE" to "Storage",
        "android.permission.READ_MEDIA_IMAGES" to "Photos"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 48, 36, 36)
        }
        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)

        root.addView(TextView(this).apply {
            text = "🔒 Privacy dashboard"
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
        })
        root.addView(TextView(this).apply {
            text = "Kaunsi app ke paas Camera/Mic/Location hai — dekho aur OFF dabao"
            textSize = 14f
            setPadding(0, 8, 0, 16)
        })

        root.addView(Button(this).apply {
            text = "🔴 PRIVACY OFF — Camera / Mic / Location control"
            setOnClickListener {
                val i = if (Build.VERSION.SDK_INT >= 31)
                    Intent(Settings.ACTION_PRIVACY_SETTINGS)
                else Intent(Settings.ACTION_SECURITY_SETTINGS)
                startActivity(i)
            }
        })
        root.addView(Button(this).apply {
            text = "📍 Location settings kholo"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        })

        val pm = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.packageName != packageName && pm.getLaunchIntentForPackage(it.packageName) != null }
            .sortedBy { it.loadLabel(pm).toString().lowercase() }

        for (app in apps) {
            val pkg = app.packageName
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 16, 0, 16)
            }
            val col = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            col.addView(TextView(this).apply {
                text = app.loadLabel(pm).toString()
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
            })
            val granted = grantedPerms(pkg, pm)
            col.addView(TextView(this).apply {
                text = if (granted.isEmpty()) "Koi special permission nahi"
                else granted.joinToString(" · ")
                textSize = 12f
            })
            row.addView(col)
            row.addView(Button(this).apply {
                text = "OFF"
                setOnClickListener {
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:$pkg")))
                }
            })
            root.addView(row)
            root.addView(View(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                setBackgroundColor(0x33E53935)
            })
        }

        root.addView(TextView(this).apply {
            text = "\n⚠️ Android ki security ke karan permissions wahan 1 tap se band karni hongi — OFF button aapko seedhi sahi screen le jata hai."
            textSize = 12f
            setPadding(0, 20, 0, 0)
        })
    }

    private fun grantedPerms(pkg: String, pm: PackageManager): List<String> {
        return try {
            val pi = pm.getPackageInfo(pkg, PackageManager.GET_PERMISSIONS)
            val requested = pi.requestedPermissions ?: return emptyList()
            val found = mutableListOf<String>()
            for (p in requested) {
                if (pm.checkPermission(p, pkg) == PackageManager.PERMISSION_GRANTED) {
                    interesting[p]?.let { if (it !in found) found.add(it) }
                }
            }
            found
        } catch (e: Exception) { emptyList() }
    }
}
