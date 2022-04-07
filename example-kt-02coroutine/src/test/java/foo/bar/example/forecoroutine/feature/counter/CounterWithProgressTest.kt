package foo.bar.example.forecoroutine.feature.counter

import co.early.fore.kt.core.delegate.Fore
import co.early.fore.kt.core.delegate.TestDelegateDefault
import co.early.fore.kt.core.logging.SystemLogger
import gmk57.helpers.backgroundDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Test


/**
 * Copyright © 2019 early.co. All rights reserved.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CounterWithProgressTest {

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
        val counterWithProgress = CounterWithProgress(logger)

        //act

        //assert
        val state = counterWithProgress.state.value
        Assert.assertEquals(false, state.isBusy)
        Assert.assertEquals(0, state.progress.toLong())
        Assert.assertEquals(0, state.count.toLong())
    }


    @Test
    @Throws(Exception::class)
    fun increasesBy20() = runTest {

        //arrange
        val counterWithProgress = CounterWithProgress(logger)

        //act
        counterWithProgress.increaseBy20()
        advanceUntilIdle()

        //assert
        val state = counterWithProgress.state.value
        Assert.assertEquals(false, state.isBusy)
        Assert.assertEquals(0, state.progress.toLong())
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
     * The contract says nothing about how many times observers will get called,
     * only that they will be called if something changes ("something" is not defined
     * and can change between implementations).
     *
     * See the databinding readme for more information about this
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun stateHasChanged() = runTest {

        //arrange
        val counterWithProgress = CounterWithProgress(logger)
        val initialState = counterWithProgress.state.value

        //act
        counterWithProgress.increaseBy20()
        advanceUntilIdle()

        //assert
        Assert.assertNotEquals(initialState, counterWithProgress.state.value)
    }

    /**
     * Thanks to TestDispatcher & TestCoroutineScheduler testing the progress publication is
     * straightforward: we advance virtual time by 200ms chunks and verify the state is exactly
     * as it should be.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun progressIsPublished() = runTest {

        //arrange
        val counterWithProgress = CounterWithProgress(logger)

        //act
        counterWithProgress.increaseBy20()
        advanceTimeBy(100)

        //assert: progress
        repeat(19) {
            advanceTimeBy(200)
            val state = counterWithProgress.state.value
//            println("cycle: $it, currentTime: $currentTime, state = ${state}")
            Assert.assertEquals(true, state.isBusy)
            Assert.assertEquals(it + 1, state.progress)
            Assert.assertEquals(0, state.count)
        }
        advanceTimeBy(200)

        //assert: final state
        val state = counterWithProgress.state.value
//        println("currentTime: $currentTime, state = ${state}")
        Assert.assertEquals(false, state.isBusy)
        Assert.assertEquals(0, state.progress)
        Assert.assertEquals(20, state.count)
    }


    companion object {
        private val logger = SystemLogger()
    }

}
