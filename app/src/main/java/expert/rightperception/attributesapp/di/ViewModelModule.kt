package expert.rightperception.attributesapp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import expert.rightperception.attributesapp.ui.configurator.ConfiguratorViewModel
import expert.rightperception.attributesapp.ui.content.ContentViewModel
import expert.rightperception.attributesapp.ui.main.MainViewModel

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(viewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ContentViewModel::class)
    abstract fun bindContentViewModel(viewModel: ContentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConfiguratorViewModel::class)
    abstract fun bindConfiguratorViewModel(viewModel: ConfiguratorViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

}