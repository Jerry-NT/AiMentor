package com.example.aisupabase.config
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONArray
import org.json.JSONObject

object PaymentsUtil {

    // Môi trường test - chuyển sang PRODUCTION khi đi live
    val PAYMENTS_ENVIRONMENT = WalletConstants.ENVIRONMENT_TEST

    private val baseRequest = JSONObject().apply {
        put("apiVersion", 2)
        put("apiVersionMinor", 0)
    }

    // Cấu hình test - Google cung cấp gateway test miễn phí
    private val gatewayTokenizationSpecification: JSONObject =
        JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put("parameters", JSONObject().apply {
                put("gateway", "example") // Gateway test của Google
                put("gatewayMerchantId", "exampleGatewayMerchantId") // Test merchant ID
            })
        }

    private val directTokenizationSpecification: JSONObject =
        JSONObject().apply {
            put("type", "DIRECT")
            put("parameters", JSONObject().apply {
                put("protocolVersion", "ECv2")
                put("publicKey", "your_public_key_here")
            })
        }

    fun createPaymentsClient(context: android.content.Context): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(PAYMENTS_ENVIRONMENT)
            .build()

        return Wallet.getPaymentsClient(context, walletOptions)
    }

    fun getBaseCardPaymentMethod(): JSONObject {
        return JSONObject().apply {
            val cardPaymentMethod = JSONObject().apply {
                put("type", "CARD")
                put("tokenizationSpecification", gatewayTokenizationSpecification)
                put("parameters", JSONObject().apply {
                    put("allowedCardNetworks", JSONArray().apply {
                        put("VISA")
                        put("MASTERCARD")
                    })
                    put("allowedAuthMethods", JSONArray().apply {
                        put("PAN_ONLY")
                        put("CRYPTOGRAM_3DS")
                    })
                    put("billingAddressRequired", true)
                    put("billingAddressParameters", JSONObject().apply {
                        put("format", "FULL")
                    })
                })
            }
            put("type", "CARD")
            put("parameters", cardPaymentMethod.getJSONObject("parameters"))
            put("tokenizationSpecification", cardPaymentMethod.getJSONObject("tokenizationSpecification"))
        }
    }

    fun getIsReadyToPayRequest(): JSONObject {
        return JSONObject(baseRequest.toString()).apply {
            put("allowedPaymentMethods", JSONArray().put(getBaseCardPaymentMethod()))
        }
    }

    fun getPaymentDataRequest(priceCents: Long): JSONObject {
        return JSONObject(baseRequest.toString()).apply {
            put("allowedPaymentMethods", JSONArray().put(getBaseCardPaymentMethod()))
            put("transactionInfo", JSONObject().apply {
                put("totalPrice", (priceCents / 100.0).toString())
                put("totalPriceStatus", "FINAL")
                put("currencyCode", "VND")
            })
            put("merchantInfo", JSONObject().apply {
                put("merchantName", "Test Merchant")
                // Không cần merchantId cho test environment
            })
            put("shippingAddressRequired", false)
            put("shippingAddressParameters", JSONObject().apply {
                put("phoneNumberRequired", false)
                put("allowedCountryCodes", JSONArray().put("VN"))
            })
        }
    }
}