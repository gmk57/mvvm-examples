package foo.bar.example.forereactiveuikt.feature.wallet

import org.junit.Assert
import org.junit.Test

class WalletStateTest {

    @Test
    fun `wallet empty`() {
        //arrange
        val state = WalletState(0, 0)

        //act

        //assert
        Assert.assertEquals(false, state.canIncrease())
        Assert.assertEquals(false, state.canDecrease())
        Assert.assertEquals(0, state.totalDollarsAvailable)
        Assert.assertEquals(0, state.savingsWalletAmount)
        Assert.assertEquals(0, state.mobileWalletAmount)
    }

    @Test
    fun `mobile wallet empty`() {
        //arrange
        val state = WalletState(12345, 0)

        //act

        //assert
        Assert.assertEquals(true, state.canIncrease())
        Assert.assertEquals(false, state.canDecrease())
        Assert.assertEquals(12345, state.totalDollarsAvailable)
        Assert.assertEquals(12345, state.savingsWalletAmount)
        Assert.assertEquals(0, state.mobileWalletAmount)
    }

    @Test
    fun `savings wallet empty`() {
        //arrange
        val state = WalletState(12345, 12345)

        //act

        //assert
        Assert.assertEquals(false, state.canIncrease())
        Assert.assertEquals(true, state.canDecrease())
        Assert.assertEquals(12345, state.totalDollarsAvailable)
        Assert.assertEquals(0, state.savingsWalletAmount)
        Assert.assertEquals(12345, state.mobileWalletAmount)
    }

    @Test
    fun `both wallets non-empty`() {
        //arrange
        val state = WalletState(2, 1)

        //act

        //assert
        Assert.assertEquals(true, state.canIncrease())
        Assert.assertEquals(true, state.canDecrease())
        Assert.assertEquals(2, state.totalDollarsAvailable)
        Assert.assertEquals(1, state.savingsWalletAmount)
        Assert.assertEquals(1, state.mobileWalletAmount)
    }
}
