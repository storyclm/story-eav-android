package expert.rightperception.attributesapp.data.repository.story_object

import expert.rightperception.attributesapp.App
import expert.rightperception.attributesapp.domain.model.objects.ObjectsContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import ru.rightperception.storyattributes.api.StoryAttributes
import ru.rightperception.storyattributes.api.model.StoryAttributesSettings
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryObjectRepository @Inject constructor(
    private val app: App,
    private val preferencesStorage: PreferencesStorage
) {

    private val storyObjectFile = File(app.filesDir.absolutePath, "storyObject.txt").apply {
        if (!exists()) {
            createNewFile()
        }
    }

    private var storyAttributes = StoryAttributes.create(app, StoryAttributesSettings(preferencesStorage.getAttributesEndpoint()))

    private val objectStateFlow = MutableSharedFlow<String>(1)

    private val objectsContainerStateFlow = MutableSharedFlow<ObjectsContainer>(1)
    private val mutex = Mutex()

    suspend fun saveObject(objectString: String) {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                storyObjectFile.writeText(objectString)
                objectStateFlow.emit(objectString)
            }
        }
    }

    fun observeObject(): Flow<String> {
        return objectStateFlow
            .onStart {
                emit(getObject())
            }
    }

    suspend fun getObject(): String {
        return withContext(Dispatchers.IO) {
            storyObjectFile.readText()
        }
    }

    //

    fun setAttributesEndpoint(endpoint: String) {
        if (preferencesStorage.getAttributesEndpoint() != endpoint) {
            preferencesStorage.setAttributesEndpoint(endpoint)
            storyAttributes = StoryAttributes.create(app, StoryAttributesSettings(endpoint))
        }
    }

    fun getAttributesEndpoint(): String {
        return preferencesStorage.getAttributesEndpoint()
    }

    suspend fun setObject(rootId: String, objectsContainer: ObjectsContainer) {
        mutex.withLock {
            storyAttributes.getStorageApi().putObject(rootId, objectsContainer)
            objectsContainerStateFlow.emit(objectsContainer)
        }
    }

    suspend fun deleteFormItem(rootId: String, key: String) {
        mutex.withLock {
            storyAttributes.getStorageApi().getByParentId(rootId).first { it.key == "form" }.get("items")?.get(key)?.id?.let { id ->
                storyAttributes.getStorageApi().deleteById(id)
            }
            storyAttributes.getStorageApi().getObjectByParentId(rootId, ObjectsContainer::class.java)
            objectsContainerStateFlow.emit(storyAttributes.getStorageApi().getObjectByParentId(rootId, ObjectsContainer::class.java))
        }
    }

    fun observeObjects(rootId: String): Flow<ObjectsContainer> {
        return objectsContainerStateFlow
            .onStart {
                if (storyAttributes.getStorageApi().getByParentId(rootId).isEmpty()) {
                    emit(ObjectsContainer())
                } else {
                    emit(storyAttributes.getStorageApi().getObjectByParentId(rootId, ObjectsContainer::class.java))
                }
            }
    }
}