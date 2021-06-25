package expert.rightperception.attributesapp.data.repository.story_object

import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import expert.rightperception.attributesapp.data.repository.license.LicenseRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.breffi.story.domain.bridge.model.AppUpdatesProvider
import ru.breffi.story.domain.bridge.model.ContentUpdatesReceiver
import ru.rightperception.storyattributes.utility.get
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestObjectRepository @Inject constructor(
    attributesServiceRepository: AttributesServiceRepository,
    licenseRepository: LicenseRepository
) : BaseAttributesRepository(
    attributesServiceRepository,
    licenseRepository
), AppUpdatesProvider, ContentUpdatesReceiver {

    companion object {

        private const val WRAPPER_KEY = "debugAppState"
    }

    private val testObjectStateFlow = MutableSharedFlow<JsonObject>(1)

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val singleThreadScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher() + SupervisorJob())
    private val mutex = Mutex()

    private var syncJob: Job? = null

    private var listener: AppUpdatesProvider.UpdateListener? = null

    init {
        createSyncJob()
        scope.launch {
            testObjectStateFlow
                .distinctUntilChanged()
                .collect {
                listener?.onUpdate(contextObject = it)
            }
        }
    }

    fun saveTestObjectString(objectString: String) {
        launchInSequence {
            withLicenseId { rootParentId ->
                try {
                    val jsonObject = JsonParser.parseString(objectString).asJsonObject
                    val wrappedJsonObject = JsonObject().apply {
                        add(WRAPPER_KEY, jsonObject)
                    }
                    attributesServiceRepository.getActiveService().getStorageApi().putJson(rootParentId, wrappedJsonObject)
                    sendUpdate(rootParentId)
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
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
            val element = attributesServiceRepository.getActiveService().getStorageApi().getJsonByParentId(rootParentId).get(WRAPPER_KEY)
            if (element == null || element == JsonNull.INSTANCE) {
                JsonObject()
            } else {
                element.asJsonObject
            }
        }
    }

    override fun deleteProperty(pathKeys: List<String>) {
        launchInSequence {
            withLicenseId { rootParentId ->
                val attrs = attributesServiceRepository.getActiveService().getStorageApi().getByParentId(rootParentId)
                pathKeys.fold(attrs[WRAPPER_KEY]) { acc, key ->
                    acc?.get(key)
                }?.let { attr ->
                    attributesServiceRepository.getActiveService().getStorageApi().deleteById(attr.id)
                }
                sendUpdate(rootParentId)
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

    override fun setUpdateListener(updateListener: AppUpdatesProvider.UpdateListener?) {
        listener = updateListener
    }

    private fun setValue(pathKeys: List<String>, value: Any?) {
        launchInSequence {
            withLicenseId { rootParentId ->
                val attrs = attributesServiceRepository.getActiveService().getStorageApi().getByParentId(rootParentId)
                set(rootParentId, attrs, listOf(WRAPPER_KEY).plus(pathKeys), value)
                sendUpdate(rootParentId)
            }
        }
    }

    private suspend fun sendUpdate(rootParentId: String) {
        val element = attributesServiceRepository.getActiveService().getStorageApi().getJsonByParentId(rootParentId).get(WRAPPER_KEY)
        if (element != null && element != JsonNull.INSTANCE) {
            testObjectStateFlow.emit(element.asJsonObject)
        }
    }

    private fun createSyncJob() {
        syncJob?.cancel()
        syncJob = scope.launch {
            withLicenseId { licenseId ->
                attributesServiceRepository.getActiveService().getSynchronizationApi().observeSynchronizationSuccess(licenseId)
                    .collect { attrs ->
                        sendUpdate(licenseId)
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