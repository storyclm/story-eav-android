package expert.rightperception.attributesapp.domain.model.objects

data class NotesContainer(
    val notes: NotesObject
)

data class NotesObject(
    val notesVisible: Boolean,
    val parameters: NotesParameters
)

data class NotesParameters(
    val text: String,
    val color: String,
    val fontSize: String
)