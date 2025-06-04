import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class QuestionResult<out T> {
    data class Success<T>(val data: T?, val raw: PostgrestResult? = null) : QuestionResult<T>()
    data class Error(val exception: Exception, val raw: PostgrestResult? = null) : QuestionResult<Nothing>()
}
class questionRepositon(private val supabase: SupabaseClient) {

    // Lấy danh sách câu hỏi
    suspend fun getQuestions(): QuestionResult<List<questions>> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["questions"].select()
            val questionsList = result.decodeList<questions>()
            return@withContext QuestionResult.Success(questionsList, result)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext QuestionResult.Error(e)
        }
    }

    //add câu hỏi mới
    suspend fun  addQuestion(
        title: String,
        option: String,
        type_option: question_option_type
    ): QuestionResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["questions"]
                .insert(
                    questions(null, title, option, type_option)
                )
            return@withContext QuestionResult.Success(Unit, result)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext QuestionResult.Error(e)
        }
    }
    // Xóa câu hỏi theo id
    suspend fun deleteQuestion(id: Int): QuestionResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["questions"]
                .delete {
                    filter { eq("id", id) }
                }
            return@withContext QuestionResult.Success(Unit, result)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext QuestionResult.Error(e)
        }
    }

    // Cập nhật câu hỏi theo id
    suspend fun updateQuestion(
        id: Int,
        title: String,
        option: String,
        type_option: question_option_type
    ): QuestionResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val data = questions(id, title, option, type_option)
            val result = supabase.postgrest["questions"].update(data) {
                filter { eq("id", id) }
            }
            return@withContext QuestionResult.Success(Unit, result)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext QuestionResult.Error(e)
        }
    }
}