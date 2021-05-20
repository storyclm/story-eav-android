package expert.rightperception.attributesapp.ui.configurator.model

import com.moqod.android.recycler.diff.DiffEntity
import expert.rightperception.attributesapp.domain.model.objects.ObjectsContainer

data class ConfiguratorUiModel(
    val objectsContainer: ObjectsContainer,
    val formItems: List<DiffEntity>
)