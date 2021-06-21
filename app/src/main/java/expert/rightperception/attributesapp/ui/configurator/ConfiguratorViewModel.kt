package expert.rightperception.attributesapp.ui.configurator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import expert.rightperception.attributesapp.data.repository.story_object.PresentationContextRepository
import expert.rightperception.attributesapp.domain.model.objects.PresentationContext
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class ConfiguratorViewModel @Inject constructor(
    private val presentationContextRepository: PresentationContextRepository
) : ViewModel() {

    val uiModel = presentationContextRepository.observePresentationContext()
        .distinctUntilChanged()
        .asLiveData()

    fun getAttributesEndpoint(): String {
        return presentationContextRepository.getAttributesEndpoint()
    }

    fun saveObjects(endpoint: String, presentationContext: PresentationContext) {
        presentationContextRepository.setAttributesEndpoint(endpoint)
        presentationContextRepository.setPresentationContext(presentationContext)
    }

    fun deleteFormItem(endpoint: String, key: String) {
        presentationContextRepository.setAttributesEndpoint(endpoint)
        presentationContextRepository.deleteFormItem(key)
    }
}