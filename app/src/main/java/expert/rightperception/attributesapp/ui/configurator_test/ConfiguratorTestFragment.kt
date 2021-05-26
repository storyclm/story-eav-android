package expert.rightperception.attributesapp.ui.configurator_test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import expert.rightperception.attributesapp.R
import expert.rightperception.attributesapp.ui.common.InjectableFragment
import kotlinx.android.synthetic.main.fragment_configurator_test.*
import javax.inject.Inject

class ConfiguratorTestFragment : InjectableFragment() {

    @Inject
    lateinit var viewModel: ConfiguratorTestViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_configurator_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.testObject.observe(viewLifecycleOwner) { jsonObject ->
            val objectString = jsonObject.toString()
            if (configurator_et.text.toString() != objectString) {
                configurator_et.setText(objectString)
            }
        }

        configurator_save_btn.setOnClickListener {
            viewModel.saveTestObject(configurator_et.text.toString())
        }
    }
}