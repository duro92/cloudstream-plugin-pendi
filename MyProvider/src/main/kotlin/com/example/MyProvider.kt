package com.example

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.SearchResponse

class MyProvider : MainAPI() {
    override var mainUrl = "https://example.org"
    override var name = "MyProvider"
    override val supportedTypes = setOf(TvType.Movie)
    override val hasMainPage = false
    override var lang = "id" // ← FIX: pakai var

    override suspend fun search(query: String): List<SearchResponse> {
        return emptyList()
    }
}
