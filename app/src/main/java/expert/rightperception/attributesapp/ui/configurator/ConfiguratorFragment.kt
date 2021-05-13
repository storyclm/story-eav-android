package expert.rightperception.attributesapp.ui.configurator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import expert.rightperception.attributesapp.R
import expert.rightperception.attributesapp.ui.common.InjectableFragment
import kotlinx.android.synthetic.main.fragment_configurator.*
import javax.inject.Inject

class ConfiguratorFragment : InjectableFragment() {

    @Inject
    lateinit var viewModel: ConfiguratorViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_configurator, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.objectString.observe(viewLifecycleOwner) { objectString ->
            if (configurator_et.text.toString() != objectString) {
                configurator_et.setText(objectString)
            }
        }

        configurator_save_btn.setOnClickListener {
            viewModel.saveObject(configurator_et.text.toString())
        }
    }
}