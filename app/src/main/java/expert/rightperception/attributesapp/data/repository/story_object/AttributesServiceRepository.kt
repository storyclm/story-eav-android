package expert.rightperception.attributesapp.data.repository.story_object

import expert.rightperception.attributesapp.App
import ru.rightperception.storyattributes.external_api.StoryAttributesService
import ru.rightperception.storyattributes.external_api.model.StoryAttributesSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttributesServiceRepository @Inject constructor(
    private val app: App,
    private val preferencesStorage: PreferencesStorage
) {

    companion object {
        const val SYNCHRONIZATION_INTERVAL_MS = 5 * 60 * 1000L
        const val AUTO_SYNCHRONIZATION_ENABLED = true
    }

    private var storyAttributes = StoryAttributesService.create(
        app,
        StoryAttributesSettings(
            preferencesStorage.getAttributesEndpoint(),
            autoSynchronizationEnabled = AUTO_SYNCHRONIZATION_ENABLED,
            autoSynchronizationIntervalMillis = SYNCHRONIZATION_INTERVAL_MS
        )
    )

    fun getActiveService(): StoryAttributesService {
        return storyAttributes
    }

    fun recreateWithEndpoint(endpoint: String): Boolean {
        return if (preferencesStorage.getAttributesEndpoint() != endpoint) {
            preferencesStorage.setAttributesEndpoint(endpoint)
            storyAttributes = StoryAttributesService.create(
                app,
                StoryAttributesSettings(
                    endpoint,
                    autoSynchronizationEnabled = PresentationContextRepository.AUTO_SYNCHRONIZATION_ENABLED,
                    autoSynchronizationIntervalMillis = PresentationContextRepository.SYNCHRONIZATION_INTERVAL_MS
                )
            )
            true
        } else {
            false
        }
    }
}