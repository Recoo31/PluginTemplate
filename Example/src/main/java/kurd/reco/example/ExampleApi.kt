package kurd.reco.example

import kurd.reco.api.RemoteRepo
import kurd.reco.api.Resource
import kurd.reco.api.model.DetailScreenModel
import kurd.reco.api.model.HomeItemModel
import kurd.reco.api.model.HomeScreenModel
import kurd.reco.api.model.PlayDataModel
import kurd.reco.api.model.SearchModel
import kurd.reco.api.model.SeriesDataModel

class ExampleApi: RemoteRepo {
    override var seriesList: List<SeriesDataModel>? = null
    override var pagerList: List<HomeItemModel>? = null

    override suspend fun getHomeScreenItems(): Resource<List<HomeScreenModel>> {
        TODO("Not yet implemented")
    }

    override suspend fun getDetailScreenItems(id: Any, isSeries: Boolean): Resource<DetailScreenModel> {
        TODO("Not yet implemented")
    }

    override suspend fun search(query: String): List<SearchModel> {
        TODO("Not yet implemented")
    }

    override suspend fun getUrl(id: Any): Resource<PlayDataModel> {
        TODO("Not yet implemented")
    }
}