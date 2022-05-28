package foo.bar.example.foreretrofitkt.feature.fruit

import co.early.fore.kt.core.delegate.Fore
import co.early.fore.kt.core.delegate.TestDelegateDefault
import co.early.fore.kt.core.logging.SystemLogger
import foo.bar.example.foreretrofitkt.api.CustomGlobalErrorHandler
import foo.bar.example.foreretrofitkt.api.fruits.FruitPojo
import foo.bar.example.foreretrofitkt.api.fruits.FruitService
import foo.bar.example.foreretrofitkt.message.ErrorMessage
import gmk57.helpers.backgroundDispatcher
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test


/**
 * Tests for this model cover a few areas:
 *
 *
 * 1) Construction: we check that the model is constructed in the correct state
 * 2) Receiving data: we check that the model behaves appropriately when receiving various success and fail responses from the FruitService
 * 3) Observers and State: we check that the model updates its observers correctly and presents its current state accurately
 *
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FruitFetcherUnitTest {

    private val fruitPojo = FruitPojo("strawberry", false, 71)

    @MockK
    private lateinit var mockErrorHandler: CustomGlobalErrorHandler

    @MockK
    private lateinit var mockFruitService: FruitService


    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

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
        logger.i("initialConditions started")

        //arrange
        val fruitFetcher = FruitFetcher(mockFruitService, mockErrorHandler, logger)

        //act

        //assert
        val fetcherState = fruitFetcher.state.value
        assertEquals(false, fetcherState.isBusy)
        assertEquals(0, fetcherState.currentFruit.tastyPercentScore.toLong())
        assertEquals(false, fetcherState.currentFruit.isCitrus)
        logger.i("initialConditions finished")
    }


    @Test
    @Throws(Exception::class)
    fun fetchFruit_MockSuccess() = runTest {
        logger.i("fetchFruit_MockSuccess started")

        //arrange
        StateBuilder(mockFruitService, mockErrorHandler).getFruitSuccess(fruitPojo)
        val fruitFetcher = FruitFetcher(mockFruitService, mockErrorHandler, logger)


        //act
        fruitFetcher.fetchFruitsAsync()
        advanceUntilIdle()


        //assert
        val fetcherState = fruitFetcher.state.value
        assertEquals(false, fetcherState.isBusy)
        assertEquals(fruitPojo, fetcherState.currentFruit)
        assertEquals(Unit, fetcherState.success?.consume())
        assertNull(fetcherState.error)
        logger.i("fetchFruit_MockSuccess finished")
    }


    @Test
    @Throws(Exception::class)
    fun fetchFruit_MockFailure() = runTest {
        logger.i("fetchFruit_MockFailure started")

        //arrange
        StateBuilder(mockFruitService, mockErrorHandler)
            .getFruitFail(ErrorMessage.ERROR_FRUIT_USER_LOGIN_CREDENTIALS_INCORRECT)
        val fruitFetcher = FruitFetcher(mockFruitService, mockErrorHandler, logger)


        //act
        fruitFetcher.fetchFruitsButFailAdvanced()
        advanceUntilIdle()

        //assert
        val fetcherState = fruitFetcher.state.value
        assertEquals(false, fetcherState.isBusy)
        assertEquals(false, fetcherState.currentFruit.isCitrus)
        assertEquals(0, fetcherState.currentFruit.tastyPercentScore.toLong())
        assertNull(fetcherState.success)
        assertEquals(
            ErrorMessage.ERROR_FRUIT_USER_LOGIN_CREDENTIALS_INCORRECT,
            fetcherState.error?.consume()
        )
        logger.i("fetchFruit_MockFailure finished")
    }


    /**
     * NB all we are checking here is that observers are called AT LEAST twice (first time should
     * happen immediately after subscription to StateFlow, so "at least once" test would not verify
     * anything about `fetchFruitsAsync`).
     *
     * We don't really want tie our tests (OR any observers in production code)
     * to an expected number of times this method might be called. (This would be
     * testing an implementation detail and make the tests unnecessarily brittle)
     *
     * Using UnconfinedTestDispatcher is recommended for this case, see
     * https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-test/MIGRATION.md#testcoroutinedispatcher-for-testing-intermediate-emissions
     */
    @Test
    fun observersNotifiedAtLeastOnce() = runTest(dispatchTimeoutMs = 1000) {
        logger.i("observersNotifiedAtLeastOnce started")

        //arrange
        StateBuilder(mockFruitService, mockErrorHandler).getFruitSuccess(fruitPojo)
        val fruitFetcher = FruitFetcher(mockFruitService, mockErrorHandler, logger)

        val mockObserver: FlowCollector<FruitFetcherState> = mockk(relaxed = true)
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            fruitFetcher.state.collect(mockObserver)
        }


        //act
        fruitFetcher.fetchFruitsAsync()
        job.cancel()  // prevents leaking infinite collection of StateFlow


        //assert
        coVerify(atLeast = 2) {
            mockObserver.emit(any())
        }
        logger.i("observersNotifiedAtLeastOnce finished")
    }

    /**
     * Another approach would be to test that state has changed after calling `fetchFruitsAsync`.
     *
     * If state has changed, but our observers weren't called, this is probably a bug either in
     * registering them or in StateFlow, but not in FruitFetcher.
     */
    @Test
    fun stateHasChanged() = runTest {
        logger.i("stateHasChanged started")

        //arrange
        StateBuilder(mockFruitService, mockErrorHandler).getFruitSuccess(fruitPojo)
        val fruitFetcher = FruitFetcher(mockFruitService, mockErrorHandler, logger)
        val initialState = fruitFetcher.state.value


        //act
        fruitFetcher.fetchFruitsAsync()


        //assert
        val fetcherState = fruitFetcher.state.value
        assertNotEquals(initialState, fetcherState)
        logger.i("stateHasChanged finished")
    }

    companion object {
        private val logger = SystemLogger()
    }
}
