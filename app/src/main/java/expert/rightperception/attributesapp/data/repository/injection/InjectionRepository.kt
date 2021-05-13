package expert.rightperception.attributesapp.data.repository.injection

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InjectionRepository @Inject constructor() {

    companion object {
        private const val INJECTION_SCRIPT_URL = "https://gist.githubusercontent.com/demkonst/caf5343319272b24116935167ceef579/raw/context.js"
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getInjectionScript(): String? {
        val request: Request = Request.Builder()
            .url(INJECTION_SCRIPT_URL)
            .build()
        return withContext(Dispatchers.IO) {
            val response: Response = OkHttpClient.Builder()
                .build()
                .newCall(request)
                .execute()
            response.body?.string()
        }
    }

}