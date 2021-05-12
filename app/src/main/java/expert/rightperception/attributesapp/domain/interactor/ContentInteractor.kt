package expert.rightperception.attributesapp.domain.interactor

import expert.rightperception.attributesapp.BuildConfig
import expert.rightperception.attributesapp.data.repository.injection.InjectionRepository
import expert.rightperception.attributesapp.domain.model.ContentModel
import ru.breffi.story.domain.interactors.AccountInteractor
import ru.breffi.story.domain.interactors.ClientInteractor
import ru.breffi.story.domain.interactors.PresentationContentInteractor
import ru.breffi.story.domain.interactors.PresentationInteractor
import ru.breffi.story.domain.models.PresentationEntity
import ru.breffi.story.domain.models.download.DownloadStatus
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
        var presentation: PresentationEntity? = presentationInteractor.getPresentation(PRESENTATION_ID)
            .blockingFirst()
            .orElseGet { null }
        if (presentation?.withContent == true && !presentation.isNeedUpdate) {
            val injectionScript = getInjectionScript()
            return if (injectionScript != null) {
                ContentModel(presentation, injectionScript)
            } else {
                null
            }
        } else {
            presentationContentInteractor.getOrUpdatePresentationContent(PRESENTATION_ID, true)
            val result = presentationContentInteractor.listenDownloadFinish(listOf(PRESENTATION_ID)).blockingGet()
            if (result.getOrNull(0)?.status == DownloadStatus.FINISHED) {
                presentation = presentationInteractor.getPresentation(PRESENTATION_ID)
                    .blockingFirst()
                    .orElseGet { null }
                if (presentation?.withContent == true && !presentation.isNeedUpdate) {
                    val injectionScript = getInjectionScript()
                    return if (injectionScript != null) {
                        ContentModel(presentation, injectionScript)
                    } else {
                        null
                    }
                } else {
                    return null
                }
            } else {
                return null
            }
        }
    }

    private suspend fun getInjectionScript(): String? {
        return injectionRepository.getInjectionScript()
    }
}