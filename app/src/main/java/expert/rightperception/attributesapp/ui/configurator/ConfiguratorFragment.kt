package expert.rightperception.attributesapp.ui.configurator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import expert.rightperception.attributesapp.R
import expert.rightperception.attributesapp.domain.model.objects.ObjectsContainer
import expert.rightperception.attributesapp.ui.common.InjectableFragment
import expert.rightperception.attributesapp.ui.configurator.widget.AttributeWidget
import expert.rightperception.attributesapp.ui.configurator.widget.NestedAttributeWidget
import kotlinx.android.synthetic.main.fragment_configurator.*
import kotlinx.android.synthetic.main.layout_accent_color.*
import kotlinx.android.synthetic.main.layout_notes.*
import kotlinx.android.synthetic.main.layout_rating.*
import javax.inject.Inject

class ConfiguratorFragment : InjectableFragment() {

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_configurator, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString(LICENSE_ID)?.let {  licenseId ->

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
                            objectsContainer.copy(notes = objectsContainer.notes.copy(
                                parameters = objectsContainer.notes.parameters.copy(text = value))
                            )
                        )
                    }
                }
            }
            notes_color_naw.listener = object : NestedAttributeWidget.Listener {
                override fun onValueSet(value: String) {
                    objects?.let { objectsContainer ->
                        renderObjects(
                            objectsContainer.copy(notes = objectsContainer.notes.copy(
                                parameters = objectsContainer.notes.parameters.copy(color = value))
                            )
                        )
                    }
                }
            }
            notes_fontsize_naw.listener = object : NestedAttributeWidget.Listener {
                override fun onValueSet(value: String) {
                    objects?.let { objectsContainer ->
                        renderObjects(
                            objectsContainer.copy(notes = objectsContainer.notes.copy(
                                parameters = objectsContainer.notes.parameters.copy(fontSize = value.toInt()))
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
                            objectsContainer.copy(accentColor = objectsContainer.accentColor.copy(
                                parameters = objectsContainer.accentColor.parameters.copy(color = value))
                            )
                        )
                    }
                }
            }
            accentcolor_transparent_naw.listener = object : NestedAttributeWidget.Listener {
                override fun onValueSet(value: String) {
                    objects?.let { objectsContainer ->
                        renderObjects(
                            objectsContainer.copy(accentColor = objectsContainer.accentColor.copy(
                                parameters = objectsContainer.accentColor.parameters.copy(transparent = value))
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
                            objectsContainer.copy(rating = objectsContainer.rating.copy(
                                parameters = objectsContainer.rating.parameters.copy(color = value))
                            )
                        )
                    }
                }
            }
            rating_quantity_naw.listener = object : NestedAttributeWidget.Listener {
                override fun onValueSet(value: String) {
                    objects?.let { objectsContainer ->
                        renderObjects(
                            objectsContainer.copy(rating = objectsContainer.rating.copy(
                                parameters = objectsContainer.rating.parameters.copy(quantity = value.toInt()))
                            )
                        )
                    }
                }
            }

            viewModel.uiModel.observe(viewLifecycleOwner) { objectsContainer ->
                renderObjects(objectsContainer)
            }

            configurator_save_btn.setOnClickListener {
                objects?.let { viewModel.saveObjects(it) }
            }
        }
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
    }
}