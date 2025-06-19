package com.myflixerz

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.models.*

class MyFlixerz : MainAPI() {
    override var name = "MyFlixerz"
    override var mainUrl = "https://myflixerz.to"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/search/$query"
        val doc = app.get(url).document
        return doc.select("div.flix-item").map {
            SearchResponse(
                title = it.selectFirst("a.title")?.text() ?: "",
                url = it.selectFirst("a")?.attr("href") ?: "",
                posterUrl = it.selectFirst("img")?.attr("src") ?: ""
            )
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val info = doc.selectFirst("div.movie-info")
        val episodes = mutableListOf<Episode>()
        doc.select("ul.episodes li").forEachIndexed { i, el ->
            episodes += Episode(name = el.text(), url = el.selectFirst("a")!!.attr("href"), episode = i + 1)
        }

        return LoadResponse(
            name = info.selectFirst("h1")!!.text(),
            posterUrl = info.selectFirst("img")!!.attr("src"),
            summary = doc.selectFirst("div.description")!!.text(),
            episodes = episodes.ifEmpty { null }
        )
    }

    override suspend fun loadLinks(
        data: String, isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = app.get(data).document
        doc.select("iframe").forEach { frame ->
            val src = frame.attr("src")
            callback.invoke(ExtractorLink(src, "MyFlixerz", src))
        }
        return true
    }
}
