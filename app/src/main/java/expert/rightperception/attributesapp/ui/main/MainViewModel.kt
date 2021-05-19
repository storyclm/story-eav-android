package expert.rightperception.attributesapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import expert.rightperception.attributesapp.data.repository.license.LicenseRepository
import expert.rightperception.attributesapp.domain.interactor.ContentInteractor
import expert.rightperception.attributesapp.ui.main.model.Data
import expert.rightperception.attributesapp.ui.main.model.Error
import expert.rightperception.attributesapp.ui.main.model.Loading
import expert.rightperception.attributesapp.ui.main.model.MainUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.breffi.storyid.auth.common.model.AuthError
import ru.breffi.storyid.auth.common.model.AuthSuccess
import ru.breffi.storyid.auth.flow.passwordless.PasswordlessAuthHandler
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val passwordlessAuthHandler: PasswordlessAuthHandler,
    private val contentInteractor: ContentInteractor,
    private val licenseRepository: LicenseRepository
) : ViewModel() {

    private val uiState = MutableStateFlow<MainUiState>(Loading)
    val uiStateLiveData = uiState
        .asStateFlow()
        .asLiveData()

    fun setup() {
        viewModelScope.launch {
            if (passwordlessAuthHandler.isAuthenticated()) {
                uiState.value = getData()
            } else {
                withContext(Dispatchers.IO) {
                    uiState.value = when (passwordlessAuthHandler.passwordlessAuth("70000000001")) {
                        is AuthSuccess -> {
                            when (passwordlessAuthHandler.passwordlessProceedWithCode("0000")) {
                                AuthSuccess -> getData()
                                is AuthError -> Error
                            }
                        }
                        is AuthError -> Error
                    }
                }
            }
        }
    }

    fun retry() {
        uiState.value = Loading
        setup()
    }

    private suspend fun getData(): MainUiState  {
        val license = licenseRepository.getLicense()
        return if (license != null) {
            val content = contentInteractor.getContent()
            if (content != null) {
                Data(license, content)
            } else{
                Error
            }
        } else {
            Error
        }
    }
}