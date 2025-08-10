package com.example

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.nicehttp.Requests
import org.jsoup.nodes.Element

class NgefilmProvider : MainAPI() {
    override var mainUrl = "https://new16.ngefilm.site"
    override var name = "Ngefilm"
    override var lang = "id"
    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    // Pakai NiceHttp langsung (mengganti AppUtils.app)
    private val http = Requests()

    // ===== SEARCH =====
    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=${query.trim().replace(' ', '+')}"
        val doc = http.get(url).document
        val items = doc.select("article, .result-item, .ml-item, .movie, .film, .item")
        return items.mapNotNull { toSearchResult(it) }
    }

    private fun toSearchResult(el: Element): SearchResponse? {
        val a = el.selectFirst("a[href]") ?: return null
        val href = fixUrl(a.attr("href"))
        val title = (el.selectFirst(".title, h2, h3")?.text()
            ?: a.attr("title")
            ?: a.text()).ifBlank { "Ngefilm Item" }

        val poster = el.selectFirst("img[src], img[data-src]")?.let { img ->
            fixUrl(img.attr("data-src").ifBlank { img.attr("src") })
        }

        val isSeries = el.text().contains("Episode", ignoreCase = true)
        return if (isSeries) {
            newTvSeriesSearchResponse(title, href) { this.posterUrl = poster }
        } else {
            newMovieSearchResponse(title, href) { this.posterUrl = poster }
        }
    }

    // ===== LOAD DETAIL =====
    override suspend fun load(url: String): LoadResponse {
        val doc = http.get(url).document
        val title = doc.selectFirst("h1, .title, .entry-title")?.text() ?: "Ngefilm"
        val poster = doc.selectFirst(".poster img, .thumbnail img, img")?.attr("src")?.let { fixUrl(it) }
        val plot = doc.selectFirst(".plot, .entry-content, .desc, p")?.text()
        val isSeries = doc.text().contains("Episode", ignoreCase = true)

        return if (isSeries) {
            newTvSeriesLoadResponse(title, url, TvType.TvSeries, emptyList()) {
                this.posterUrl = poster
                this.plot = plot
            }
        } else {
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.plot = plot
            }
        }
    }

    // ===== EXTRACT LINKS =====
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = http.get(data).document
        val frames = doc.select("iframe[src], .player iframe[src], .embed-container iframe[src]")

        frames.forEach { f ->
    val link = fixUrl(f.attr("src"))
    callback(
        newExtractorLink(
            name,                       // source
            "Ngefilm",                  // name
            link,                       // url
            mainUrl,                    // referer
            Qualities.Unknown.value,    // quality
            link.endsWith(".m3u8")      // isM3u8
        )
    )
}

        return frames.isNotEmpty()
    }

    // ===== MAIN PAGE =====
    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val doc = http.get(mainUrl).document
        val latest = doc.select("article, .ml-item, .movie, .film, .item")
            .mapNotNull { toSearchResult(it) }
            .take(20)
        return newHomePageResponse(HomePageList("Terbaru", latest))
    }
}
