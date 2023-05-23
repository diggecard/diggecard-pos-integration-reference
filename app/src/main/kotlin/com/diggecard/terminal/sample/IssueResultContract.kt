package com.diggecard.terminal.sample

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

const val ISSUE_ACTION = "com.diggecard.terminal.ISSUE"

class IssueResultContract : ActivityResultContract<IssueActionConfig, IssueResultData?>() {

    // IMPORTANT: Make sure FLAG_ACTIVITY_NEW_TASK is not set as it's not
    // possible to pass results between tasks https://stackoverflow.com/a/15668778
    override fun createIntent(context: Context, input: IssueActionConfig) =
        Intent(ISSUE_ACTION).apply {
            if (input.colorSchemeSeed != null) {
                putExtra("color_scheme_seed", input.colorSchemeSeed)
            }

            if (input.externalReferenceId != null) {
                putExtra("external_reference_id", input.externalReferenceId)
            }

            if (input.issueAmount != null) {
                putExtra("issue_amount", input.issueAmount)
            }
        }

    override fun parseResult(resultCode: Int, intent: Intent?): IssueResultData? =
        intentToIssueResult(resultCode, intent)

    companion object {
        fun intentToIssueResult(resultCode: Int, intent: Intent?): IssueResultData? =
            if (intent != null && resultCode == Activity.RESULT_OK) {
                val cardType = intent.getStringExtra("card_type")

                IssueResultData(
                    intent.getDoubleExtra("issue_amount", -1.0),
                    if (cardType == "refund_card") CardType.REFUND_CARD else CardType.GIFT_CARD,
                    intent.getStringExtra("card_number"),
                    intent.getStringExtra("email"),
                    intent.getStringExtra("phone"),
                )
            } else {
                null
            }
    }
}

data class IssueActionConfig(
    val externalReferenceId: String? = null,
    val colorSchemeSeed: Int? = null,
    val issueAmount: Double? = null,
)

data class IssueResultData(
    val issueAmount: Double,
    val cardType: CardType,
    val cardNumber: String?,
    val email: String?,
    val phone: String?,
)

enum class CardType {
    GIFT_CARD, REFUND_CARD
}
