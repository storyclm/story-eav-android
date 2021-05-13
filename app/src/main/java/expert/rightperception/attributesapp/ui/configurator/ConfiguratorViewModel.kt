package expert.rightperception.attributesapp.ui.configurator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import expert.rightperception.attributesapp.data.repository.story_object.StoryObjectRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class ConfiguratorViewModel @Inject constructor(
    private val storyObjectRepository: StoryObjectRepository
) : ViewModel() {

    private val mutex = Mutex()

    val objectString = storyObjectRepository
        .observeObject()
        .asLiveData()

    fun saveObject(objectString: String) {
        viewModelScope.launch {
            mutex.withLock {
                storyObjectRepository.saveObject(objectString)
            }
        }
    }
}