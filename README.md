## Diggecard terminal app integration(DRAFT)

Diggecard gift card app uses standard android activity result mechanism.
You can read about it more detailed [here](https://developer.android.com/training/basics/intents/result).

For now the app supports 3 actions: 

* `com.diggecard.terminal.HOME` - leads user to the home screen.
  The result will depend on which flow user selects on home screen. See `com.diggecard.terminal.REDEEM` and `com.diggecard.terminal.ISSUE` actions.
* `com.diggecard.terminal.REDEEM` - leads user to the redemption flow. In case the user terminates the flow -
  `Intent.RESULT_CANCELED` is returned. In case user finishes the flow successfully - Intent.RESULT_OK is
  returned.

    ```text
        input fields:
          basket_amount: Double?
          external_reference_id: String?
          color_scheme_seed: Integer?(experimental) // may be removed in the future
        result fields
          result_action: String // com.diggecard.terminal.REDEEM
          redeem_amount: Double
    ```
    
* com.diggecard.terminal.**ISSUE** - leads user to the card issuing flow. In case the user terminates the flow -
  `Intent.RESULT_CANCELED` is returned. In case user finishes the flow successfully - `Intent.RESULT_OK` is
  returned.

    ```text
        input fields:
          external_reference_id: String?
          color_scheme_seed: Integer? (experimental) // may be removed in the future
        result fields:
          result_action: String // com.diggecard.terminal.ISSUE
          card_type: String // refund_card or gift_card
          card_number: String? // card number available only if user selects physical card 
          email: String? // available if user selects delivery method email
          phone: String? // available if user selects delivery method sms 
    ```

**color_scheme_seed** - is an experimental parameter, which defines the flow color scheme. The gift card app will 
try to apply the color for the flows.

Each result intent with `Intent.RESULT_OK` contains `result_action` field, which 
 may content `com.diggecard.terminal.REDEEM` or `com.diggecard.terminal.ISSUE` values.
In case the original action is `com.diggecard.terminal.HOME` `result_action` will depend on the selected flow by the suer.

### Redeem example:
```kotlin
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

const val REDEEM_ACTION = "com.diggecard.terminal.REDEEM"

class RedeemContentContract : ActivityResultContract<RedeemActionConfig, Double?>() {

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
    }

  override fun parseResult(resultCode: Int, intent: Intent?): Double? =
      intentToResult(resultCode, intent)

  companion object {
    fun intentToResult(resultCode: Int, intent: Intent?): Double? =
      if (resultCode == Activity.RESULT_OK) {
        intent?.getDoubleExtra("redeem_amount", -1.0)
      } else {
        null
      }
  }
}

data class RedeemActionConfig(
    val basketAmount: Double?,
    val externalReferenceId: String?,
    val colorSchemeSeed: Int?,
)


```

in Activity / Fragment:
```kotlin
registerForActivityResult(RedeemContentContract()) { redeemAmount: Double? ->
  if (redeemAmount == null) {
    Toast.makeText(context, "Redemption cancelled", Toast.LENGTH_SHORT).show()
  } else {
    Toast.makeText(context, "Redeem result: $redeemAmount", Toast.LENGTH_SHORT).show()
  }
}
```

### Issue example:
```kotlin

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
    }

  override fun parseResult(resultCode: Int, intent: Intent?): IssueResultData? =
      intentToIssueResult(resultCode, intent)

  companion object {
    fun intentToIssueResult(resultCode: Int, intent: Intent?): IssueResultData? =
      if (intent != null && resultCode == Activity.RESULT_OK) {
        val cardType = intent.getStringExtra("card_type")

        IssueResultData(
          intent.getDoubleExtra("issue_amount", -1.0),
          if (cardType == "refund_card") CardType.REFUND_CARD else CardType.GIFT_CART,
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
)

data class IssueResultData(
    val issueAmount: Double,
    val cardType: CardType,
    val cardNumber: String?,
    val email: String?,
    val phone: String?,
)

enum class CardType {
  GIFT_CART, REFUND_CARD
}

```

in Activity/Fragment:
```kotlin
registerForActivityResult(IssueResultContract()) { result: IssueResultData? ->
  if (issueAmount == null) {
    Toast.makeText(context, "Issue flow cancelled", Toast.LENGTH_SHORT).show()
  } else {
    Toast.makeText(context, "Issue result: $result", Toast.LENGTH_SHORT).show()
  }
}
```

### Home example:
```kotlin

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class HomeContentContract : ActivityResultContract<HomeActionConfig, HomeResponseData?>() {

  // IMPORTANT: Make sure FLAG_ACTIVITY_NEW_TASK is not set as it's not
  // possible to pass results between tasks https://stackoverflow.com/a/15668778
  override fun createIntent(context: Context, input: HomeActionConfig) =
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
  val redeemAmount: Double? = null,
  val issueResult: IssueResultData? = null,
)

```


in Activity/Fragment:
```kotlin
registerForActivityResult(HomeContentContract()) { homeResult: HomeResponseData? ->
  if (homeResult?.redeemAmount != null) {
    Toast.makeText(
      context,
      "Home redeem result: ${homeResult.redeemAmount}",
      Toast.LENGTH_SHORT
    ).show()
  }

  if (homeResult?.issueResult != null) {
    Toast.makeText(
      context,
      "Home issue result: ${homeResult.issueResult}",
      Toast.LENGTH_SHORT
    ).show()
  }
}
```
