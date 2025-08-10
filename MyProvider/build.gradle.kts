
version = 1

cloudstream {
    description = "Contoh plugin Cloudstream oleh pendi"
    authors = listOf("pendi")
    status = 1
    tvTypes = listOf("Movie")
    language = "id"
    iconUrl = "https://upload.wikimedia.org/wikipedia/commons/4/4e/Pleiades_large.jpg"
}

dependencies {
    implementation("org.jsoup:jsoup:1.16.2")
    implementation("com.github.Blatzar:NiceHttp:0.4.11")
}

android {
    namespace = "com.example.myprovider"
    buildFeatures {
        viewBinding = true
    }
}
