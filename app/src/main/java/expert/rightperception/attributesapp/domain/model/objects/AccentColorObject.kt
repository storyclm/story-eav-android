package expert.rightperception.attributesapp.domain.model.objects

data class AccentColorObject(
    val accentColorVisible: Boolean,
    val parameters: AccentColorParameters
)

data class AccentColorParameters(
    val color: String,
    val transparent: String
)