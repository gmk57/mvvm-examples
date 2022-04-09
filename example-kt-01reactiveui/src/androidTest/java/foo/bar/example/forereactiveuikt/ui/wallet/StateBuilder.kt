package foo.bar.example.forereactiveuikt.ui.wallet

import androidx.test.rule.ActivityTestRule
import foo.bar.example.forereactiveuikt.OG
import foo.bar.example.forereactiveuikt.feature.wallet.Wallet
import foo.bar.example.forereactiveuikt.feature.wallet.WalletState
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow

/**
 *
 */
class StateBuilder internal constructor(private val mockWallet: Wallet) {

    private lateinit var mockWalletState: WalletState

    internal fun withMobileWalletMaximum(totalFundsAvailable: Int): StateBuilder {

        mockWalletState = WalletState(
            totalDollarsAvailable = totalFundsAvailable,
            mobileWalletAmount = totalFundsAvailable
        )

        return this
    }

    internal fun withMobileWalletHalfFull(savingsWalletAmount: Int, mobileWalletAmount: Int): StateBuilder {

        mockWalletState = WalletState(
            totalDollarsAvailable = savingsWalletAmount + mobileWalletAmount,
            mobileWalletAmount = mobileWalletAmount
        )

        return this
    }

    internal fun withMobileWalletEmpty(totalFundsAvailable: Int): StateBuilder {

        mockWalletState = WalletState(
            totalDollarsAvailable = totalFundsAvailable,
            mobileWalletAmount = 0
        )

        return this
    }

    fun createRule(): ActivityTestRule<WalletsActivity> {
        return object : ActivityTestRule<WalletsActivity>(WalletsActivity::class.java) {
            override fun beforeActivityLaunched() {

                every { mockWallet.state } returns MutableStateFlow(mockWalletState)

                //inject our mocks so our UI layer will pick them up
                OG.putMock(Wallet::class.java, mockWallet)
            }
        }
    }
}
