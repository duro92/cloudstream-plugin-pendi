package com.example

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class NgefilmProvider : MainAPI() {
    override var mainUrl = "https://new16.ngefilm.site"
    override var name = "Ngefilm"
    override var lang = "id"
    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    /** =======================
     *  SEARCH
     *  ======================= */
    override suspend fun search(query: String): List<SearchResponse> {
        // Banyak situs WP pakai ?s= untuk pencarian
        val url = "$mainUrl/?s=${query.trim().replace(' ', '+')}"
        val doc = app.get(url).document

        // Coba beberapa selector umum (tema WP/Playtube/Dooplay dsb)
        val items = doc.select(
            "article, .result-item, .ml-item, .movie, .film, .item"
        )

        return items.mapNotNull { el ->
            toSearchResult(el)
        }
    }

    private fun toSearchResult(el: Element): SearchResponse? {
        // Cari anchor & title di berbagai kemungkinan struktur
        val a = el.selectFirst("a[href]") ?: return null
        val href = fixUrl(a.attr("href"))
        val title = (el.selectFirst(".title, h2, h3")?.text()
            ?: a.attr("title")
            ?: a.text()).ifBlank { "Ngefilm Item" }

        val poster = el.selectFirst("img[src], img[data-src]")?.let {
            fixUrl(it.attr("data-src").ifBlank { it.attr("src") })
        }

        // Heuristik sederhana: kalau ada teks "Episode" anggap series
        val isSeries = el.text().contains("Episode", ignoreCase = true)

        return if (isSeries) {
            newTvSeriesSearchResponse(title, href) {
                this.posterUrl = poster
            }
        } else {
            newMovieSearchResponse(title, href) {
                this.posterUrl = poster
            }
        }
    }

    /** =======================
     *  LOAD DETAIL
     *  ======================= */
    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1, .title, .entry-title")?.text() ?: "Ngefilm"
        val poster = doc.selectFirst(".poster img, .thumbnail img, img")?.attr("src")?.let { fixUrl(it) }
        val plot = doc.selectFirst(".plot, .entry-content, .desc, p")?.text()

        // Jika halaman punya “Episode” → anggap series, else movie
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

    /** =======================
     *  EXTRACT LINKS
     *  ======================= */
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = app.get(data).document

        // Ambil semua <iframe> player
        val frames = doc.select("iframe[src], .player iframe[src], .embed-container iframe[src]")
        frames.forEach { f ->
            val link = fixUrl(f.attr("src"))
            // Kirim sebagai link direct ke extractor bawaan Cloudstream
            callback(
                ExtractorLink(
                    source = name,
                    name = "Ngefilm",
                    url = link,
                    referer = mainUrl,
                    quality = Qualities.Unknown.value,
                    isM3u8 = link.endsWith(".m3u8")
                )
            )
        }
        return frames.isNotEmpty()
    }

    /** =======================
     *  MAIN PAGE (opsional)
     *  ======================= */
    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val doc = app.get(mainUrl).document

        // Kategori umum: Terbaru
        val latest = doc.select("article, .ml-item, .movie, .film, .item")
            .mapNotNull { toSearchResult(it) }
            .take(20)

        return newHomePageResponse(
            HomePageList("Terbaru", latest)
        )
    }
}
