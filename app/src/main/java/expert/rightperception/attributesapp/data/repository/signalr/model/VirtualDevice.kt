package expert.rightperception.attributesapp.data.repository.signalr.model

data class VirtualDevice(
        val deviceId: String,
        val os: Int,
        val osVersion: String,
        val app: String,
        val appVersion: String,
        val language: String,
        val model: String,
        val timeZone: Int,
        val theme: String,
        val subscriptions: List<String>
)