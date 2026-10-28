package com.akay.blocker

import android.content.Context
import android.content.SharedPreferences

object Config {
    // The special code is NOT asked while testing. Change this to true when testing is finished.
    const val CODE_ENFORCED = false
}

object Store {
    private var sp: SharedPreferences? = null

    const val DEFAULT_MSG = "This page has been blocked \uD83D\uDD12 \uD83D\uDC80 \uD83D\uDD25"

    fun init(c: Context) {
        if (sp == null) sp = c.applicationContext.getSharedPreferences("blocker", Context.MODE_PRIVATE)
    }

    private fun p(): SharedPreferences = sp!!

    // toggles: adult, imgvid, ytshorts, igreels, snap, wachan, uninstall, browsers, newapps
    fun on(key: String): Boolean = p().getBoolean("t_$key", key == "adult")

    fun set(key: String, v: Boolean) {
        p().edit().putBoolean("t_$key", v).apply()
    }

    fun message(): String {
        val m = p().getString("msg", null)
        return if (m.isNullOrBlank()) DEFAULT_MSG else m
    }

    fun setMessage(v: String) {
        p().edit().putString("msg", v).apply()
    }

    fun countdown(): Int = p().getInt("count", 3).coerceIn(1, 60)

    fun setCountdown(v: Int) {
        p().edit().putInt("count", v.coerceIn(1, 60)).apply()
    }

    fun redirect(): String = p().getString("redirect", "") ?: ""

    fun setRedirect(v: String) {
        p().edit().putString("redirect", v).apply()
    }

    fun code(): String = p().getString("code", "") ?: ""

    fun setCode(v: String) {
        p().edit().putString("code", v).apply()
    }

    fun discSeen(): Boolean = p().getBoolean("disc_seen", false)

    fun setDiscSeen(v: Boolean) {
        p().edit().putBoolean("disc_seen", v).apply()
    }

    private fun getSet(k: String): MutableSet<String> =
        HashSet(p().getStringSet(k, emptySet()) ?: emptySet())

    private fun putSet(k: String, s: Set<String>) {
        p().edit().putStringSet(k, HashSet(s)).apply()
    }

    fun blockedApps(): Set<String> = getSet("apps")

    fun addBlockedApp(pk: String) {
        val s = getSet("apps")
        s.add(pk)
        putSet("apps", s)
    }

    fun removeBlockedApp(pk: String) {
        val s = getSet("apps")
        s.remove(pk)
        putSet("apps", s)
    }

    fun userWords(): Set<String> = getSet("words")

    fun addWord(w: String) {
        val s = getSet("words")
        s.add(w)
        putSet("words", s)
    }

    fun removeWord(w: String) {
        val s = getSet("words")
        s.remove(w)
        putSet("words", s)
    }

    fun whiteWords(): Set<String> = getSet("white")

    fun addWhite(w: String) {
        val s = getSet("white")
        s.add(w)
        putSet("white", s)
    }

    fun removeWhite(w: String) {
        val s = getSet("white")
        s.remove(w)
        putSet("white", s)
    }
}
