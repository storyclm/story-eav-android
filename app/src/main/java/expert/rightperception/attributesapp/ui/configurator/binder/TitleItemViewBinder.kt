package expert.rightperception.attributesapp.ui.configurator.binder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.moqod.android.recycler.multitype.MultiTypeViewBinder
import expert.rightperception.attributesapp.R

import expert.rightperception.attributesapp.ui.configurator.model.TitleItemUiModel

class TitleItemViewBinder : MultiTypeViewBinder<TitleItemViewBinder.ViewHolder, TitleItemUiModel>() {

    override fun isValidModel(o: Any): Boolean {
        return o is TitleItemUiModel
    }

    override fun onCreateViewHolder(layoutInflater: LayoutInflater, viewGroup: ViewGroup): ViewHolder {
        val view = layoutInflater.inflate(R.layout.item_title, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, model: TitleItemUiModel) {
        val itemView = holder.itemView

//        itemView.item_simple_text_tv.text = model.title

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
