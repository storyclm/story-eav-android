package expert.rightperception.attributesapp.ui.configurator.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import expert.rightperception.attributesapp.R
import kotlinx.android.synthetic.main.item_nested_attribute.view.*

class NestedAttributeWidget(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    init {
        View.inflate(context, R.layout.item_nested_attribute, this)

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.NestedAttributeWidget, 0, 0)
            item_nested_attribute_key_tv.text = a.getString(R.styleable.NestedAttributeWidget_key)
            a.recycle()
        }
    }

    fun setValue(value: String) {
        item_nested_attribute_value_tv.text = value
    }
}