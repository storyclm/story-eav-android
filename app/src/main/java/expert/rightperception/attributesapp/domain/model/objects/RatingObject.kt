package expert.rightperception.attributesapp.domain.model.objects

data class RatingContainer(
    val rating: RatingObject
)

data class RatingObject(
    val ratingVisible: Boolean,
    val ratingParameters: RatingParameters
)

data class RatingParameters(
    val color: String,
    val quantity: Int,
    val progress: Int
)