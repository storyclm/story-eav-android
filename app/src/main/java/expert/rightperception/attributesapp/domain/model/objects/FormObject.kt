package expert.rightperception.attributesapp.domain.model.objects

data class FormObject(
    val formVisible: Boolean,
    val items: Map<String, FormItem>
)

data class FormItem(
    val name: String?,
    val order: Int?,
    val backgroundColor: String?,
    val fontColor: String?,
    val fontSize: Int?,
    val inputValue: String?
)