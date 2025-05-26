import kotlinx.serialization.Serializable

@Serializable
data class blogs(
    val id: Int? = null,
    val title_blog:String,
    val public_id_image: String,
    val url_image: String,
    val id_tag: Int,
    val content_blog: String,
    val created_at: String? = null,
)