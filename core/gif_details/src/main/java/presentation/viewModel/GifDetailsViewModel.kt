package presentation.viewModel
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.models.GifDetailsModel
import domain.repo.GifDetailsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import presentation.actions.GifDetailsScreenActions

class GifDetailsViewModel(
    private val repository: GifDetailsRepository
) : ViewModel() {

    private val _gifPositionState = MutableStateFlow(0)
    val gifPositionState = _gifPositionState.asStateFlow()

    private val _gifDetails = MutableStateFlow(
        GifDetailsModel(
            title = "",
            gifId = "",
            url = "",
        )
    )
    val gifDetails = _gifDetails.asStateFlow()

    fun onAction(action: GifDetailsScreenActions) {
        when (action) {
            is GifDetailsScreenActions.OnRetrievedGifDetails -> getCurrentGifDetails(action.retrievedGifId)
            is GifDetailsScreenActions.OnNextGifChanged -> nextGifId()
            is GifDetailsScreenActions.OnPreviousGifChanged -> previousGifId()
        }
    }

    private fun getCurrentGifDetails(gifId: String) {
        viewModelScope.launch {
            val gifDetails = repository.getGifById(gifId)
            val currentIndex = repository.getGifIndexInsideListByGifId(gifId)
            _gifPositionState.emit(currentIndex)
            _gifDetails.emit(gifDetails)
        }
    }

    private fun nextGifId() {
        viewModelScope.launch {
            val newIndex = _gifPositionState.value + 1
            _gifPositionState.emit(newIndex)

            val newGifId = repository.getGifIdByPosition(newIndex)
            val updatedGifDetails = repository.updateGifDetails(newGifId)

            _gifDetails.emit(
                GifDetailsModel(
                    title = updatedGifDetails.title,
                    gifId = updatedGifDetails.gifId,
                    url = updatedGifDetails.url,
                    id = updatedGifDetails.id
                )
            )
        }
    }

    private fun previousGifId() {
        viewModelScope.launch {
            val newIndex = _gifPositionState.value - 1
            _gifPositionState.emit(newIndex)

            val previousGifId = repository.getGifIdByPosition(newIndex)
            val updatedDetails = repository.updateGifDetails(previousGifId)

            async {
                _gifDetails.emit(
                    GifDetailsModel(
                        title = updatedDetails.title,
                        gifId = updatedDetails.gifId,
                        url = updatedDetails.url,
                        id = updatedDetails.id
                    )
                )
            }.await()
        }
    }
}