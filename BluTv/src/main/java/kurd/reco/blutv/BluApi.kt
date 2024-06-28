package kurd.reco.blutv

import kurd.reco.api.RemoteRepo
import kurd.reco.api.Resource
import kurd.reco.api.app
import kurd.reco.api.model.DetailScreenModel
import kurd.reco.api.model.DrmDataModel
import kurd.reco.api.model.HomeItemModel
import kurd.reco.api.model.HomeScreenModel
import kurd.reco.api.model.PlayDataModel
import kurd.reco.api.model.SearchModel
import kurd.reco.api.model.SeriesDataModel
import kurd.reco.api.model.SeriesItem
import kurd.reco.api.model.SubtitleDataModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody


fun parseImage(
    images: List<ImageModel>,
    imageType: String,
    width: Int = 0,
    height: Int = 0
): String {
    val image = images.find { it.type == imageType } ?: images.firstOrNull()
    return "https://blutv-images.mncdn.com/q/t/i/bluv2/300/${width}x${height}/${image?.id}"
}

object ImageType {
    const val Gallery = "gallery"
    const val Logo = "logo"
    const val Portrait = "portrait"
    const val Landscape = "landscape"
}

private val TAG = "BluTv"

val headers = mapOf(
    "Appauthorization" to "Basic 549a90e9fbead3126851951d:yO2Mo/pWtdtJPhrr+h4HvXI4jaYtDOQ+FCARtVsYzrKU0bK4lqycChAcuG0AvPqxAfgc9PhAJE65/e2MryBG3g==",
    "Appplatform" to "com.blu",
    "Appversion" to "62124567",
    "Authorization" to "Basic 5d36e6c40780020024687002:cE8vwiQrAULRGZ6ZqqXgtztqFgWRU7o6",
    "user-agent" to "okhttp/5.0.0-alpha.2",
    "Accept-Language" to "tr-TR",
    "Accept" to "application/json,application/json",
    "Accept-Charset" to "UTF-8",
    "Content-Type" to "application/json",
    "Connection" to "Keep-Alive"
)

class BluApi : RemoteRepo {
    override var seriesList: List<SeriesDataModel>? = null
    override var pagerList: List<HomeItemModel>? = null


    override suspend fun getHomeScreenItems(): Resource<List<HomeScreenModel>> {
        val movieList = mutableListOf<HomeScreenModel>()
        val url =
            "https://adapter.blupoint.io/api/projects/5d2dc68a92f3636930ba6466/mobile/v2/get-category"
        val jsonData =
            """{"contentTypes":["SerieContainer","MovieContainer"],"id":"60c34e66866ac31908b698fd","package":"SVOD","path":"/","profileId":"5ff5763a84bdbccb076bc98e","sort":true}"""
        val requestBody = jsonData.toRequestBody("application/json".toMediaTypeOrNull())

        return try {
            val liveTv = getLiveTv()
            val liveContents = liveTv.map {
                val image = parseImage(it.images, ImageType.Portrait, 323, 452)
                HomeItemModel(it.id, image, false, true)
            }
            movieList.add(HomeScreenModel("Canlı TV", liveContents))

            val response = app.post(url, headers = headers, requestBody = requestBody)
                .parsed<Array<CategoryModel>>()

            val pager = response.find { it.type == "WidgetPromo" }
            pagerList = getPager(pager)

            response.forEach { item ->
                val title = item.title
                if (title != "Ads") {
                    val contents = item.contents.map {
                        val image = parseImage(it.images, ImageType.Portrait, 323, 452)
                        HomeItemModel(it.url, image, it.contentType == "SerieContainer", false)
                    }
                    movieList.add(HomeScreenModel(title, contents))
                }
            }

            Resource.Success(movieList)
        } catch (e: Throwable) {
            e.printStackTrace()
            Resource.Failure(e.localizedMessage ?: e.message ?: "Unknown Error")
        }
    }

