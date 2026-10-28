package com.akay.blocker

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

object Perm {

    fun accEnabled(c: Context): Boolean {
        val s = Settings.Secure.getString(c.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?: return false
        val cn = ComponentName(c, BlockService::class.java)
        val full = cn.flattenToString()
        val short = cn.flattenToShortString()
        for (part in s.split(':')) {
            if (part.equals(full, true) || part.equals(short, true)) return true
        }
        return false
    }

    fun overlayOk(c: Context): Boolean = Settings.canDrawOverlays(c)

    fun batteryOk(c: Context): Boolean {
        val pm = c.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(c.packageName)
    }

    private fun adminComp(c: Context) = ComponentName(c, AdminReceiver::class.java)

    fun adminActive(c: Context): Boolean {
        val dpm = c.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isAdminActive(adminComp(c))
    }

    fun removeAdmin(c: Context) {
        try {
            val dpm = c.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            dpm.removeActiveAdmin(adminComp(c))
        } catch (e: Throwable) {
        }
    }

    fun adminIntent(c: Context): Intent {
        val i = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComp(c))
        i.putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            "Needed so that the app cannot be uninstalled easily."
        )
        return i
    }

    fun openAccessibility(a: Activity) {
        try {
            a.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        } catch (e: Throwable) {
        }
    }

    fun openOverlay(a: Activity) {
        try {
            a.startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + a.packageName))
            )
        } catch (e: Throwable) {
            try {
                a.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
            } catch (e2: Throwable) {
            }
        }
    }

    fun openBattery(a: Activity) {
        try {
            a.startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + a.packageName))
            )
        } catch (e: Throwable) {
            try {
                a.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            } catch (e2: Throwable) {
            }
        }
    }
}
