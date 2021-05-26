package expert.rightperception.attributesapp.ui.configurator_test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import expert.rightperception.attributesapp.data.repository.story_object.TestObjectRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConfiguratorTestViewModel @Inject constructor(
    private val testObjectRepository: TestObjectRepository
) : ViewModel() {

    val testObject = testObjectRepository
        .observeTestObject()
        .asLiveData()

    fun saveTestObject(objectString: String) {
        viewModelScope.launch {
            try {
                val jsonObject = JsonParser.parseString(objectString).asJsonObject
                testObjectRepository.saveTestObject(jsonObject)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
            }
        }
    }
}