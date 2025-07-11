import kotlinx.serialization.Serializable

@Serializable
enum class question_option_type {
    input,
    abcd
}
@Serializable
data class Question(
    val question: String,
    val A: String,
    val B: String,
    val C: String,
    val D: String,
    val choice: String // Đáp án đúng
)