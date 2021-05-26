package expert.rightperception.attributesapp.ui.content.model

import com.google.gson.JsonObject
import expert.rightperception.attributesapp.domain.model.objects.PresentationContext

data class ContentDataModel(
    val presentationContext: PresentationContext?,
    val testObject: JsonObject?
)