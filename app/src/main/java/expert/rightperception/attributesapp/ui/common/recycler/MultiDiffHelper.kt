package expert.rightperception.attributesapp.ui.common.recycler

import androidx.recyclerview.widget.RecyclerView
import com.moqod.android.recycler.multitype.MultiTypeViewBinder

object MultiDiffHelper {

    @Suppress("UNCHECKED_CAST")
    fun cast(binder: Any): MultiTypeViewBinder<RecyclerView.ViewHolder, Any> {
        return binder as MultiTypeViewBinder<RecyclerView.ViewHolder, Any>
    }

}