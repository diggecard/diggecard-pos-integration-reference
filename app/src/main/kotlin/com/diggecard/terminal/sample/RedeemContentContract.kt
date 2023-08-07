package com.diggecard.terminal.sample

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

const val REDEEM_ACTION = "com.diggecard.terminal.REDEEM"

class RedeemContentContract : ActivityResultContract<RedeemActionConfig, RedeemResultData>() {

    // IMPORTANT: Make sure FLAG_ACTIVITY_NEW_TASK is not set as it's not
    // possible to pass results between tasks https://stackoverflow.com/a/15668778
    override fun createIntent(context: Context, input: RedeemActionConfig) =
        Intent(REDEEM_ACTION).apply {
            if (input.basketAmount != null) {
                putExtra("basket_amount", input.basketAmount)
            }

            if (input.colorSchemeSeed != null) {
                putExtra("color_scheme_seed", input.colorSchemeSeed)
            }

            if (input.externalReferenceId != null) {
                putExtra("external_reference_id", input.externalReferenceId)
            }

            if (input.currency != null) {
                putExtra("currency", input.currency)
            }
        }

    override fun parseResult(resultCode: Int, intent: Intent?): RedeemResultData =
        intentToResult(resultCode, intent)

    companion object {
        fun intentToResult(resultCode: Int, intent: Intent?): RedeemResultData =
            when {
                resultCode == Activity.RESULT_OK && intent != null -> {
                    RedeemResultDataSuccess(
                        intent.getDoubleExtra("redeem_amount", -1.0),
                        intent.getDoubleExtra("remaining_to_pay", -1.0),
                    )
                }

                resultCode == Activity.RESULT_CANCELED -> {
                    RedeemResultDataCanceled(intent?.getStringExtra("reason"))
                }

                else -> {
                    RedeemResultDataCanceled(null)
                }
            }
    }
}

sealed interface RedeemResultData

data class RedeemResultDataSuccess(
    val redeemAmount: Double,
    val remainingToPay: Double,
) : RedeemResultData

data class RedeemResultDataCanceled(
    val reason: String?,
) : RedeemResultData

data class RedeemActionConfig(
    val basketAmount: Double?,
    val externalReferenceId: String?,
    val colorSchemeSeed: Int?,
    val currency: String?,
)
