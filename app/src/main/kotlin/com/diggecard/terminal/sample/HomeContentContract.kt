package com.diggecard.terminal.sample

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class HomeContentContract : ActivityResultContract<HomeActionConfig, HomeResponseData?>() {

    override fun createIntent(context: Context, input: HomeActionConfig) =

        // IMPORTANT: Make sure FLAG_ACTIVITY_NEW_TASK is not set as it's not
        // possible to pass results between tasks https://stackoverflow.com/a/15668778
        Intent("com.diggecard.terminal.HOME").apply {
            if (input.colorSchemeSeed != null) {
                putExtra("color_scheme_seed", input.colorSchemeSeed)
            }

            if (input.externalReferenceId != null) {
                putExtra("external_reference_id", input.externalReferenceId)
            }
        }

    override fun parseResult(resultCode: Int, intent: Intent?): HomeResponseData? =
        if (resultCode == Activity.RESULT_OK) {
            when (intent?.getStringExtra("result_action")) {
                REDEEM_ACTION -> {
                    HomeResponseData(
                        redeemAmount = RedeemContentContract.intentToResult(
                            resultCode,
                            intent
                        )
                    )
                }

                ISSUE_ACTION -> {
                    HomeResponseData(
                        issueResult = IssueResultContract.intentToIssueResult(resultCode, intent)
                    )
                }

                else -> {
                    null
                }
            }
        } else {
            null
        }
}

data class HomeActionConfig(
    val externalReferenceId: String? = null,
    val colorSchemeSeed: Int? = null,
)

data class HomeResponseData(
    val redeemAmount: RedeemResultData? = null,
    val issueResult: IssueResultData? = null,
)
