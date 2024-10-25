package presentation.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import domain.models.PagedGifsModel
import domain.repo.GifsScreenRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import presentation.actions.GifsScreenAction

@OptIn(ExperimentalCoroutinesApi::class)
class GifsScreenViewModel(
    repository: GifsScreenRepository,
):ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()


     val pagedGifsFlow:StateFlow<PagingData<PagedGifsModel>> = _searchText.flatMapLatest { query->
         repository.updateGifsList(
             PagedGifsModel(
                 query,
             )
         )
         delay(1000)
         repository.gifsPagingSource(query)
     }.cachedIn(viewModelScope)
         .stateIn(
         viewModelScope,
         SharingStarted.WhileSubscribed(),
         PagingData.empty()
     )

    fun onAction(
        action: GifsScreenAction,
    ){
       when(action){
           is GifsScreenAction.onSearchTextChanged -> onSearchTextChange(action.text)
       }
    }

    private fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

}