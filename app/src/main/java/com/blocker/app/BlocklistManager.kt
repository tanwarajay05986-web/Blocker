package com.blocker.app

import android.content.Context
import java.util.regex.Pattern

object BlocklistManager {

    private const val PREF = "blocker_prefs"

    private fun prefs(c: Context) = c.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    // ---------- Lists ----------
    fun blockedKeywords(c: Context): Set<String> =
        prefs(c).getStringSet("keywords", null) ?: defaultKeywords()

    fun blockedSites(c: Context): Set<String> =
        prefs(c).getStringSet("sites", null) ?: defaultSites()

    fun blockedApps(c: Context): Set<String> =
        prefs(c).getStringSet("apps", emptySet()) ?: emptySet()

    fun whitelist(c: Context): Set<String> =
        prefs(c).getStringSet("whitelist", emptySet()) ?: emptySet()

    fun addKeyword(c: Context, v: String) = addToSet(c, "keywords", v)
    fun addSite(c: Context, v: String) = addToSet(c, "sites", v)
    fun addApp(c: Context, v: String) = addToSet(c, "apps", v)
    fun addWhitelist(c: Context, v: String) = addToSet(c, "whitelist", v)
    fun removeKeyword(c: Context, v: String) = removeFromSet(c, "keywords", v)
    fun removeSite(c: Context, v: String) = removeFromSet(c, "sites", v)
    fun removeApp(c: Context, v: String) = removeFromSet(c, "apps", v)
    fun removeWhitelist(c: Context, v: String) = removeFromSet(c, "whitelist", v)

    private fun addToSet(c: Context, key: String, v: String) {
        val cur = HashSet(currentSet(c, key))
        cur.add(v.trim().lowercase())
        prefs(c).edit().putStringSet(key, cur).apply()
    }

    private fun removeFromSet(c: Context, key: String, v: String) {
        val cur = HashSet(currentSet(c, key))
        cur.remove(v)
        prefs(c).edit().putStringSet(key, cur).apply()
    }

    private fun currentSet(c: Context, key: String): Set<String> = when (key) {
        "keywords" -> blockedKeywords(c)
        "sites" -> blockedSites(c)
        "apps" -> blockedApps(c)
        else -> whitelist(c)
    }

    // ---------- Settings ----------
    fun isProtectionOn(c: Context) = prefs(c).getBoolean("protection", true)

    fun setProtection(c: Context, on: Boolean) =
        prefs(c).edit().putBoolean("protection", on).apply()

    fun blockMessage(c: Context) = prefs(c).getString("msg", null) ?: "This page is blocked."

    fun setBlockMessage(c: Context, m: String) =
        prefs(c).edit().putString("msg", m).apply()

    fun password(c: Context) = prefs(c).getString("pass", "") ?: ""

    fun setPassword(c: Context, p: String) =
        prefs(c).edit().putString("pass", p).apply()

    // ---------- Matching (asli blocking yahan hoti hai) ----------
    fun isTextBlocked(c: Context, text: String): Boolean {
        if (!isProtectionOn(c)) return false
        val t = text.lowercase()
        if (t.isBlank()) return false

        if (whitelist(c).any { it.isNotBlank() && t.contains(it) }) return false

        for (k in blockedKeywords(c)) {
            if (k.isBlank()) continue
            val p = Pattern.compile("(?i)\\b" + Pattern.quote(k) + "\\b")
            if (p.matcher(t).find()) return true
        }

        return blockedSites(c).any { it.isNotBlank() && t.contains(it) }
    }

    // ---------- Built-in lists ----------
    private fun defaultKeywords(): Set<String> = setOf(
        "porn", "porno", "xxx", "sex", "nude", "naked", "nsfw", "hentai", "erotic",
        "pornhub", "xvideos", "xnxx", "redtube", "chaturbate", "onlyfans", "xhamster"
    )

    private fun defaultSites(): Set<String> = setOf(
        "pornhub.com", "xvideos.com", "xnxx.com", "xhamster.com", "redtube.com",
        "youporn.com", "tube8.com", "spankbang.com", "chaturbate.com", "onlyfans.com",
        "beeg.com", "nudevista.com", "porntube.com", "sex.com", "tnaflix.com",
        "drtuber.com", "motherless.com", "keezmovies.com", "slutload.com", "pornhd.com",
        "eporner.com", "youjizz.com", "hclips.com", "pornhat.com", "brazzers.com",
        "naughtyamerica.com", "bangbros.com", "realitykings.com",
        "adultfriendfinder.com", "livejasmin.com", "stripchat.com", "cam4.com"
    )
}
