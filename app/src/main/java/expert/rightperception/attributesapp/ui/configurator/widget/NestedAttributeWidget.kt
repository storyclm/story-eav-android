package expert.rightperception.attributesapp.ui.configurator.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import expert.rightperception.attributesapp.R
import kotlinx.android.synthetic.main.dialog_edit.view.*
import kotlinx.android.synthetic.main.item_nested_attribute.view.*

class NestedAttributeWidget(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    interface Listener {
        fun onValueSet(value: String)
    }

    var listener: Listener? = null

    init {
        View.inflate(context, R.layout.item_nested_attribute, this)

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.NestedAttributeWidget, 0, 0)
            item_nested_attribute_key_tv.text = a.getString(R.styleable.NestedAttributeWidget_key)
            a.recycle()
        }

        item_nested_attribute_value_tv.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit, null)
            val dialog = AlertDialog.Builder(context)
                .setTitle(R.string.dialog_edit_title)
                .setView(dialogView)
                .create()
            dialogView.dialog_edit_et.setText(item_nested_attribute_value_tv.text)
            dialogView.dialog_edit_et.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                }

            })
            dialogView.dialog_edit_cancel_btn.setOnClickListener {
                dialog.dismiss()
            }
            dialogView.dialog_edit_add_btn.setOnClickListener {
                dialog.dismiss()
                listener?.onValueSet(dialogView.dialog_edit_et.text.toString())
            }
            dialog.show()
        }
    }

    fun setValue(value: String) {
        item_nested_attribute_value_tv.text = value
    }
}