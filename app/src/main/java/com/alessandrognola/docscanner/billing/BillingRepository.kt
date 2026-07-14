package com.alessandrognola.docscanner.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Sblocco da 1 mese senza limiti, acquisto singolo (non abbonamento) — configurare su Play Console come prodotto in-app. */
const val PRODUCT_MONTH = "unlock_month"

/** Sblocco da 1 anno senza limiti, acquisto singolo — configurare su Play Console come prodotto in-app. */
const val PRODUCT_YEAR = "unlock_year"

class BillingRepository(
    context: Context,
    private val scope: CoroutineScope,
    private val onPurchaseCompleted: suspend (productId: String) -> Unit
) {
    private val productDetailsMap = mutableMapOf<String, ProductDetails>()

    private val purchasesListener = PurchasesUpdatedListener { result, purchases ->
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { handlePurchase(it) }
        }
    }

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesListener)
        .enablePendingPurchases()
        .build()

    fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    loadProductDetails()
                    restorePendingPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // BillingClient riproverà la connessione alla prossima richiesta
            }
        })
    }

    private fun loadProductDetails() {
        val products = listOf(PRODUCT_MONTH, PRODUCT_YEAR).map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(products)
            .build()
        billingClient.queryProductDetailsAsync(params) { _, result ->
            result.forEach { details -> productDetailsMap[details.productId] = details }
        }
    }

    fun launchPurchaseFlow(activity: Activity, productId: String) {
        val details = productDetailsMap[productId] ?: return
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        billingClient.launchBillingFlow(activity, flowParams)
    }

    /** Recupera acquisti pagati ma non ancora consumati (es. app chiusa a metà flusso). */
    private fun restorePendingPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(params) { _, purchases ->
            purchases.forEach { handlePurchase(it) }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        val productId = purchase.products.firstOrNull { it == PRODUCT_MONTH || it == PRODUCT_YEAR } ?: return

        // Consumando il prodotto, l'utente può riacquistarlo di nuovo al termine del periodo pagato.
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.consumeAsync(consumeParams) { billingResult, _ ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                scope.launch { onPurchaseCompleted(productId) }
            }
        }
    }

    fun endConnection() {
        billingClient.endConnection()
    }
}
