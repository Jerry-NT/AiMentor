import kotlinx.serialization.Serializable

@Serializable
data class invoices(
    val id: Int? = null,
    val id_user: Int,
    val amount: Double,
    val status: String,
    val transaction_date:  String,
)