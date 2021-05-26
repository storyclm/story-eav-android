package expert.rightperception.attributesapp.data.repository.story_object

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesStorage @Inject constructor(context: Context) {

    companion object {
        const val DEFAULT_ATTRIBUTES_ENDPOINT = "https://api-staging.rightperception.expert/"

        private const val KEY_ATTRIBUTES_ENDPOINT = "KEY_ENDPOINT"
    }

    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun setAttributesEndpoint(endpoint: String) {
        prefs.edit().putString(KEY_ATTRIBUTES_ENDPOINT, endpoint).apply()
    }

    fun getAttributesEndpoint(): String {
        return prefs.getString(KEY_ATTRIBUTES_ENDPOINT, DEFAULT_ATTRIBUTES_ENDPOINT) as String
    }
}