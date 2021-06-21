package expert.rightperception.attributesapp.ui.configurator_test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import expert.rightperception.attributesapp.data.repository.story_object.TestObjectRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class ConfiguratorTestViewModel @Inject constructor(
    private val testObjectRepository: TestObjectRepository
) : ViewModel() {

    val testObject = testObjectRepository
        .observeTestObject()
        .distinctUntilChanged()
        .asLiveData()

    fun saveTestObject(objectString: String) {
        testObjectRepository.saveTestObjectString(objectString)
    }
}