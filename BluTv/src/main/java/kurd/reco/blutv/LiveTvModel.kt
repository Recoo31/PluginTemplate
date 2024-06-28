package kurd.reco.blutv

data class LiveTvModel(
    val contents: List<LiveContent>,
    val totalCount: Int
)

data class LiveContent(
    val title: String,
    val id: String,
    val images: List<ImageModel>,
    val media: List<MediaModel>
)

data class MediaModel(
    val url: String,
    val dvr: Boolean
)