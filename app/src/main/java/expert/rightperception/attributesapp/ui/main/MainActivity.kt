package expert.rightperception.attributesapp.ui.main

import android.os.Bundle
import androidx.core.view.isVisible
import com.google.android.material.tabs.TabLayoutMediator
import expert.rightperception.attributesapp.R
import expert.rightperception.attributesapp.ui.common.InjectableActivity
import expert.rightperception.attributesapp.ui.main.adapter.MainAdapter
import expert.rightperception.attributesapp.ui.main.model.Data
import expert.rightperception.attributesapp.ui.main.model.Error
import expert.rightperception.attributesapp.ui.main.model.Loading
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : InjectableActivity() {

    @Inject
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_retry_btn.setOnClickListener {
            viewModel.retry()
        }

        viewModel.uiStateLiveData.observe(this, { uiState ->
            main_content_layout.isVisible = uiState is Data
            main_loading_layout.isVisible = uiState == Loading
            main_error_layout.isVisible = uiState == Error

            if (uiState is Data) {
                main_pager.adapter = MainAdapter(this, uiState.licenseModel.id, uiState.contentModel)
                TabLayoutMediator(main_tab_layout, main_pager) { tab, position ->
                    tab.text = getString(if (position == 0) R.string.main_tab_content else R.string.main_tab_configurator)
                }.attach()
            }
        })

        viewModel.setup()
    }
}