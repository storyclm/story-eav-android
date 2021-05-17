package expert.rightperception.attributesapp.ui.configurator.model

import com.moqod.android.recycler.diff.DiffEntity

data class TitleItemUiModel(
    val id: String,
    val title: String
) : DiffEntity {

    override fun areItemsTheSame(entity: DiffEntity?): Boolean {
        return entity is TitleItemUiModel && entity.id == id
    }

    override fun areContentsTheSame(entity: DiffEntity?): Boolean {
        return equals(entity)
    }
}