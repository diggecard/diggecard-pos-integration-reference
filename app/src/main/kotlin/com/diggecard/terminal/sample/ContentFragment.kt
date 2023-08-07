package com.diggecard.terminal.sample

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.diggecard.terminal.integration.sample.R
import com.diggecard.terminal.integration.sample.databinding.FragmentContentBinding

/**
 * Optional external ref id which helps different systems to track the transactions.
 * The field is not mandatory so if you don't know what is it about just ignore it
 */
const val SAMPLE_EXTERNAL_REF_ID = "0xDEADBEEF"

const val POSITIVE_RESULT_COLOR = 0XFF109E12.toInt()
const val NEGATIVE_RESULT_COLOR = Color.RED

@SuppressLint("SetTextI18n")
class ContentFragment : Fragment() {

    private var binding: FragmentContentBinding? = null

    private var redeemLauncher: ActivityResultLauncher<RedeemActionConfig>? = null
    private var homeLauncher: ActivityResultLauncher<HomeActionConfig>? = null
    private var issueLauncher: ActivityResultLauncher<IssueActionConfig>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentContentBinding.inflate(inflater, container, false).also {
        binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindRedeemRequest()
        bindIssueRequest()
        bindHomeRequest()
    }

    private fun bindHomeRequest() {
        homeLauncher =
            registerForActivityResult(HomeContentContract()) { homeResult: HomeResponseData? ->
                if (homeResult == null) {
                    Toast.makeText(context, R.string.homeActionCancelledMessage, Toast.LENGTH_LONG)
                        .show()
                } else if (homeResult.redeemAmount != null) {
                    renderRedeemResult(homeResult.redeemAmount)
                } else if (homeResult.issueResult != null) {
                    renderIssueResult(homeResult.issueResult)
                }
            }

        binding?.homeDefault?.setOnClickListener {
            homeLauncher?.launch(
                HomeActionConfig(
                    externalReferenceId = SAMPLE_EXTERNAL_REF_ID,
                    currency = getCurrency(),
                )
            )
        }

        binding?.homeStyled?.setOnClickListener {
            homeLauncher?.launch(
                HomeActionConfig(
                    externalReferenceId = SAMPLE_EXTERNAL_REF_ID,
                    colorSchemeSeed = context?.getColor(R.color.primary),
                    currency = getCurrency(),
                )
            )
        }
    }

    private fun bindIssueRequest() {
        issueLauncher =
            registerForActivityResult(IssueResultContract()) { result: IssueResultData ->
                renderIssueResult(result)
            }

        binding?.issueDefault?.setOnClickListener {
            issueLauncher?.launch(
                IssueActionConfig(
                    issueAmount = getIssueAmount(),
                    externalReferenceId = SAMPLE_EXTERNAL_REF_ID,
                    currency = getCurrency(),
                )
            )
        }

        binding?.issueStyled?.setOnClickListener {
            issueLauncher?.launch(
                IssueActionConfig(
                    issueAmount = getIssueAmount(),
                    externalReferenceId = SAMPLE_EXTERNAL_REF_ID,
                    currency = getCurrency(),
                    colorSchemeSeed = context?.getColor(R.color.primary),
                ),
            )
        }
    }

    private fun getIssueAmount(): Double? = binding?.let {
        val amount = it.issueAmount.text.toString().toDoubleOrNull()
        return if (amount != null && amount > 0) amount else null
    }

    private fun renderIssueResult(result: IssueResultData) {
        binding?.issueResult?.apply {
            visibility = View.VISIBLE
            when (result) {
                is IssueResultDataCancelled -> {
                    if (result.reason == "currency_not_supported") {
                        setText(R.string.redeemNotSupportedCurrency)
                    } else {
                        setText(R.string.redeemCanceled)
                    }

                    setText(R.string.issueCanceled)
                    setTextColor(NEGATIVE_RESULT_COLOR)
                }

                is IssueResultDataSuccess -> {
                    text = """
                                    Issued amount: ${result.issueAmount};
                                    Card type: ${result.cardType};
                                    Card number ${result.cardNumber};
                                    Email: ${result.email};
                                    Phone: ${result.phone}.
                                """
                    setTextColor(POSITIVE_RESULT_COLOR)
                }
            }
        }
    }

    private fun bindRedeemRequest() {
        redeemLauncher =
            registerForActivityResult(RedeemContentContract()) { redeemResult: RedeemResultData ->
                renderRedeemResult(redeemResult)
            }

        binding?.redeemDefault?.setOnClickListener {
            val amount = binding?.basketAmount?.text?.toString()?.toDoubleOrNull()
            redeemLauncher?.launch(
                RedeemActionConfig(
                    amount, SAMPLE_EXTERNAL_REF_ID, null, getCurrency()
                )
            )
        }

        binding?.redeemStyled?.setOnClickListener {
            val amount = binding?.basketAmount?.text?.toString()?.toDoubleOrNull()
            redeemLauncher?.launch(
                RedeemActionConfig(
                    amount, "0xDEADBEEF", context?.getColor(R.color.primary), getCurrency()
                )
            )
        }
    }

    private fun renderRedeemResult(redeemResult: RedeemResultData) {
        binding?.redeemResult?.apply {
            when (redeemResult) {
                is RedeemResultDataCanceled -> {
                    if (redeemResult.reason == "currency_not_supported") {
                        setText(R.string.redeemNotSupportedCurrency)
                    } else {
                        setText(R.string.redeemCanceled)
                    }
                    setTextColor(NEGATIVE_RESULT_COLOR)
                }

                is RedeemResultDataSuccess -> {
                    text = """
                            Redeem amount: ${redeemResult.redeemAmount}
                            Remaining to pay: ${redeemResult.remainingToPay}
                        """
                    setTextColor(POSITIVE_RESULT_COLOR)
                }
            }
        }
    }

    private fun getCurrency(): String? {
        return when (binding?.currencyGroup?.checkedRadioButtonId) {
            R.id.currencyUsd -> "USD"
            R.id.currencyGbp -> "GBP"
            else -> null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        redeemLauncher?.unregister()
    }
}
