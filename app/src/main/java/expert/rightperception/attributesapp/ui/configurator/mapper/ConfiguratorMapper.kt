package expert.rightperception.attributesapp.ui.configurator.mapper

import android.content.Context
import expert.rightperception.attributesapp.R
import expert.rightperception.attributesapp.ui.configurator.model.AttributeItemUiModel
import expert.rightperception.attributesapp.ui.configurator.model.NestedAttributeItemUiModel
import expert.rightperception.attributesapp.ui.configurator.model.ConfiguratorUiModel
import expert.rightperception.attributesapp.ui.configurator.model.TitleItemUiModel
import ru.rightperception.storyattributes.domain.model.ValidatedAttributeModel

class ConfiguratorMapper(private val context: Context) {

    fun map(notes: ValidatedAttributeModel?, accentColor: ValidatedAttributeModel, rating: ValidatedAttributeModel): ConfiguratorUiModel {
        val items = listOf(TitleItemUiModel("notes", context.getString(R.string.configurator_item_title_notes)))
            .plus(mapItem("notesVisible", notes?.get("notesVisible")))
            .plus(mapChildItem("text", notes?.get("text")))
            .plus(mapChildItem("color", notes?.get("color")))
            .plus(mapChildItem("fontSize", notes?.get("fontSize")))
        return ConfiguratorUiModel(items)
    }

    private fun mapItem(key: String, validatedAttributeModel: ValidatedAttributeModel?): AttributeItemUiModel {
        return AttributeItemUiModel(
            key = key,
            value = validatedAttributeModel?.boolValue() ?: true
        )
    }

    private fun mapChildItem(key: String, validatedAttributeModel: ValidatedAttributeModel?): NestedAttributeItemUiModel {
        return NestedAttributeItemUiModel(
            key = key,
            value = validatedAttributeModel?.value?.toString() ?: ""
        )
    }

    fun <T> List<T>.plusNullable(item: T?): List<T> {
        return if (item == null) {
            this
        } else {
            this.plus(item)
        }
    }
}