package kurd.reco.blutv



data class SearchWrapper(
    val searchResult: SearchResult
)

data class SearchResult(
    val contents: List<Content>,
    val totalCount: Int
)

data class Content(
    val id: String,
    val url: String,
    val title: String,
    val description: String,
    val images: List<ImageModel>,
    val contentType: String
)