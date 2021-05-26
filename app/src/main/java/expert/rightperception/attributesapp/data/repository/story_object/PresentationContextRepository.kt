package expert.rightperception.attributesapp.data.repository.story_object

import expert.rightperception.attributesapp.App
import expert.rightperception.attributesapp.data.repository.license.LicenseRepository
import expert.rightperception.attributesapp.domain.model.objects.PresentationContext
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
class PresentationContextRepository @Inject constructor(
    private val app: App,
    private val preferencesStorage: PreferencesStorage,
    private val licenseRepository: LicenseRepository
) : AppUpdatesProvider, ContentUpdatesReceiver {

    private var storyAttributes = StoryAttributes.create(app, StoryAttributesSettings(preferencesStorage.getAttributesEndpoint()))

    private val objectsContainerStateFlow = MutableSharedFlow<PresentationContext>(1)
    private val mutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var listener: AppUpdatesProvider.UpdateListener? = null

    init {
        scope.launch {
            objectsContainerStateFlow.collect {
                listener?.onUpdate(contextObject = it)
            }
        }
    }

    fun setAttributesEndpoint(endpoint: String) {
        if (preferencesStorage.getAttributesEndpoint() != endpoint) {
            preferencesStorage.setAttributesEndpoint(endpoint)
            storyAttributes = StoryAttributes.create(app, StoryAttributesSettings(endpoint))
        }
    }

    fun getAttributesEndpoint(): String {
        return preferencesStorage.getAttributesEndpoint()
    }

    suspend fun setPresentationContext(presentationContext: PresentationContext) {
        withLicenseId { rootParentId ->
            mutex.withLock {
                storyAttributes.getStorageApi().putObject(rootParentId, presentationContext)
                objectsContainerStateFlow.emit(presentationContext)
            }
        }
    }

    suspend fun deleteFormItem(key: String) {
        modifyAttribute(listOf("form", "item", key)) {
            storyAttributes.getStorageApi().deleteById(it.id)
        }
    }

    fun observePresentationContext(): Flow<PresentationContext> {
        return objectsContainerStateFlow
            .onStart {
                getPresenationContext()?.let {
                    emit(it)
                }
            }
    }

    suspend fun getPresenationContext(): PresentationContext? {
        return withLicenseId { rootParentId ->
            if (storyAttributes.getStorageApi().getByParentId(rootParentId).isEmpty()) {
                PresentationContext()
            } else {
                storyAttributes.getStorageApi().getObjectByParentId(rootParentId, PresentationContext::class.java)
            }
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
                    objectsContainerStateFlow.emit(storyAttributes.getStorageApi().getObjectByParentId(rootParentId, PresentationContext::class.java))
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