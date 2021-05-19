package expert.rightperception.attributesapp.di

import android.content.Context
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.Module
import dagger.Provides
import expert.rightperception.attributesapp.App
import expert.rightperception.attributesapp.BuildConfig
import expert.rightperception.attributesapp.data.api.ExpertApi
import expert.rightperception.attributesapp.data.db.AttributesDatabase
import expert.rightperception.attributesapp.data.db.LicenceDao
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.breffi.storyid.auth.common.model.AuthConfig
import ru.breffi.storyid.auth.flow.passwordless.PasswordlessAuthHandler
import ru.breffi.storyid.auth.flow.passwordless.PasswordlessAuthProvider
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
internal class AppModule {

    @Singleton
    @Provides
    fun provideContext(context: App): Context {
        return context
    }

    @Provides
    @Singleton
    internal fun provideAuthProvider(context: Context): PasswordlessAuthProvider {
        val authConfig = AuthConfig(
            BuildConfig.STORY_ID_CONFIG_URL,
            BuildConfig.STORY_ID_CLIENT_ID,
            BuildConfig.STORY_ID_CLIENT_SECRET,
            "nopass_auth_data_storage"
        )
        return PasswordlessAuthProvider(context, authConfig)
    }

    @Provides
    @Singleton
    internal fun provideAuthFlows(authProvider: PasswordlessAuthProvider): PasswordlessAuthHandler {
        return authProvider.getFlowHandler()
    }

    @Provides
    @Singleton
    internal fun provideOkHttpClient(authProvider: PasswordlessAuthProvider): OkHttpClient {
        return authProvider.getFlowClient()
    }

    @Singleton
    @Provides
    internal fun provideExpertService(client: OkHttpClient): ExpertApi {
        val gson = Gson()
        val retrofit = Retrofit.Builder()
            .baseUrl(ExpertApi.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()

        return retrofit.create(ExpertApi::class.java)
    }

    @Singleton
    @Provides
    fun provideDatabase(context: Context): AttributesDatabase {
        return AttributesDatabase.build(context)
    }

    @Singleton
    @Provides
    internal fun provideLicenceDao(db: AttributesDatabase): LicenceDao {
        return db.licenceDao()
    }
}