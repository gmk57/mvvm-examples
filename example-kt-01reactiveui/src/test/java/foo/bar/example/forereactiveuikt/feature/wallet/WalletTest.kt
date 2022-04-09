package foo.bar.example.forereactiveuikt.feature.wallet

import co.early.fore.kt.core.delegate.Fore
import co.early.fore.kt.core.delegate.TestDelegateDefault
import co.early.fore.kt.core.logging.Logger
import co.early.fore.kt.core.logging.SystemLogger
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Copyright © 2015-2020 early.co. All rights reserved.
 */
class WalletTest {

    @Before
    fun setup() {

        // make the code run synchronously, reroute Log.x to
        // System.out.println() so we see it in the test log
        Fore.setDelegate(TestDelegateDefault())
    }

    @Test
    @Throws(Exception::class)
    fun initialConditions() {

        //arrange
        val wallet = Wallet(logger)

        //act

        //assert
        val state = wallet.state.value
        Assert.assertEquals(true, state.canIncrease())
        Assert.assertEquals(false, state.canDecrease())
        Assert.assertEquals(state.totalDollarsAvailable, state.savingsWalletAmount)
        Assert.assertEquals(0, state.mobileWalletAmount)
    }

    @Test
    @Throws(Exception::class)
    fun increaseMobileWallet() {

        //arrange
        val wallet = Wallet(logger)

        //act
        wallet.increaseMobileWallet()

        //assert
        val state = wallet.state.value
        Assert.assertEquals(true, state.canIncrease())
        Assert.assertEquals(true, state.canDecrease())
        Assert.assertEquals(state.totalDollarsAvailable - 1, state.savingsWalletAmount)
        Assert.assertEquals(1, state.mobileWalletAmount)
    }

    @Test
    @Throws(Exception::class)
    fun decreaseMobileWallet() {

        //arrange
        val wallet = Wallet(logger)
        wallet.increaseMobileWallet()
        Assert.assertEquals(1, wallet.state.value.mobileWalletAmount)

        //act
        wallet.decreaseMobileWallet()

        //assert
        val state = wallet.state.value
        Assert.assertEquals(true, state.canIncrease())
        Assert.assertEquals(false, state.canDecrease())
        Assert.assertEquals(state.totalDollarsAvailable, state.savingsWalletAmount)
        Assert.assertEquals(0, state.mobileWalletAmount)
    }

    @Test
    @Throws(Exception::class)
    fun canIncreaseIsFalseAtLimit() {

        //arrange
        val wallet = Wallet(logger)
        for (ii in 0 until wallet.state.value.totalDollarsAvailable) {
            wallet.increaseMobileWallet()
        }

        //act

        //assert
        Assert.assertEquals(false, wallet.state.value.canIncrease())
        Assert.assertEquals(true, wallet.state.value.canDecrease())
    }

    /**
     *
     * NB all we are checking here is that wallet state has changed
     *
     * We don't really want tie our tests (OR any observers in production code)
     * to an expected number of times it has changed. (This would be
     * testing an implementation detail and make the tests unnecessarily brittle)
     *
     * StateFlow conflates updates, so "a slow collector skips fast updates, but
     * always collects the most recently emitted value".
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun stateChangedForIncrease() {

        //arrange
        val wallet = Wallet(logger)
        val initialState = wallet.state.value

        //act
        wallet.increaseMobileWallet()

        //assert
        Assert.assertNotEquals(initialState, wallet.state.value)
    }

    @Test
    @Throws(Exception::class)
    fun stateChangedForDecrease() {

        //arrange
        val wallet = Wallet(logger)
        wallet.increaseMobileWallet()
        val initialState = wallet.state.value

        //act
        wallet.decreaseMobileWallet()

        //assert
        Assert.assertNotEquals(initialState, wallet.state.value)
    }

    companion object {
        private val logger: Logger = SystemLogger()
    }
}
