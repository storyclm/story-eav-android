package expert.rightperception.attributesapp.domain.model.objects

data class RatingObject(
    val ratingVisible: Boolean,
    val parameters: RatingParameters
)

data class RatingParameters(
    val color: String,
    val quantity: Int,
    val progress: Int
)