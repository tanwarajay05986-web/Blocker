package com.akay.blocker

object Matcher {
    private val hostRx = Regex("[a-z0-9][a-z0-9-]*(?:\\.[a-z0-9-]+)+")
    private val nonAlnum = Regex("[^a-z0-9]+")
    private val domains: Set<String> = HashSet(Builtin.DOMAINS.toList())
    private val wordKeys: List<String> = Builtin.WORDS.map { " $it " }
    private val strongKeys: List<String> = Builtin.STRONG.map { " $it " }

    private fun words(raw: String): String = " " + raw.replace(nonAlnum, " ").trim() + " "

    private fun hosts(raw: String): List<String> =
        hostRx.findAll(raw).map { it.value.removePrefix("www.") }.toList()

    private fun domainHit(host: String, set: Set<String>): Boolean {
        var h = host
        while (true) {
            if (set.contains(h)) return true
            val i = h.indexOf('.')
            if (i < 0) return false
            h = h.substring(i + 1)
        }
    }

    // a list entry typed by the user: a web address or a keyword
    private fun norm(e: String): String {
        var s = e.lowercase().trim()
        s = s.removePrefix("https://").removePrefix("http://").removePrefix("www.")
        if (s.contains('.') && !s.contains(' ')) {
            val i = s.indexOf('/')
            if (i > 0) s = s.substring(0, i)
        }
        return s
    }

    private fun isDomainEntry(s: String): Boolean = s.contains('.') && !s.contains(' ')

    private fun entryHit(entries: Set<String>, w: String, hs: List<String>): Boolean {
        for (e in entries) {
            val s = norm(e)
            if (s.isEmpty()) continue
            if (isDomainEntry(s)) {
                for (h in hs) if (h == s || h.endsWith(".$s")) return true
            } else {
                val k = " " + words(s).trim() + " "
                if (k.length > 2 && w.contains(k)) return true
            }
        }
        return false
    }

    fun isWhiteUrl(text: String): Boolean {
        val raw = text.lowercase()
        val white = Store.whiteWords()
        if (white.isEmpty()) return false
        return entryHit(white, words(raw), hosts(raw))
    }

    // a web address, a search text or a typed word
    fun hit(text: String): Boolean {
        val raw = text.lowercase()
        if (raw.length < 3) return false
        val w = words(raw)
        val hs = hosts(raw)
        val white = Store.whiteWords()
        if (white.isNotEmpty() && entryHit(white, w, hs)) return false
        if (Store.on("adult")) {
            for (h in hs) if (domainHit(h, domains)) return true
            for (s in Builtin.SUBSTR) if (raw.contains(s)) return true
            for (k in wordKeys) if (w.contains(k)) return true
        }
        val mine = Store.userWords()
        if (mine.isNotEmpty() && entryHit(mine, w, hs)) return true
        return false
    }

    // image and video search pages
    fun mediaHit(text: String): Boolean {
        val t = text.lowercase()
        for (m in Builtin.MEDIA) if (t.contains(m)) return true
        return false
    }

    // the text of a whole web page
    fun pageHit(text: String): Boolean {
        val raw = text.lowercase()
        val w = words(raw)
        for (e in Store.userWords()) {
            val s = norm(e)
            if (s.isEmpty() || isDomainEntry(s)) continue
            val k = " " + words(s).trim() + " "
            if (k.length > 2 && w.contains(k)) return true
        }
        if (!Store.on("adult")) return false
        var count = 0
        for (k in strongKeys) {
            var i = w.indexOf(k)
            while (i >= 0) {
                count++
                if (count >= 4) return true
                i = w.indexOf(k, i + 1)
            }
        }
        return false
    }
}
