package expert.rightperception.attributesapp.domain.model.objects

data class AccentColorContainer(
    val accentColor: AccentColorObject
)

data class AccentColorObject(
    val accentColorVisible: Boolean,
    val parameters: AccentColorParameters
)

data class AccentColorParameters(
    val color: String,
    val transparency: String
)