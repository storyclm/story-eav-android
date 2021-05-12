package expert.rightperception.attributesapp.ui.common

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

abstract class InjectableActivity : AppCompatActivity(), HasAndroidInjector {

    lateinit var context: Activity

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        context = this
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return androidInjector
    }
}
