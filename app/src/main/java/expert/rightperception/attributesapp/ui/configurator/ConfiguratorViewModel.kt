package expert.rightperception.attributesapp.ui.configurator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import expert.rightperception.attributesapp.data.repository.story_object.StoryObjectRepository
import expert.rightperception.attributesapp.domain.model.objects.ObjectsContainer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConfiguratorViewModel @Inject constructor(
    private val storyObjectRepository: StoryObjectRepository
) : ViewModel() {

    private lateinit var licenseId: String
    private val licenseIdFlow = MutableSharedFlow<String>(1)

    val uiModel = licenseIdFlow
        .filterNotNull()
        .flatMapLatest { licenseId -> storyObjectRepository.observeObjects(licenseId) }
        .asLiveData()

    fun setup(licenseId: String) {
        this.licenseId = licenseId
        licenseIdFlow.tryEmit(licenseId)
    }

    fun getAttributesEndpoint(): String {
        return storyObjectRepository.getAttributesEndpoint()
    }

    fun saveObjects(endpoint: String, objectsContainer: ObjectsContainer) {
        viewModelScope.launch {
            storyObjectRepository.setAttributesEndpoint(endpoint)
            storyObjectRepository.setObject(licenseId, objectsContainer)
        }
    }

    fun deleteFormItem(endpoint: String, key: String) {
        viewModelScope.launch {
            storyObjectRepository.setAttributesEndpoint(endpoint)
            storyObjectRepository.deleteFormItem(licenseId, key)
        }
    }
}