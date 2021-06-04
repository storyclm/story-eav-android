package expert.rightperception.attributesapp.data.repository.signalr.model

data class ReconnectPolicy(
    val numberOfRetries: Int = 3,
    val millisBetweenRetries: Long = 5000,
    val reconnectIntervalMillis: Long = 5 * 60 * 60 * 1000
)
