package expert.rightperception.attributesapp.data.repository.signalr

import android.content.Context
import expert.rightperception.attributesapp.data.repository.license.LicenseRepository
import expert.rightperception.attributesapp.data.repository.signalr.model.EventsSubscriber1
import expert.rightperception.attributesapp.data.repository.story_object.AttributesServiceRepository
import expert.rightperception.attributesapp.domain.model.LicenseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import ru.breffi.storyid.auth.flow.passwordless.PasswordlessAuthHandler
import ru.rightperception.storyattributes.remote.model.AttributeDto
import ru.rightperception.storyattributes.utility.toFlatList
import ru.rightperception.storyattributes.utility.toStructuredList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalRRepository @Inject constructor(
    private val context: Context,
    private val licenseRepository: LicenseRepository,
    private val passwordlessAuthHandler: PasswordlessAuthHandler,
    private val attributesServiceRepository: AttributesServiceRepository
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
            try {
                attributesServiceRepository
                    .getActiveService()
                    .getSynchronizationApi()
                    .postApplyRemoteTask(listOf(attribute).withMappedType())
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

    private fun AttributeDto.withMappedType(): AttributeDto {
        val mappedType = when (type) {
            "0" -> "string"
            "1" -> "integer"
            "2" -> "boolean"
            "3" -> "float"
            null -> null
            else -> throw RuntimeException("Could not map $type")
        }
        return copy(type = mappedType)
    }

    private fun List<AttributeDto>.withMappedType(): List<AttributeDto> {
        return toFlatList().map { it.withMappedType() }.toStructuredList()
    }
}