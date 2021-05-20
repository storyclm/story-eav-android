package expert.rightperception.attributesapp.domain.model.objects

data class ObjectsContainer(
    val notes: NotesObject = NotesObject(
        notesVisible = true,
        parameters = NotesParameters(
            text = "Hello world!",
            color = "#000000",
            fontSize = 12
        )
    ),
    val accentColor: AccentColorObject = AccentColorObject(
        accentColorVisible = true,
        parameters = AccentColorParameters(
            color = "#000000",
            transparent = "25"
        )
    ),
    val rating: RatingObject = RatingObject(
        ratingVisible = true,
        parameters = RatingParameters(
            color = "#000000",
            quantity = 1,
            progress = 10
        )
    ),
    val form: FormObject = FormObject(
        formVisible = true,
        items = mapOf(
            "input_1" to FormItem(
                name = "input name",
                order = 1,
                backgroundColor = "#FFFFFF",
                fontColor = "#000000",
                fontSize = 15,
                inputValue = "some value"
            )
        )
    )
)
