package expert.rightperception.attributesapp.ui.configurator.model

import com.moqod.android.recycler.diff.DiffEntity
import expert.rightperception.attributesapp.domain.model.objects.PresentationContext

data class ConfiguratorUiModel(
    val presentationContext: PresentationContext,
    val formItems: List<DiffEntity>
)