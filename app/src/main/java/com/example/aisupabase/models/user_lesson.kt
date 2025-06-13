import kotlinx.serialization.Serializable

@Serializable
data class user_lesson(
    val id: Int? = null,
    val id_user: Int,
    val id_lesson: Int,
    val checked: Boolean,
)