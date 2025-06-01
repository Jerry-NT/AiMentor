import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class  InvoiceResult<out T> {
    data class Success<T>(val data: T?, val raw: PostgrestResult? = null) : InvoiceResult<T>()
    data class Error(val exception: Exception, val raw: PostgrestResult? = null) : InvoiceResult<Nothing>()
}
// 2. Repository
class invoicesRepository(private val supabase: SupabaseClient) {

    suspend fun getInvoice(): InvoiceResult<List<invoices>> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["invoices"].select()
            val invoice = result.decodeList<invoices>()
            return@withContext InvoiceResult.Success(invoice, result)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext InvoiceResult.Error(e)
        }
    }
}