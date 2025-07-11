import kotlinx.serialization.Serializable

@Serializable
data class lessons(
    val id: Int? = null,
    val id_course: Int,
    val title_lesson: String,
    val content_lesson: String,
    val duration: Int,
    val public_id_image: String,
    val url_image: String,
    val practice_questions:String
)