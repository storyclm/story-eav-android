package expert.rightperception.attributesapp.data.repository.signalr.model

data class ReconnectPolicy(
    val numberOfRetries: Long = 3,
    val millisBetweenRetries: Long = 5000,
    val reconnectIntervalMillis: Long = 1 * 60 * 1000
)
