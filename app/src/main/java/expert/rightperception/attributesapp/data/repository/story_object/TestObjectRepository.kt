package expert.rightperception.attributesapp.data.repository.story_object

import com.google.gson.JsonNull
import com.google.gson.JsonObject
import expert.rightperception.attributesapp.App
import expert.rightperception.attributesapp.data.repository.license.LicenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.breffi.story.domain.bridge.model.AppUpdatesProvider
import ru.breffi.story.domain.bridge.model.ContentUpdatesReceiver
import ru.rightperception.storyattributes.external_api.StoryAttributesService
import ru.rightperception.storyattributes.external_api.model.StoryAttributesSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestObjectRepository @Inject constructor(
    app: App,
    licenseRepository: LicenseRepository
) : BaseAttributesRepository(
    StoryAttributesService.create(app, StoryAttributesSettings(PreferencesStorage.DEFAULT_ATTRIBUTES_ENDPOINT)),
    licenseRepository
), AppUpdatesProvider, ContentUpdatesReceiver {

    companion object {

        private const val WRAPPER_KEY = "debugAppState"
    }

    private val testObjectStateFlow = MutableSharedFlow<JsonObject>(1)

    private val mutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var listener: AppUpdatesProvider.UpdateListener? = null

    init {
        scope.launch {
            testObjectStateFlow.collect {
                listener?.onUpdate(contextObject = it)
            }
        }
    }

    suspend fun saveTestObject(jsonObject: JsonObject) {
        withLicenseId { rootParentId ->
            mutex.withLock {
                val wrappedJsonObject = JsonObject().apply {
                    add(WRAPPER_KEY, jsonObject)
                }
                storyAttributes.getStorageApi().putJson(rootParentId, wrappedJsonObject)
                sendUpdate(rootParentId)
            }
        }
    }

    fun observeTestObject(): Flow<JsonObject> {
        return testObjectStateFlow
            .onStart {
                getTestObject()?.let {
                    emit(it)
                }
            }
    }

    suspend fun getTestObject(): JsonObject? {
        return withLicenseId { rootParentId ->
            val element = storyAttributes.getStorageApi().getJsonByParentId(rootParentId).get(WRAPPER_KEY)
            if (element == null || element == JsonNull.INSTANCE) {
                JsonObject()
            } else {
                element.asJsonObject
            }
        }
    }

    override fun deleteProperty(pathKeys: List<String>) {
        scope.launch {
            withLicenseId { rootParentId ->
                mutex.withLock {
                    val attrs = storyAttributes.getStorageApi().getByParentId(rootParentId)
                    pathKeys.fold(attrs.get(WRAPPER_KEY)) { acc, key ->
                        acc?.get(key)
                    }?.let { attr ->
                        storyAttributes.getStorageApi().deleteById(attr.id)
                    }
                    sendUpdate(rootParentId)
                }
            }
        }
    }

    override fun setProperty(pathKeys: List<String>, value: Boolean) {
        setValue(pathKeys, value)
    }

    override fun setProperty(pathKeys: List<String>, value: Double) {
        setValue(pathKeys, value)
    }

    override fun setProperty(pathKeys: List<String>, value: Long) {
        setValue(pathKeys, value)
    }

    override fun setProperty(pathKeys: List<String>, value: String) {
        setValue(pathKeys, value)
    }

    override fun setPropertyNull(pathKeys: List<String>) {
        setValue(pathKeys, null)
    }

    override fun setUpdateListener(updateListener: AppUpdatesProvider.UpdateListener) {
        listener = updateListener
    }

    private fun setValue(pathKeys: List<String>, value: Any?) {
        scope.launch {
            withLicenseId { rootParentId ->
                mutex.withLock {
                    val attrs = storyAttributes.getStorageApi().getByParentId(rootParentId)
                    set(rootParentId, attrs, listOf(WRAPPER_KEY).plus(pathKeys), value)
                    sendUpdate(rootParentId)
                }
            }
        }
    }

    private suspend fun sendUpdate(rootParentId: String) {
        val element = storyAttributes.getStorageApi().getJsonByParentId(rootParentId).get(WRAPPER_KEY)
        if (element != null && element != JsonNull.INSTANCE) {
            testObjectStateFlow.emit(element.asJsonObject)
        }
    }
}