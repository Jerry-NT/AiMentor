import kotlinx.serialization.Serializable

@Serializable
data class lessons(
    val id: Int? = null,
    val id_course: Int,
    val title_lesson: String,
    val content_lesson: String,
    val duration: Int,
)