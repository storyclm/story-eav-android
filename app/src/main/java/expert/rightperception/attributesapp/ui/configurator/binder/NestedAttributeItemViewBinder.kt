package expert.rightperception.attributesapp.ui.configurator.binder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.moqod.android.recycler.multitype.MultiTypeViewBinder
import expert.rightperception.attributesapp.R
import expert.rightperception.attributesapp.ui.configurator.model.AttributeItemUiModel

class NestedAttributeItemViewBinder(@param:NonNull private val listener: Listener) : MultiTypeViewBinder<NestedAttributeItemViewBinder.ViewHolder, AttributeItemUiModel>() {

    interface Listener {
        fun onCityClick(uiModel: AttributeItemUiModel)
    }

    override fun isValidModel(o: Any): Boolean {
        return o is AttributeItemUiModel
    }

    override fun onCreateViewHolder(layoutInflater: LayoutInflater, viewGroup: ViewGroup): ViewHolder {
        val view = layoutInflater.inflate(R.layout.item_nested_attribute, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, model: AttributeItemUiModel) {
        val itemView = holder.itemView

//        itemView.item_simple_text_tv.text = model.title
        itemView.setOnClickListener {
            listener.onCityClick(model)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
