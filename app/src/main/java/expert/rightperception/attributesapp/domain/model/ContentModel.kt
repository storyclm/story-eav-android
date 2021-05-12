package expert.rightperception.attributesapp.domain.model

import ru.breffi.story.domain.models.PresentationEntity

data class ContentModel(
    val presentationEntity: PresentationEntity,
    val injectionScript: String
)