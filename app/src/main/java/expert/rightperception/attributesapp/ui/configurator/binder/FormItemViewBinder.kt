package expert.rightperception.attributesapp.ui.configurator.binder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.moqod.android.recycler.multitype.MultiTypeViewBinder
import expert.rightperception.attributesapp.R
import expert.rightperception.attributesapp.ui.configurator.model.FormItemUiModel
import expert.rightperception.attributesapp.ui.configurator.widget.NestedAttributeWidget
import kotlinx.android.synthetic.main.item_form.view.*

class FormItemViewBinder(@param:NonNull private val listener: Listener) : MultiTypeViewBinder<FormItemViewBinder.ViewHolder, FormItemUiModel>() {

    interface Listener {
        fun onValueChange(uiModel: FormItemUiModel)
    }

    override fun isValidModel(o: Any): Boolean {
        return o is FormItemUiModel
    }

    override fun onCreateViewHolder(layoutInflater: LayoutInflater, viewGroup: ViewGroup): ViewHolder {
        val view = layoutInflater.inflate(R.layout.item_form, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, model: FormItemUiModel) {
        val itemView = holder.itemView

        itemView.item_form_input_name_naw.setKey(model.key)
        itemView.item_form_input_name_naw.setValue(model.name)
        itemView.item_form_bg_color_naw.setValue(model.backgroundColor)
        itemView.item_form_font_color_naw.setValue(model.fontColor)
        itemView.item_form_font_size_naw.setValue(model.fontSize.toString())
        itemView.item_form_input_value_naw.setValue(model.inputValue)

        itemView.item_form_input_name_naw.listener = object : NestedAttributeWidget.Listener {

            override fun onValueSet(value: String) {
                listener.onValueChange(model.copy(name = value))
            }
        }
        itemView.item_form_bg_color_naw.listener = object : NestedAttributeWidget.Listener {

            override fun onValueSet(value: String) {
                listener.onValueChange(model.copy(backgroundColor = value))
            }
        }
        itemView.item_form_font_color_naw.listener = object : NestedAttributeWidget.Listener {

            override fun onValueSet(value: String) {
                listener.onValueChange(model.copy(fontColor = value))
            }
        }
        itemView.item_form_font_size_naw.listener = object : NestedAttributeWidget.Listener {

            override fun onValueSet(value: String) {
                listener.onValueChange(model.copy(fontSize = value.toInt()))
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
