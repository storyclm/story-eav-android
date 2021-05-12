package expert.rightperception.attributesapp.ui.main

import android.os.Bundle
import androidx.core.view.isVisible
import expert.rightperception.attributesapp.R
import expert.rightperception.attributesapp.ui.common.InjectableActivity
import expert.rightperception.attributesapp.ui.main.model.Content
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

        viewModel.uiStateLiveData.observe(this, { uiState ->
            main_content_layout.isVisible = uiState is Content
            main_loading_layout.isVisible = uiState == Loading
            main_error_layout.isVisible = uiState == Error
        })

        viewModel.start()
    }
}