package expert.rightperception.attributesapp.domain.interactor

import expert.rightperception.attributesapp.BuildConfig
import expert.rightperception.attributesapp.data.repository.injection.InjectionRepository
import expert.rightperception.attributesapp.domain.model.ContentModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.breffi.story.domain.interactors.AccountInteractor
import ru.breffi.story.domain.interactors.ClientInteractor
import ru.breffi.story.domain.interactors.PresentationContentInteractor
import ru.breffi.story.domain.interactors.PresentationInteractor
import ru.breffi.story.domain.models.PresentationEntity
import javax.inject.Inject

class ContentInteractor @Inject constructor(
    private val presentationContentInteractor: PresentationContentInteractor,
    private val presentationInteractor: PresentationInteractor,
    private val clientInteractor: ClientInteractor,
    private val accountInteractor: AccountInteractor,
    private val injectionRepository: InjectionRepository
) {

    companion object {
        private const val PRESENTATION_ID = 209
    }

    suspend fun getContent(): ContentModel? {
        return withContext(Dispatchers.IO) {
            try {
                val account = accountInteractor.getAccount(
                    BuildConfig.STORY_CONTENT_CLIENT_ID,
                    BuildConfig.STORY_CONTENT_SECRET,
                    "",
                    "",
                    BuildConfig.STORY_CONTENT_GRANT_TYPE
                ).blockingGet()
                clientInteractor.updateClients().blockingAwait()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            var presentation: PresentationEntity? = getPresentation()
            when {
                presentation == null -> null
                presentation.withContent && !presentation.isNeedUpdate -> getContentModel(presentation)
                else -> {
                    try {
                        presentationContentInteractor.getOrUpdatePresentationContent(PRESENTATION_ID, true)
                        presentationContentInteractor.listenDownloadFinish(listOf(PRESENTATION_ID)).blockingGet()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    presentation = getPresentation()
                    if (presentation?.withContent == true) {
                        getContentModel(presentation)
                    } else {
                        null
                    }
                }
            }
        }
    }

    private suspend fun getContentModel(presentation: PresentationEntity): ContentModel? {
        val injectionScript = injectionRepository.getInjectionScript()
        return if (injectionScript != null) {
            ContentModel(presentation, injectionScript)
        } else {
            null
        }
    }

    private fun getPresentation(): PresentationEntity? {
        return presentationInteractor.getPresentation(PRESENTATION_ID)
            .blockingFirst()
            .orElseGet { null }
    }
}