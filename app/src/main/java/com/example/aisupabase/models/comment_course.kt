import kotlinx.serialization.Serializable

@Serializable
data class comment_course(
    val id: Int? = null,
    val id_user: Int,
    val id_course: Int,
    val content: String,
    val created_at: String? = null,
)
