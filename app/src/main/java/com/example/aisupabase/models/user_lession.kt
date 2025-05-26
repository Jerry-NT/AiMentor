import kotlinx.serialization.Serializable

@Serializable
data class user_lession(
    val id: Int? = null,
    val id_user: Int,
    val id_lession: Int,
    val checked: Boolean,
)