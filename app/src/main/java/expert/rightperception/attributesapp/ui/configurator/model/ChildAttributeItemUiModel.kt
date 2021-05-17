package expert.rightperception.attributesapp.ui.configurator.model

import com.moqod.android.recycler.diff.DiffEntity

data class ChildAttributeItemUiModel(
    val id: String
) : DiffEntity {

    override fun areItemsTheSame(entity: DiffEntity?): Boolean {
        return entity is ChildAttributeItemUiModel && entity.id == id
    }

    override fun areContentsTheSame(entity: DiffEntity?): Boolean {
        return equals(entity)
    }
}