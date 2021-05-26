package expert.rightperception.attributesapp.ui.content

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import expert.rightperception.attributesapp.data.repository.story_object.PresentationContextRepository
import expert.rightperception.attributesapp.data.repository.story_object.TestObjectRepository
import expert.rightperception.attributesapp.ui.content.model.ContentDataModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class ContentViewModel @Inject constructor(
    val presentationContextRepository: PresentationContextRepository,
    val testObjectRepository: TestObjectRepository
) : ViewModel() {

    private val initialStoryObjectLiveData = MutableLiveData<ContentDataModel>()

    fun getInitialData(): LiveData<ContentDataModel> {
        viewModelScope.launch {
            initialStoryObjectLiveData.value = ContentDataModel(
                presentationContext = presentationContextRepository.getPresenationContext(),
                testObject = testObjectRepository.getTestObject()
            )
        }
        return initialStoryObjectLiveData
    }
}