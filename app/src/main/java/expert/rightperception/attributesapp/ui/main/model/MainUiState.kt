package expert.rightperception.attributesapp.ui.main.model

import expert.rightperception.attributesapp.domain.model.ContentModel

sealed class MainUiState

object Loading : MainUiState()

object Error : MainUiState()

data class Data(val contentModel: ContentModel) : MainUiState()