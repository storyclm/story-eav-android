package expert.rightperception.attributesapp.data.repository.story_object

import expert.rightperception.attributesapp.data.repository.license.LicenseRepository
import expert.rightperception.attributesapp.domain.model.objects.PresentationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.breffi.story.domain.bridge.model.AppUpdatesProvider
import ru.breffi.story.domain.bridge.model.ContentUpdatesReceiver
import ru.rightperception.storyattributes.domain.model.AttributeModel
import ru.rightperception.storyattributes.domain.model.ValidatedAttributeModel
import ru.rightperception.storyattributes.utility.asObject
import ru.rightperception.storyattributes.utility.get
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresentationContextRepository @Inject constructor(
    private val preferencesStorage: PreferencesStorage,
    attributesServiceRepository: AttributesServiceRepository,
    licenseRepository: LicenseRepository
) : BaseAttributesRepository(
    attributesServiceRepository,
    licenseRepository
), AppUpdatesProvider, ContentUpdatesReceiver {

    companion object {
        const val SYNCHRONIZATION_INTERVAL_MS = 5 * 60 * 1000L
        const val AUTO_SYNCHRONIZATION_ENABLED = true
    }

    private val presentationContextStateFlow = MutableSharedFlow<PresentationContext>(1)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val singleThreadScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher() + SupervisorJob())
    private val mutex = Mutex()

    private var syncJob: Job? = null

    private var listener: AppUpdatesProvider.UpdateListener? = null

    init {
        createSyncJob()
        scope.launch {
            presentationContextStateFlow
                .distinctUntilChanged()
                .collect {
                    listener?.onUpdate(contextObject = it)
                }
        }
    }

    fun setAttributesEndpoint(endpoint: String) {
        if (attributesServiceRepository.recreateWithEndpoint(endpoint)) {
            createSyncJob()
        }
    }

    fun getAttributesEndpoint(): String {
        return preferencesStorage.getAttributesEndpoint()
    }

    fun setPresentationContext(presentationContext: PresentationContext) {
        launchInSequence {
            withLicenseId { licenseId ->
                attributesServiceRepository.getActiveService().getStorageApi().putObject(licenseId, presentationContext)
                presentationContextStateFlow.emit(presentationContext)
            }
        }
    }

    fun deleteFormItem(key: String) {
        modifyAttribute(listOf("form", "items", key)) {
            attributesServiceRepository.getActiveService().getStorageApi().deleteById(it.id)
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
            val attrs = attributesServiceRepository.getActiveService().getStorageApi().getByParentId(licenseId)
            if (attrs.any { it.key == "notes" }) {
                attrs.asObject(licenseId, PresentationContext::class.java)
            } else {
                attributesServiceRepository.getActiveService().getStorageApi().putObject(licenseId, PresentationContext())
                attributesServiceRepository.getActiveService().getStorageApi().getObjectByParentId(licenseId, PresentationContext::class.java)
            }
        }
    }

    override fun deleteProperty(pathKeys: List<String>) {
        modifyAttribute(pathKeys) {
            attributesServiceRepository.getActiveService().getStorageApi().deleteById(it.id)
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

    override fun setUpdateListener(updateListener: AppUpdatesProvider.UpdateListener?) {
        listener = updateListener
    }

    private fun setValue(pathKeys: List<String>, value: Any?) {
        modifyAttribute(pathKeys) { validatedAttr ->
            val attributeModel = AttributeModel(
                key = validatedAttr.key,
                parentId = validatedAttr.parentId,
                value = value
            )
            attributesServiceRepository.getActiveService().getStorageApi().putAttributes(listOf(attributeModel))
        }
    }

    private fun modifyAttribute(pathKeys: List<String>, block: suspend (attributeModel: ValidatedAttributeModel) -> Unit) {
        launchInSequence {
            withLicenseId { licenseId ->
                val attrs = attributesServiceRepository.getActiveService().getStorageApi().getByParentId(licenseId)
                pathKeys.subList(1, pathKeys.size).fold(attrs[pathKeys[0]]) { acc, key ->
                    acc?.get(key)
                }?.let { attr ->
                    block(attr)
                }
                val presentation = attributesServiceRepository.getActiveService().getStorageApi().getObjectByParentId(licenseId, PresentationContext::class.java)
                presentationContextStateFlow.emit(presentation)
            }
        }
    }

    private fun createSyncJob() {
        syncJob?.cancel()
        syncJob = scope.launch {
            withLicenseId { licenseId ->
                attributesServiceRepository.getActiveService().getSynchronizationApi().observeSynchronizationSuccess(licenseId)
                    .collect { attrs ->
                        if (attrs.any { it.key == "notes" }) {
                            val presentation = attributesServiceRepository.getActiveService().getStorageApi().getObjectByParentId(licenseId, PresentationContext::class.java)
                            presentationContextStateFlow.emit(presentation)
                        }
                    }
            }
        }
    }

    private fun launchInSequence(block: suspend CoroutineScope.() -> Unit) {
        singleThreadScope.launch {
            mutex.withLock {
                block()
            }
        }
    }
}