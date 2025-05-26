import kotlinx.serialization.Serializable

@Serializable
enum class question_option_type {
    input,
    abcd
}
@Serializable
data class questions(
    val id: Int? = null,
    val title: String,
    val option: String,
    val type_option: question_option_type,
)