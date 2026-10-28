package com.akay.blocker

// Built-in lists. They are used by the blocker only and are never shown in the app.
object Builtin {

    val DOMAINS = arrayOf(
        "pornhub.com", "xvideos.com", "xnxx.com", "xhamster.com", "redtube.com", "youporn.com",
        "tube8.com", "spankbang.com", "eporner.com", "beeg.com", "porn.com", "tnaflix.com",
        "hclips.com", "txxx.com", "drtuber.com", "motherless.com", "brazzers.com", "bangbros.com",
        "realitykings.com", "naughtyamerica.com", "mofos.com", "onlyfans.com", "fansly.com",
        "chaturbate.com", "stripchat.com", "bongacams.com", "cam4.com", "livejasmin.com",
        "myfreecams.com", "camsoda.com", "rule34.xxx", "e-hentai.org", "nhentai.net",
        "hentaihaven.xxx", "fakku.net", "hanime.tv", "hqporner.com", "porntrex.com",
        "thumbzilla.com", "pornone.com", "upornia.com", "xtube.com", "sunporno.com", "fapello.com",
        "erome.com", "thothub.to", "sex.com", "xvideos2.com", "xnxx2.com", "pornpics.com",
        "pornzog.com", "fuq.com", "nuvid.com", "vporn.com", "analdin.com", "4tube.com",
        "gotporn.com", "daftsex.com", "ixxx.com", "xxxvideos.com", "desixxxtube.com",
        "indianpornvideos.com", "mmsbee.com", "kamababa.desi", "savitabhabhi.com",
        "bollywoodporn.com", "masalaseduction.com", "desisexclips.com", "fsiblog.com",
        "hotmovies.com", "adultfriendfinder.com", "ashleymadison.com", "literotica.com",
        "redgifs.com", "spankwire.com", "keezmovies.com", "slutload.com", "extremetube.com",
        "pornmd.com", "freeones.com", "tubegalore.com", "youjizz.com", "porndig.com",
        "pornhat.com", "pornktube.com", "xvideos.red", "xnxx.tv", "xhamster.desi"
    )

    // found anywhere inside the text or the web address
    val SUBSTR = arrayOf(
        "porn", "xxx", "xvideo", "xnxx", "xhamster", "hentai", "onlyfans", "redtube", "youporn",
        "spankbang", "brazzers", "camgirl", "sexvideo", "sexvid", "nudevid", "sexchat", "sexcam"
    )

    // must be a whole word (or phrase)
    val WORDS = arrayOf(
        "sex", "sexy", "nude", "nudes", "naked", "nsfw", "milf", "boobs", "boob", "tits", "pussy",
        "blowjob", "handjob", "anal", "orgy", "erotic", "erotica", "fetish", "bdsm", "stripper",
        "striptease", "horny", "creampie", "gangbang", "threesome", "masturbate", "masturbation",
        "cumshot", "hardcore", "softcore", "sex video", "sex videos", "sex story", "sex stories",
        "adult video", "adult videos", "blue film", "bf video", "chudai", "chudayi", "chut", "lund",
        "gaand", "gand", "randi", "nangi", "nanga", "chodna", "bhabhi sex", "desi sex", "desi mms",
        "mms video", "mms leaked", "sexy video", "sexy bhabhi", "hot bhabhi", "aunty sex",
        "xxx video"
    )

    // page text: if these show up many times, the page is blocked
    val STRONG = arrayOf(
        "porn", "pornhub", "xxx", "hentai", "xvideos", "pussy", "blowjob", "milf", "anal", "creampie",
        "gangbang", "cumshot", "handjob", "nsfw", "onlyfans", "orgy", "bdsm", "fetish", "nude",
        "nudes", "naked", "boobs", "horny", "erotic", "sex", "sexy", "chudai", "randi"
    )

    // image and video search pages
    val MEDIA = arrayOf(
        "tbm=isch", "udm=2", "tbm=vid", "udm=7", "/imghp", "images.google", "google.com/images",
        "bing.com/images", "bing.com/videos", "iax=images", "iax=videos", "ia=images", "ia=videos",
        "yandex.com/images", "yandex.ru/images", "images.search.yahoo", "video.search.yahoo",
        "search.brave.com/images", "search.brave.com/videos"
    )

    val BROWSERS = setOf(
        "com.android.chrome", "com.chrome.beta", "com.chrome.dev", "com.chrome.canary",
        "com.brave.browser", "com.brave.browser_beta", "com.brave.browser_nightly",
        "org.mozilla.firefox", "org.mozilla.firefox_beta", "org.mozilla.fenix", "org.mozilla.focus",
        "org.mozilla.klar", "com.microsoft.emmx", "com.opera.browser", "com.opera.browser.beta",
        "com.opera.mini.native", "com.opera.mini.native.beta", "com.opera.gx",
        "com.sec.android.app.sbrowser", "com.duckduckgo.mobile.android", "com.vivaldi.browser",
        "com.kiwibrowser.browser", "com.zoho.ulaa", "com.zoho.ulaa.browser", "com.mi.globalbrowser",
        "com.android.browser", "com.heytap.browser", "com.oppo.browser"
    )

    val BROWSER_NAMES = arrayOf(
        "chrome", "brave", "firefox", "edge", "opera", "ulaa", "vivaldi", "duckduckgo",
        "samsung internet", "kiwi"
    )
}
