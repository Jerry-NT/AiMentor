import kotlinx.serialization.Serializable

@Serializable
data class user_course(
    val id: Int? = null,
    val id_user: Int,
    val id_course: Int,
)