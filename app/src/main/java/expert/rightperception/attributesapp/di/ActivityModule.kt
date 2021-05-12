package expert.rightperception.attributesapp.di


import dagger.Module
import dagger.android.ContributesAndroidInjector
import expert.rightperception.attributesapp.ui.main.MainActivity

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity
}