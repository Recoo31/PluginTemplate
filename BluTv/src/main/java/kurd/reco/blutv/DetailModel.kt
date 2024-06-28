package kurd.reco.blutv

import com.fasterxml.jackson.annotation.JsonProperty


data class DetailModel(
    val data: Data,
    val status: String
)

data class Data(
    val model: DetailModelItems,
    val type: String
)

data class DetailModelItems(
    val Description: String,
    val EpisodesCount: Int?,
    val Image: String,
    val PosterImage: String,
    val Title: String,
    val Id: String,
    val ContentType: String,
    val Seasons: List<SeasonItem>?,
    @JsonProperty("Media")
    val media: MediaItem
)

data class MediaItem(
    @JsonProperty("Id")
    val id: String,
)

data class SeasonItem(
    val Id: String,
    val Title: String,
    val Episodes: List<EpisodeItem>
)

data class EpisodeItem(
    val Id: String,
    val Title: String,
    val Description: String?,
    val Image: String,
    val isDrm: Boolean,
)