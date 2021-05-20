package expert.rightperception.attributesapp.ui.configurator

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import expert.rightperception.attributesapp.R
import expert.rightperception.attributesapp.domain.model.objects.FormItem
import expert.rightperception.attributesapp.domain.model.objects.ObjectsContainer
import expert.rightperception.attributesapp.ui.common.InjectableFragment
import expert.rightperception.attributesapp.ui.common.Utils
import expert.rightperception.attributesapp.ui.configurator.adapter.FormAdapter
import expert.rightperception.attributesapp.ui.configurator.model.FormItemUiModel
import expert.rightperception.attributesapp.ui.configurator.widget.AttributeWidget
import expert.rightperception.attributesapp.ui.configurator.widget.NestedAttributeWidget
import kotlinx.android.synthetic.main.dialog_add_form_item.view.*
import kotlinx.android.synthetic.main.fragment_configurator.*
import kotlinx.android.synthetic.main.layout_accent_color.*
import kotlinx.android.synthetic.main.layout_form.*
import kotlinx.android.synthetic.main.layout_notes.*
import kotlinx.android.synthetic.main.layout_rating.*
import javax.inject.Inject

class ConfiguratorFragment : InjectableFragment(), FormAdapter.Listener {

    companion object {

        private const val LICENSE_ID = "LICENSE_ID"

        fun newInstance(licenseId: String): ConfiguratorFragment {
            return ConfiguratorFragment().apply {
                arguments = Bundle().apply {
                    putString(LICENSE_ID, licenseId)
                }
            }
        }
    }

    @Inject
    lateinit var viewModel: ConfiguratorViewModel

    private var objects: ObjectsContainer? = null
    private var formAdapter: FormAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_configurator, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurator_endpoint_et.setText(viewModel.getAttributesEndpoint())

        formAdapter = FormAdapter(this)
        form_items_rv.layoutManager = LinearLayoutManager(context)
        form_items_rv.adapter = formAdapter

        form_minus_item_btn.setOnClickListener {
            objects?.let { objectsContainer ->
                val size = objectsContainer.form.items.values.size
                if (size > 1) {
                    val key = objectsContainer.form.items.entries
                        .sortedByDescending { it.value.order }[0].key
                    viewModel.deleteFormItem(configurator_endpoint_et.text.toString(), key)
                }
            }
        }
        form_plus_item_btn.setOnClickListener {
            showAddFormItemDialog()
        }

