import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schlaftagebuch_vers_1.api.ApiClient
import com.example.schlaftagebuch_vers_1.api.JwtUtils
import com.example.schlaftagebuch_vers_1.api.Session
import com.example.schlaftagebuch_vers_1.api.auth.PatientFirstLoginRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class LogInUiState(
    val loading: Boolean = false,
    val error: String? = null
)

class LogInViewModel : ViewModel() {

    var uiState by mutableStateOf(LogInUiState())
        private set

    fun firstLogin(
        consentAccepted: Boolean,
        code: String,
        givenName: String,
        familyName: String,
        birthDate: String,
        password: String,
        onSuccess: (jwt: String, username: String?) -> Unit
    ) {
        uiState = LogInUiState(loading = true)

        viewModelScope.launch {
            try {
                val response = ApiClient.authApi.patientFirstLogin(
                    PatientFirstLoginRequest(
                        code = code.trim(),
                        consentAccepted = consentAccepted,
                        givenName = givenName.trim(),
                        familyName = familyName.trim(),
                        birthDate = birthDateToIso(birthDate),
                        password = password
                    )
                )

                val jwt = response.token
                Session.jwt = jwt
                val username = JwtUtils.extractUsername(jwt)

                uiState = LogInUiState(loading = false)
                onSuccess(jwt, username)

            } catch (e: HttpException) {
                uiState = LogInUiState(
                    loading = false,
                    error = "HTTP ${e.code()}: ${e.message()}"
                )
            } catch (e: Exception) {
                uiState = LogInUiState(
                    loading = false,
                    error = e.message ?: "Unbekannter Fehler"
                )
            }
        }
    }
}

private fun birthDateToIso(input: String): String {
    val parts = input.split(".")
    val day = parts[0]
    val month = parts[1]
    val year = parts[2]
    return "$year-$month-$day" // YYYY-MM-DD
}
