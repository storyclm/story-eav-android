package expert.rightperception.attributesapp.data.repository.story_object

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryObjectRepository @Inject constructor(context: Context) {

    private val storyObjectFile = File(context.filesDir.absolutePath, "storyObject.txt").apply {
        if (!exists()) {
            createNewFile()
        }
    }

    private val objectStateFlow = MutableSharedFlow<String>(1)

    suspend fun saveObject(objectString: String) {
        withContext(Dispatchers.IO) {
            storyObjectFile.writeText(objectString)
            objectStateFlow.emit(objectString)
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
}