package expert.rightperception.attributesapp.data.repository.story_object

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
import ru.rightperception.storyattributes.api.StoryAttributes
import ru.rightperception.storyattributes.api.model.StoryAttributesSettings
import ru.rightperception.storyattributes.domain.model.AttributeModel
import ru.rightperception.storyattributes.domain.model.ValidatedAttributeModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestObjectRepository @Inject constructor(
    app: App,
    private val licenseRepository: LicenseRepository
) : AppUpdatesProvider, ContentUpdatesReceiver {

    companion object {

        private const val WRAPPER_KEY = "debugAppState"
    }

    private val storyAttributes = StoryAttributes.create(app, StoryAttributesSettings(PreferencesStorage.DEFAULT_ATTRIBUTES_ENDPOINT))

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
                storyAttributes.getStorageApi().getJsonByParentId(rootParentId).getAsJsonObject(WRAPPER_KEY)?.let {
                    testObjectStateFlow.emit(it)
                }
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
            storyAttributes.getStorageApi().getJsonByParentId(rootParentId).getAsJsonObject(WRAPPER_KEY) ?: JsonObject()
        }
    }

    override fun deleteProperty(pathKeys: List<String>) {
        modifyAttribute(pathKeys) {
            storyAttributes.getStorageApi().deleteById(it.id)
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

    fun List<ValidatedAttributeModel>.get(key: String): ValidatedAttributeModel? {
        return firstOrNull { it.key == key }
    }

    private fun setValue(pathKeys: List<String>, value: Any?) {
        modifyAttribute(pathKeys) { validatedAttr ->
            val attributeModel = AttributeModel(
                key = validatedAttr.key,
                parentId = validatedAttr.parentId,
                value = value
            )
            storyAttributes.getStorageApi().putAttributes(listOf(attributeModel))
        }
    }

    private fun modifyAttribute(pathKeys: List<String>, block: suspend (attributeModel: ValidatedAttributeModel) -> Unit) {
        scope.launch {
            withLicenseId { rootParentId ->
                mutex.withLock {
                    val attrs = storyAttributes.getStorageApi().getByParentId(rootParentId)
                    pathKeys.subList(1, pathKeys.size).fold(attrs.get(pathKeys[0])) { acc, key ->
                        acc?.get(key)
                    }?.let { attr ->
                        block(attr)
                    }
                    storyAttributes.getStorageApi().getJsonByParentId(rootParentId).getAsJsonObject(WRAPPER_KEY)?.let {
                        testObjectStateFlow.emit(it)
                    }
                }
            }
        }
    }

    private suspend fun <T : Any> withLicenseId(block: suspend (attributeModel: String) -> T?): T? {
        return licenseRepository.getLicense()?.id?.let { rootParentId ->
            block(rootParentId)
        }
    }
}