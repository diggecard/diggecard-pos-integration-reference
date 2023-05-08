## Diggecard terminal app integration(DRAFT)

Diggecard gift card app uses standard android activity result mechanism.
You can read about it more detailed [here](https://developer.android.com/training/basics/intents/result).

For now the app supports 3 actions: 

* `com.diggecard.terminal.HOME` - leads user to the home screen.
  The result will depend on what flow user selects on the screen. See `com.diggecard.terminal.REDEEM` and `com.diggecard.terminal.ISSUE` actions.
* `com.diggecard.terminal.REDEEM` - leads to the redemption flow. In case the user exits the flow -
  `Intent.RESULT_CANCELED` is returned. In case user finishes the flow successfully - Intent.RESULT_OK is
  returned.

    ```text
        input fields:
          basket_amount: Double?
          external_reference_id: String?
          color_scheme_seed: Integer?(experimental)
        result fields
          redeem_amount: Double
    ```
    
* com.diggecard.terminal.**ISSUE** - leads user to the card issuing flow. In case the user exits the flow -
  `Intent.RESULT_CANCELED` is returned. In case user finishes the flow successfully - `Intent.RESULT_OK` is
  returned.

    ```text
        input fields:
          external_reference_id: String?
          color_scheme_seed: Integer? (experimental)
        result fields
          issue_amount: Double
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

class RedeemContentContract : ActivityResultContract<RedeemConfig, Double?>() {

    override fun createIntent(context: Context, input: RedeemConfig) =
        // IMPORTANT: Make sure FLAG_ACTIVITY_NEW_TASK is not set. In case it is set,
        // current task always cancels the result as it's not possible to pass results
        // between tasks https://stackoverflow.com/a/15668778
        Intent("com.diggecard.terminal.REDEEM").apply {
            if (input.basketAmount != null) {
                putExtra("basket_amount", input.basketAmount)
            }
            
            if (input.externalReferenceId != null) {
                putExtra("external_reference_id", input.externalReferenceId)
            }
            
            if (input.colorSchemeSeed != null) {
                putExtra("color_scheme_seed", input.colorSchemeSeed)
            }
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Double? =
        if (resultCode == Activity.RESULT_OK) {
            intent?.getDoubleExtra("redeem_amount", -1.0)
        } else {
            null
        }

}

data class RedeemConfig(
    val basketAmount: Double?,
    val externalReferenceId: String?,
    val colorSchemeSeed: Int?
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

class IssueContentContract : ActivityResultContract<IssueConfig, Double?>() {

    override fun createIntent(context: Context, input: IssueConfig) =
        // IMPORTANT: Make sure FLAG_ACTIVITY_NEW_TASK is not set. In case it is set,
        // current task always cancels the result as it's not possible to pass results
        // between tasks https://stackoverflow.com/a/15668778
        Intent("com.diggecard.terminal.ISSUE").apply {
          if (input.externalReferenceId != null) {
            putExtra("external_reference_id", input.externalReferenceId)
          }

          if (input.colorSchemeSeed != null) {
            putExtra("color_scheme_seed", input.colorSchemeSeed)
          }
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Double? =
        if (resultCode == Activity.RESULT_OK) {
            intent?.getDoubleExtra("issue_amount", -1.0)
        } else {
            null
        }

}

data class IssueConfig(
    val externalReferenceId: String?,
    val colorSchemeSeed: Int?,
)

```

in Activity/Fragment:
```kotlin
registerForActivityResult(IssueContentContract()) { issueAmount: Double? ->
    if (issueAmount == null) {
        Toast.makeText(context, "Issue flow cancelled", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, "Issued: $redeemAmount", Toast.LENGTH_SHORT).show()
    }
}
```
