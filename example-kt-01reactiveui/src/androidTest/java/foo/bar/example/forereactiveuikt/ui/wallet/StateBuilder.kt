package foo.bar.example.forereactiveuikt.ui.wallet

import androidx.test.rule.ActivityTestRule
import foo.bar.example.forereactiveuikt.OG
import foo.bar.example.forereactiveuikt.feature.wallet.Wallet
import foo.bar.example.forereactiveuikt.feature.wallet.WalletState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

/**
 *
 */
class StateBuilder internal constructor(private val mockWallet: Wallet) {

    private val mockWalletState: WalletState = mockk()

    internal fun withMobileWalletMaximum(totalFundsAvailable: Int): StateBuilder {

        every {
            mockWalletState.mobileWalletAmount
        } returns totalFundsAvailable

        every {
            mockWalletState.savingsWalletAmount
        } returns 0

        every {
            mockWalletState.canDecrease()
        } returns true

        every {
            mockWalletState.canIncrease()
        } returns false

        return this
    }

    internal fun withMobileWalletHalfFull(savingsWalletAmount: Int, mobileWalletAmount: Int): StateBuilder {

        every {
            mockWalletState.mobileWalletAmount
        } returns mobileWalletAmount

        every {
            mockWalletState.savingsWalletAmount
        } returns savingsWalletAmount

        every {
            mockWalletState.canDecrease()
        } returns true

        every {
            mockWalletState.canIncrease()
        } returns true

        return this
    }

    internal fun withMobileWalletEmpty(totalFundsAvailable: Int): StateBuilder {

        every {
            mockWalletState.mobileWalletAmount
        } returns 0

        every {
            mockWalletState.savingsWalletAmount
        } returns totalFundsAvailable

        every {
            mockWalletState.canDecrease()
        } returns false

        every {
            mockWalletState.canIncrease()
        } returns true

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
