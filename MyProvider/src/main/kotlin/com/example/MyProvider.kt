package com.example

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class MyPlugin : Plugin() {
    override fun load(context: Context) {
        // daftar semua provider di sini
        registerMainAPI(MyProvider())
        registerMainAPI(NgefilmProvider())
    }
}
