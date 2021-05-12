package expert.rightperception.attributesapp.ui.main.model

import com.moqod.android.recycler.diff.DiffEntity

sealed class MainUiState

object Loading : MainUiState()

object Error : MainUiState()

data class Content(val items: List<DiffEntity>) : MainUiState()