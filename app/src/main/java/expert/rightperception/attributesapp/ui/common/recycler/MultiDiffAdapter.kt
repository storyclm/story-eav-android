package expert.rightperception.attributesapp.ui.common.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moqod.android.recycler.diff.DiffCallback
import com.moqod.android.recycler.diff.DiffEntity
import com.moqod.android.recycler.multitype.MultiTypeManager

abstract class MultiDiffAdapter : ListAdapter<DiffEntity, RecyclerView.ViewHolder>(DiffCallback()) {

    private val mManager = MultiTypeManager()

    protected fun addBinder(type: Int, binder: Any) {
        mManager.addBinder(type, MultiDiffHelper.cast(binder))
    }

    fun setData(list: List<DiffEntity>) {
        submitList(list)
    }

    override fun getItemViewType(position: Int): Int {
        val model: Any = this.getItem(position)
        return mManager.getItemViewType(model)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return mManager.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        mManager.onBindViewHolder(holder, getItem(position))
    }
}
