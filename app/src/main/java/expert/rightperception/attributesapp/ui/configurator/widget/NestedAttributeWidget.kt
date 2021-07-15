package expert.rightperception.attributesapp.ui.configurator.widget

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.redmadrobot.inputmask.MaskedTextChangedListener.Companion.installOn
import expert.rightperception.attributesapp.R
import expert.rightperception.attributesapp.ui.common.Utils
import kotlinx.android.synthetic.main.dialog_edit.view.*
import kotlinx.android.synthetic.main.item_nested_attribute.view.*


class NestedAttributeWidget(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    interface Listener {
        fun onValueSet(value: String)
    }

    var listener: Listener? = null
    var locked = false

    private var valueType: Int = 0

    init {
        View.inflate(context, R.layout.item_nested_attribute, this)

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.NestedAttributeWidget, 0, 0)
            item_nested_attribute_key_tv.text = a.getString(R.styleable.NestedAttributeWidget_key)
            valueType = a.getInt(R.styleable.NestedAttributeWidget_value_type, 0)
            locked = a.getBoolean(R.styleable.NestedAttributeWidget_locked, false)
            a.recycle()
        }

        if (locked) {
            item_nested_attribute_value_tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_lock_closed, 0)
            item_nested_attribute_value_tv.setBackgroundResource(R.drawable.bg_item_attr_value_locked)
            item_nested_attribute_value_tv.setTextColor(context.getColor(R.color.dark_grey))
        } else {
            item_nested_attribute_value_tv.setOnClickListener {
                val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit, null)
                val dialog = AlertDialog.Builder(context)
                    .setTitle(R.string.dialog_edit_title)
                    .setView(dialogView)
                    .create()
                when (valueType) {
                    0 -> {
                        val filterArray = arrayOfNulls<InputFilter>(1)
                        filterArray[0] = InputFilter.LengthFilter(100)
                        dialogView.dialog_edit_et.filters = filterArray
                    }
                    1 -> {
                        installOn(
                            dialogView.dialog_edit_et,
                            "#[______]",
                            object : MaskedTextChangedListener.ValueListener {
                                override fun onTextChanged(maskFilled: Boolean, extractedValue: String, formattedValue: String) {
                                    dialogView.dialog_edit_add_btn.isEnabled = Utils.colorRegex.matches(formattedValue)
                                }
                            }
                        )
                        dialogView.dialog_edit_et.inputType = InputType.TYPE_CLASS_TEXT
                        dialogView.dialog_edit_et.hint = "#ffffff"
                        val filterArray = arrayOfNulls<InputFilter>(1)
                        filterArray[0] = InputFilter { source, start, end, dest, dstart, dend ->
                            if (source.any { it !in "#0123456789abcdefABCDEF" }) {
                                ""
                            } else {
                                null
                            }
                        }
                        dialogView.dialog_edit_et.filters = filterArray
                    }
                    2 -> {
                        dialogView.dialog_edit_et.inputType = InputType.TYPE_CLASS_NUMBER
                        val filterArray = arrayOfNulls<InputFilter>(1)
                        filterArray[0] = InputFilter.LengthFilter(2)
                        dialogView.dialog_edit_et.filters = filterArray
                        dialogView.dialog_edit_et.addTextChangedListener {
                            dialogView.dialog_edit_add_btn.isEnabled = it.toString().isNotEmpty()
                        }
                    }
                }
                dialogView.dialog_edit_et.setText(item_nested_attribute_value_tv.text)
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
    }

    fun setKey(key: String) {
        item_nested_attribute_key_tv.text = key
    }

    fun setValue(value: String) {
        item_nested_attribute_value_tv.text = value
    }
}