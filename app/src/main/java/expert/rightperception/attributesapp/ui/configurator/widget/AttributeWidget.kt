package expert.rightperception.attributesapp.ui.configurator.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import expert.rightperception.attributesapp.R
import kotlinx.android.synthetic.main.item_attribute.view.*

class AttributeWidget(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    init {
        View.inflate(context, R.layout.item_attribute, this)

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.AttributeWidget, 0, 0)
            item_attribute_key_tv.text = a.getString(R.styleable.AttributeWidget_key)
            a.recycle()
        }
    }

    fun setValue(value: Boolean) {
        item_attribute_value_tv.text = if (value) "visible" else "invisible"
    }
}