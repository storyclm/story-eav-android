package expert.rightperception.attributesapp.data.repository.injection

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InjectionRepository @Inject constructor(context: Context) {

    companion object {
        private const val INJECTION_SCRIPT_URL = "https://pastebin.com/raw/V5FfUV2m"
    }

    private val scriptFile = File(context.filesDir.absolutePath, "script.js")

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getInjectionScript(): String? {
        val request: Request = Request.Builder()
            .url(INJECTION_SCRIPT_URL)
            .build()
        return withContext(Dispatchers.IO) {
            val response = try {
                 OkHttpClient.Builder()
                    .build()
                    .newCall(request)
                    .execute()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            when {
                response?.isSuccessful == true -> {
                    val scriptText = response.body?.string() ?: ""
                    if (!scriptFile.exists()) {
                        scriptFile.createNewFile()
                    }
                    scriptFile.writeText(scriptText)
                    scriptText
                }
                scriptFile.exists() -> scriptFile.readText()
                else -> null
            }
        }
    }

}