    override suspend fun search(query: String): List<SearchModel> {
        val searchUrl =
            "https://adapter.blupoint.io/api/projects/5d2dc68a92f3636930ba6466/mobile/v2/search"
        val jsonData =
            """{"package":"SVOD","path":"/","profileId":"5ff5763a84bdbccb076bc98e","skip":0,"take":300,"query":"$query"}"""
        val requestBody = jsonData.toRequestBody("application/json".toMediaTypeOrNull())

        return try {
            val searchResponse = app.post(searchUrl, requestBody = requestBody, headers = headers)
                .parsed<SearchWrapper>()
            searchResponse.searchResult.contents.map {
                val image = parseImage(it.images, ImageType.Landscape, 1080, 607)
                SearchModel(it.url, it.title, image, it.contentType == "SerieContainer")
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getDetailScreenItems(
        id: Any,
        isSeries: Boolean
    ): Resource<DetailScreenModel> {
        val url = "https://smarttv.blutv.com.tr/actions/content/getcontent"
        val body = mapOf(
            "url" to id.toString(),
            "platform" to "com.blu.smarttvv2",
            "mediatype" to "smil",
            "package" to "SVOD",
            "dvr" to "true"
        )

        return try {
            val response =
                app.post(url, headers = headers, data = body).parsed<DetailModel>().data.model
            val image =
                "https://blutv-images.mncdn.com/q/t/i/bluv2/300/323x452/" + response.PosterImage
            val backImage =
                "https://blutv-images.mncdn.com/q/t/i/bluv2/300/1080x683/" + response.Image
            if (isSeries) parseSeries(response.Seasons)
            Resource.Success(
                DetailScreenModel(
                    response.Title,
                    response.Description,
                    response.media.id,
                    image,
                    backImage,
                    isSeries
                )
            )
        } catch (e: Throwable) {
            e.printStackTrace()
            Resource.Failure(e.localizedMessage ?: e.message ?: "Unknown Error")
        }
    }

    override suspend fun getUrl(id: Any): Resource<PlayDataModel> {
        val liveTv = getLiveTv()
        val liveContent = liveTv.find { it.id == id }

        if (liveContent != null) {
            val url = liveContent.media.first { !it.dvr }.url

            return Resource.Success(
                PlayDataModel(
                    listOf("BluTv - Canlı" to url),
                    liveContent.title,
                    null,
                    null,
                    null
                )
            )
        }


        val head = mapOf(
            "Host" to "www.blutv.com",
            "Appplatform" to "com.blu",
            "user-agent" to "okhttp/5.0.0-alpha.2"
        )
        val url =
            "https://www.blutv.com/api/player-config?id=$id&media=mpd&url=/reklamlar/web/player"

        return try {
            val response = app.get(url, headers = head).parsed<LiveItemModel>()
            val uri = response.media.source
            val finalUrl =
                if (app.get(uri).code == 200) uri else "https://media.blutv.com/blutv/$id.mpd?bandwidth=-1&height=-1&width=-1&subtitles=vtt&hevc=false"
            Resource.Success(
                PlayDataModel(
                    listOf("BluTv - 1080p" to finalUrl),
                    response.media.title,
                    response.media.drm.let { DrmDataModel("https://wdvn.blutv.com/", null) },
                    response.media.subtitles.map { SubtitleDataModel(it.src, it.code, it.id) },
                    null
                )
            )
        } catch (e: Throwable) {
            Resource.Success(
                PlayDataModel(
                    listOf("BluTv - 1080p" to "https://media.blutv.com/blutv/$id.mpd?bandwidth=-1&height=-1&width=-1&subtitles=vtt&hevc=false"),
                    null,
                    DrmDataModel("https://wdvn.blutv.com/", null),
                    null,
                    null
                )
            )
        }
    }

    private fun parseSeries(seasons: List<SeasonItem>?) {
        seriesList = seasons?.map { season ->
            SeriesDataModel(season.Id, season.Title, season.Episodes.map { episode ->
                SeriesItem(
                    episode.Id,
                    episode.Title,
                    "https://blutv-images.mncdn.com/q/t/i/bluv2/300/600x400/" + episode.Image,
                    episode.Description
                )
            })
        } ?: emptyList()
    }

    private suspend fun getLiveTv(): List<LiveContent> {
        val url = "https://recoo.vercel.app/getliveblu"
        val response = app.get(url).parsed<LiveTvModel>()

        return response.contents
    }

    private fun getPager(pager: CategoryModel?): List<HomeItemModel>? {
        return pager?.contents?.map {
            val image = parseImage(it.images, ImageType.Portrait, 400, 450)
            HomeItemModel(it.url, image, it.contentType == "SerieContainer", false)
        }
    }
}
