import kotlinx.serialization.Serializable

@Serializable
data class courses(
    val id: Int? = null,
    val title_course: String,
    val des_course: String,
    val public_id_image: String,
    val url_image: String,
    val is_private: Boolean,
    val user_create: Int, // users
    val created_at: String? = null,
)