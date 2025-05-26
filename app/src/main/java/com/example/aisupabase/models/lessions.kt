import kotlinx.serialization.Serializable

@Serializable
data class lessions(
    val id: Int? = null,
    val id_course: Int,
    val title_lession: String,
    val content_lession: String,
    val duration: String,

)