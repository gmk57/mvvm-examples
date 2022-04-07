package foo.bar.example.forecoroutine.feature.counter

import co.early.fore.kt.core.delegate.Fore
import co.early.fore.kt.core.delegate.TestDelegateDefault
import co.early.fore.kt.core.logging.SystemLogger
import gmk57.helpers.backgroundDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Copyright © 2019 early.co. All rights reserved.
 */
class CounterTest {

    @Before
    fun setup() {
        // make the code run synchronously, reroute Log.x to
        // System.out.println() so we see it in the test log
        Fore.setDelegate(TestDelegateDefault())
        val dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        backgroundDispatcher = dispatcher
    }

    @Test
    @Throws(Exception::class)
    fun initialConditions() {

        //arrange
        val counter = Counter(logger)

        //act

        //assert
        val state = counter.state.value
        Assert.assertEquals(false, state.isBusy)
        Assert.assertEquals(0, state.count.toLong())
    }


    @Test
    @Throws(Exception::class)
    fun increasesBy20() = runTest {

        //arrange
        val counter = Counter(logger)

        //act
        counter.increaseBy20()
        advanceUntilIdle()

        //assert
        val state = counter.state.value
        Assert.assertEquals(false, state.isBusy)
        Assert.assertEquals(20, state.count.toLong())
    }


    /**
     *
     * NB all we are checking here is that counter state has changed
     *
     * We don't really want tie our tests (OR any observers in production code)
     * to an expected number of times it has changed. (This would be
     * testing an implementation detail and make the tests unnecessarily brittle)
     *
     * The contract says nothing about how many times the observers will get called,
     * only that they will be called if something changes ("something" is not defined
     * and can change between implementations).
     *
     * See the reactive UIs section of the fore docs for more information about this
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun stateHasChanged() {

        //arrange
        val counter = Counter(logger)
        val initialState = counter.state.value

        //act
        counter.increaseBy20()

        //assert
        Assert.assertNotEquals(initialState, counter.state.value)
    }

    companion object {
        private val logger = SystemLogger()
    }

}
