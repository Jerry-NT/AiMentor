import com.example.aisupabase.models.Users
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ket qua va bug crud user
sealed class UserResult<out T> {
    data class Success<T>(val data: T?, val raw: PostgrestResult? = null) : UserResult<T>()
    data class Error(val exception: Exception, val raw: PostgrestResult? = null) : UserResult<Nothing>()
}
// 2. Repository
 class UserRepository(private val supabase: SupabaseClient) {
    // Lấy danh sách users
    suspend fun getUsers(): UserResult<List<Users>> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["users"].select()
            val users = result.decodeList<Users>()
            return@withContext UserResult.Success(users, result)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext UserResult.Error(e)
        }
    }
}