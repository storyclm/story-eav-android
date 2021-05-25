package expert.rightperception.attributesapp.ui.content

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import expert.rightperception.attributesapp.data.repository.story_object.StoryObjectRepository
import expert.rightperception.attributesapp.domain.model.objects.ObjectsContainer
import kotlinx.coroutines.launch
import javax.inject.Inject

class ContentViewModel @Inject constructor(
    val storyObjectRepository: StoryObjectRepository
) : ViewModel() {

    private val initialStoryObjectLiveData = MutableLiveData<ObjectsContainer>()

    fun getInitialData(): LiveData<ObjectsContainer> {
        viewModelScope.launch {
            storyObjectRepository.getAttributes()?.let {
                initialStoryObjectLiveData.value = it
            }
        }
        return initialStoryObjectLiveData
    }
}