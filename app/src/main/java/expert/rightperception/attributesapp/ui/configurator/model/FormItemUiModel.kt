package expert.rightperception.attributesapp.ui.configurator.model

import com.moqod.android.recycler.diff.DiffEntity

data class FormItemUiModel(
    val key: String,
    val name: String,
    val backgroundColor: String,
    val fontColor: String,
    val fontSize: Int,
    val inputValue: String
) : DiffEntity {

    override fun areItemsTheSame(entity: DiffEntity?): Boolean {
        return entity is FormItemUiModel && entity.key == key
    }

    override fun areContentsTheSame(entity: DiffEntity?): Boolean {
        return equals(entity)
    }
}