package expert.rightperception.attributesapp.data.repository.story_object

import expert.rightperception.attributesapp.App
import expert.rightperception.attributesapp.data.repository.license.LicenseRepository
import expert.rightperception.attributesapp.domain.model.objects.PresentationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.breffi.story.domain.bridge.model.AppUpdatesProvider
import ru.breffi.story.domain.bridge.model.ContentUpdatesReceiver
import ru.rightperception.storyattributes.domain.model.AttributeModel
import ru.rightperception.storyattributes.domain.model.ValidatedAttributeModel
import ru.rightperception.storyattributes.external_api.StoryAttributesService
import ru.rightperception.storyattributes.external_api.model.StoryAttributesSettings
import ru.rightperception.storyattributes.utility.asObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresentationContextRepository @Inject constructor(
    private val app: App,
    private val preferencesStorage: PreferencesStorage,
    licenseRepository: LicenseRepository
) : BaseAttributesRepository(
    StoryAttributesService.create(
        app,
        StoryAttributesSettings(
            preferencesStorage.getAttributesEndpoint(),
            autoSynchronizationEnabled = AUTO_SYNCHRONIZATION_ENABLED,
            autoSynchronizationIntervalMillis = SYNCHRONIZATION_INTERVAL_MS
        )
    ),
    licenseRepository
), AppUpdatesProvider, ContentUpdatesReceiver {

    companion object {
        const val SYNCHRONIZATION_INTERVAL_MS = 5 * 60 * 1000L
        const val AUTO_SYNCHRONIZATION_ENABLED = true
    }

    private val presentationContextStateFlow = MutableSharedFlow<PresentationContext>(1)
    private val mutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var syncJob: Job? = null

    private var listener: AppUpdatesProvider.UpdateListener? = null

    init {
        createSyncJob()
        scope.launch {
            presentationContextStateFlow.collect {
                listener?.onUpdate(contextObject = it)
            }
        }
    }

    private fun createSyncJob() {
        syncJob?.cancel()
        syncJob = scope.launch {
            withLicenseId { licenseId ->
                storyAttributes.getSynchronizationApi().observeSynchronizationSuccess(licenseId)
                    .collect { attrs ->
                        if (attrs.any { it.key == "notes" }) {
                            presentationContextStateFlow.emit(attrs.asObject(licenseId, PresentationContext::class.java))
                        }
                    }
            }
        }
    }

    fun setAttributesEndpoint(endpoint: String) {
        if (preferencesStorage.getAttributesEndpoint() != endpoint) {
            preferencesStorage.setAttributesEndpoint(endpoint)
            storyAttributes = StoryAttributesService.create(
                app,
                StoryAttributesSettings(
                    endpoint,
                    autoSynchronizationEnabled = AUTO_SYNCHRONIZATION_ENABLED,
                    autoSynchronizationIntervalMillis = SYNCHRONIZATION_INTERVAL_MS
                )
            )
            createSyncJob()
        }
    }

    fun getAttributesEndpoint(): String {
        return preferencesStorage.getAttributesEndpoint()
    }

    suspend fun setPresentationContext(presentationContext: PresentationContext) {
        withLicenseId { licenseId ->
            mutex.withLock {
                storyAttributes.getStorageApi().putObject(licenseId, presentationContext)
                presentationContextStateFlow.emit(presentationContext)
            }
        }
    }

    suspend fun deleteFormItem(key: String) {
        modifyAttribute(listOf("form", "items", key)) {
            storyAttributes.getStorageApi().deleteById(it.id)
        }
    }

    fun observePresentationContext(): Flow<PresentationContext> {
        return presentationContextStateFlow
            .onStart {
                getPresentationContext()?.let {
                    emit(it)
                }
            }
    }

    suspend fun getPresentationContext(): PresentationContext? {
        return withLicenseId { licenseId ->
            val attrs = storyAttributes.getStorageApi().getByParentId(licenseId)
            if (attrs.any { it.key == "notes" }) {
                attrs.asObject(licenseId, PresentationContext::class.java)
            } else {
                storyAttributes.getStorageApi().putObject(licenseId, PresentationContext())
                storyAttributes.getStorageApi().getObjectByParentId(licenseId, PresentationContext::class.java)
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
            withLicenseId { licenseId ->
                mutex.withLock {
                    val attrs = storyAttributes.getStorageApi().getByParentId(licenseId)
                    pathKeys.subList(1, pathKeys.size).fold(attrs.get(pathKeys[0])) { acc, key ->
                        acc?.get(key)
                    }?.let { attr ->
                        block(attr)
                    }
                    presentationContextStateFlow.emit(storyAttributes.getStorageApi().getObjectByParentId(licenseId, PresentationContext::class.java))
                }
            }
        }
    }
}