package expert.rightperception.attributesapp.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import expert.rightperception.attributesapp.ui.configurator.ConfiguratorFragment
import expert.rightperception.attributesapp.ui.configurator_test.ConfiguratorTestFragment
import expert.rightperception.attributesapp.ui.content.ContentFragment

@Module
abstract class FragmentsProvider {

    @ContributesAndroidInjector
    abstract fun contributeContentFragment(): ContentFragment

    @ContributesAndroidInjector
    abstract fun contributeConfiguratorFragment(): ConfiguratorFragment

    @ContributesAndroidInjector
    abstract fun contributeConfiguratorTestFragment(): ConfiguratorTestFragment
}