package expert.rightperception.attributesapp.data.repository.signalr

import android.content.Context
import android.util.Log
import expert.rightperception.attributesapp.data.repository.license.LicenseRepository
import expert.rightperception.attributesapp.data.repository.signalr.model.EventsSubscriber1
import expert.rightperception.attributesapp.domain.model.LicenseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import ru.breffi.storyid.auth.flow.passwordless.PasswordlessAuthHandler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalRRepository @Inject constructor(
    private val context: Context,
    private val licenseRepository: LicenseRepository,
    private val passwordlessAuthHandler: PasswordlessAuthHandler
) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var connector: SignalRConnector? = null

    init {
        scope.launch {
            licenseRepository.observeLicense()
                .filterNotNull()
                .distinctUntilChangedBy { it.id }
                .collect(::createConnector)
        }
    }

    private fun createConnector(license: LicenseModel) {
        val attributesEventSubscriber = EventsSubscriber1("OnAttributeChange", AttributeDto::class.java) { attribute ->
            Log.e("DBG_", "on attrs: ${attribute.parentId} ${attribute.key} ")
        }
        connector = SignalRConnector(
            context,
            "https://api-staging.rightperception.expert/ws/device",
            listOf(license.id),
            listOf(attributesEventSubscriber)
        ) {
            passwordlessAuthHandler.getAuthData()?.accessToken
        }
    }

    data class AttributeDto(
        val id: String,
        val parentId: String,
        val key: String,
        val value: Any?,
        val type: String?,
        val modified: String,
        val deleted: Boolean = false,
        val attributes: List<AttributeDto> = listOf()
    )
}