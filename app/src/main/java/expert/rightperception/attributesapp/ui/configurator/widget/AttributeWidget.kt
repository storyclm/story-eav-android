package expert.rightperception.attributesapp.ui.configurator.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupMenu
import expert.rightperception.attributesapp.R
import kotlinx.android.synthetic.main.item_attribute.view.*

class AttributeWidget(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    interface Listener {
        fun onValueSet(value: Boolean)
    }

    var listener: Listener? = null
    private var popup: PopupMenu

    init {
        View.inflate(context, R.layout.item_attribute, this)

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.AttributeWidget, 0, 0)
            item_attribute_key_tv.text = a.getString(R.styleable.AttributeWidget_key)
            a.recycle()
        }

        popup = PopupMenu(context, this)
        popup.gravity = Gravity.END
        popup.menuInflater.inflate(R.menu.menu_visibility, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.visibility_visible -> listener?.onValueSet(true)
                R.id.visibility_invisible -> listener?.onValueSet(false)
            }
            true
        }

        item_attribute_value_tv.setOnClickListener {
            popup.show()
        }
    }

    fun setValue(value: Boolean) {
        item_attribute_value_tv.text = if (value) "visible" else "invisible"
    }
}