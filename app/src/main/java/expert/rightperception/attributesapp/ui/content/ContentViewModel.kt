package expert.rightperception.attributesapp.ui.content

import androidx.lifecycle.*
import expert.rightperception.attributesapp.data.repository.story_object.StoryObjectRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class ContentViewModel @Inject constructor(
    private val storyObjectRepository: StoryObjectRepository
) : ViewModel() {

    private val mutex = Mutex()

    val storyObjectLiveData = storyObjectRepository.observeObject()
        .asLiveData()

    private val initialStoryObjectLiveData = MutableLiveData<String>()

    fun getData(): LiveData<String> {
        viewModelScope.launch {
            initialStoryObjectLiveData.value = storyObjectRepository.getObject()
        }
        return initialStoryObjectLiveData
    }

    fun updateStoryObject(objectString: String) {
        viewModelScope.launch {
            mutex.withLock {
                storyObjectRepository.saveObject(objectString)
            }
        }
    }
}