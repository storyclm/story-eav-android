package expert.rightperception.attributesapp

import android.app.Application
import com.orhanobut.hawk.Hawk
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import expert.rightperception.attributesapp.data.repository.signalr.SignalRRepository
import expert.rightperception.attributesapp.di.DaggerAppComponent
import ru.breffi.story.StoryContent
import ru.breffi.story.di.StoryContentConfiguration
import ru.breffi.story.domain.models.AppInfoEntity
import javax.inject.Inject

class App : Application(), HasAndroidInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var signalRRepository: SignalRRepository

    override fun onCreate() {
        super.onCreate()

        DaggerAppComponent.builder()
            .application(this)
            .build()
            .inject(this)

        Hawk.init(this).build()

        val appInfo = AppInfoEntity(
            "StoryAttributes",
            BuildConfig.VERSION_NAME
        )
        val storyConfig = StoryContentConfiguration(
            context = applicationContext,
            authApiUrl = BuildConfig.STORY_CONTENT_AUTH_ENDPOINT,
            contentApiUrl = BuildConfig.STORY_CONTENT_API_ENDPOINT,
            appInfo = appInfo,
            storyRealmFilename = "story.realm"
        )
        StoryContent.init(storyConfig)
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }
}