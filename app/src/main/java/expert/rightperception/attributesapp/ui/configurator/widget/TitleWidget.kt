package expert.rightperception.attributesapp.ui.configurator.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import expert.rightperception.attributesapp.R
import kotlinx.android.synthetic.main.item_title.view.*

class TitleWidget(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    init {
        View.inflate(context, R.layout.item_title, this)

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.TitleWidget, 0, 0)
            item_title_tv.text = a.getString(R.styleable.TitleWidget_title)
            a.recycle()
        }
    }

    fun setTitle(title: String) {
        item_title_tv.text = title
    }
}