        arguments?.getString(LICENSE_ID)?.let { licenseId ->

            viewModel.setup(licenseId)

            notes_visibility_aw.listener = object : AttributeWidget.Listener {
                override fun onValueSet(value: Boolean) {
                    objects?.let { objectsContainer ->
                        renderObjects(objectsContainer.copy(notes = objectsContainer.notes.copy(notesVisible = value)))
                    }
                }
            }
            notes_text_naw.listener = object : NestedAttributeWidget.Listener {
                override fun onValueSet(value: String) {
                    objects?.let { objectsContainer ->
                        renderObjects(
                            objectsContainer.copy(
                                notes = objectsContainer.notes.copy(
                                    parameters = objectsContainer.notes.parameters.copy(text = value)
                                )
                            )
                        )
                    }
                }
            }
            notes_color_naw.listener = object : NestedAttributeWidget.Listener {
                override fun onValueSet(value: String) {
                    objects?.let { objectsContainer ->
                        renderObjects(
                            objectsContainer.copy(
                                notes = objectsContainer.notes.copy(
                                    parameters = objectsContainer.notes.parameters.copy(color = value)
                                )
                            )
                        )
                    }
                }
            }
            notes_fontsize_naw.listener = object : NestedAttributeWidget.Listener {
                override fun onValueSet(value: String) {
                    objects?.let { objectsContainer ->
                        renderObjects(
                            objectsContainer.copy(
                                notes = objectsContainer.notes.copy(
                                    parameters = objectsContainer.notes.parameters.copy(fontSize = value.toInt())
                                )
                            )
                        )
                    }
                }
            }

            accentcolor_visibility_aw.listener = object : AttributeWidget.Listener {
                override fun onValueSet(value: Boolean) {
                    objects?.let { objectsContainer ->
                        renderObjects(objectsContainer.copy(accentColor = objectsContainer.accentColor.copy(accentColorVisible = value)))
                    }
                }
            }
            accentcolor_color_naw.listener = object : NestedAttributeWidget.Listener {
                override fun onValueSet(value: String) {
                    objects?.let { objectsContainer ->
                        renderObjects(
                            objectsContainer.copy(
                                accentColor = objectsContainer.accentColor.copy(
                                    parameters = objectsContainer.accentColor.parameters.copy(color = value)
                                )
                            )
                        )
                    }
                }
            }
            accentcolor_transparent_naw.listener = object : NestedAttributeWidget.Listener {
                override fun onValueSet(value: String) {
                    objects?.let { objectsContainer ->
                        renderObjects(
                            objectsContainer.copy(
                                accentColor = objectsContainer.accentColor.copy(
                                    parameters = objectsContainer.accentColor.parameters.copy(transparent = value)
                                )
                            )
                        )
                    }
                }
            }

            rating_visibility_aw.listener = object : AttributeWidget.Listener {
                override fun onValueSet(value: Boolean) {
                    objects?.let { objectsContainer ->
                        renderObjects(objectsContainer.copy(rating = objectsContainer.rating.copy(ratingVisible = value)))
                    }
                }
            }
            rating_color_naw.listener = object : NestedAttributeWidget.Listener {
                override fun onValueSet(value: String) {
                    objects?.let { objectsContainer ->
                        renderObjects(
                            objectsContainer.copy(
                                rating = objectsContainer.rating.copy(
                                    parameters = objectsContainer.rating.parameters.copy(color = value)
                                )
                            )
                        )
                    }
                }
            }
            rating_quantity_naw.listener = object : NestedAttributeWidget.Listener {
                override fun onValueSet(value: String) {
                    objects?.let { objectsContainer ->
                        renderObjects(
                            objectsContainer.copy(
                                rating = objectsContainer.rating.copy(
                                    parameters = objectsContainer.rating.parameters.copy(quantity = value.toInt())
                                )
                            )
                        )
                    }
                }
            }
            form_visibility_aw.listener = object : AttributeWidget.Listener {
                override fun onValueSet(value: Boolean) {
                    objects?.let { objectsContainer ->
                        renderObjects(objectsContainer.copy(form = objectsContainer.form.copy(formVisible = value)))
                    }
                }
            }

            viewModel.uiModel.observe(viewLifecycleOwner) { objectsContainer ->
                renderObjects(objectsContainer)
            }

            configurator_save_btn.setOnClickListener {
                objects?.let { viewModel.saveObjects(configurator_endpoint_et.text.toString(), it) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        formAdapter = null
    }

    fun renderObjects(objectsContainer: ObjectsContainer) {
        objects = objectsContainer
        notes_visibility_aw.setValue(objectsContainer.notes.notesVisible)
        notes_color_naw.setValue(objectsContainer.notes.parameters.color)
        notes_text_naw.setValue(objectsContainer.notes.parameters.text)
        notes_fontsize_naw.setValue(objectsContainer.notes.parameters.fontSize.toString())

        accentcolor_visibility_aw.setValue(objectsContainer.accentColor.accentColorVisible)
        accentcolor_color_naw.setValue(objectsContainer.accentColor.parameters.color)
        accentcolor_transparent_naw.setValue(objectsContainer.accentColor.parameters.transparent)

        rating_visibility_aw.setValue(objectsContainer.rating.ratingVisible)
        rating_color_naw.setValue(objectsContainer.rating.parameters.color)
        rating_quantity_naw.setValue(objectsContainer.rating.parameters.quantity.toString())
        rating_progress_naw.setValue(objectsContainer.rating.parameters.progress.toString())

        form_visibility_aw.setValue(objectsContainer.form.formVisible)

        val items = objectsContainer.form.items.entries
            .sortedBy { it.value.order }
            .map { (key, formItem) ->
                FormItemUiModel(
                    key = key,
                    name = formItem.name,
                    backgroundColor = formItem.backgroundColor,
                    fontColor = formItem.fontColor,
                    fontSize = formItem.fontSize,
                    inputValue = formItem.inputValue
                )
            }
        form_minus_item_btn.isEnabled = items.size > 1
        formAdapter?.setData(items)
    }

    override fun onValueChange(uiModel: FormItemUiModel) {
        objects?.let { objectsContainer ->
            objectsContainer.form.items[uiModel.key]?.copy(
                name = uiModel.name,
                backgroundColor = uiModel.backgroundColor,
                fontColor = uiModel.fontColor,
                fontSize = uiModel.fontSize
            )?.let { updatedItem ->
                val updatedMap = objectsContainer.form.items.plus(uiModel.key to updatedItem)
                renderObjects(
                    objectsContainer.copy(form = objectsContainer.form.copy(items = updatedMap))
                )
            }
        }
    }

    private fun showAddFormItemDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_form_item, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_form_add_item)
            .setView(dialogView)
            .create()
        val nameFilterArray = arrayOfNulls<InputFilter>(1)
        nameFilterArray[0] = InputFilter.LengthFilter(100)
        dialogView.dialog_form_add_name_et.filters = nameFilterArray

        dialogView.dialog_form_add_font_size_et.inputType = InputType.TYPE_CLASS_NUMBER
        val fontSizeFilterArray = arrayOfNulls<InputFilter>(1)
        fontSizeFilterArray[0] = InputFilter.LengthFilter(2)
        dialogView.dialog_form_add_font_size_et.filters = fontSizeFilterArray

        fun updateAddButton() {
            dialogView.dialog_form_add_add_btn.isEnabled =
                Utils.colorRegex.matches(dialogView.dialog_form_add_bg_color_et.text.toString())
                        && Utils.colorRegex.matches(dialogView.dialog_form_add_font_color_et.text.toString())
                        && dialogView.dialog_form_add_font_size_et.text.toString().isNotEmpty()
        }
        dialogView.dialog_form_add_bg_color_et.addTextChangedListener {
            updateAddButton()
        }
        dialogView.dialog_form_add_font_color_et.addTextChangedListener {
            updateAddButton()
        }
        dialogView.dialog_form_add_font_size_et.addTextChangedListener {
            updateAddButton()
        }

        dialogView.dialog_form_add_cancel_btn.setOnClickListener {
            dialog.dismiss()
        }
        dialogView.dialog_form_add_add_btn.setOnClickListener {
            dialog.dismiss()
            objects?.let { objectsContainer ->
                val order = objectsContainer.form.items.values
                    .sortedByDescending { it.order }[0].order + 1
                val key = "input_${order}"
                val newFormItem = FormItem(
                    order = order,
                    name = dialogView.dialog_form_add_name_et.text.toString(),
                    backgroundColor = dialogView.dialog_form_add_bg_color_et.text.toString(),
                    fontColor = dialogView.dialog_form_add_font_color_et.text.toString(),
                    fontSize = dialogView.dialog_form_add_font_size_et.text.toString().toInt(),
                    inputValue = ""
                )
                val updatedMap = objectsContainer.form.items.plus(key to newFormItem)
                renderObjects(
                    objectsContainer.copy(form = objectsContainer.form.copy(items = updatedMap))
                )
            }
        }
        dialog.show()
    }
}