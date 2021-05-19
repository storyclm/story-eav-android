package expert.rightperception.attributesapp.ui.main.model

import expert.rightperception.attributesapp.domain.model.ContentModel
import expert.rightperception.attributesapp.domain.model.LicenseModel

sealed class MainUiState

object Loading : MainUiState()

object Error : MainUiState()

data class Data(val licenseModel: LicenseModel, val contentModel: ContentModel) : MainUiState()