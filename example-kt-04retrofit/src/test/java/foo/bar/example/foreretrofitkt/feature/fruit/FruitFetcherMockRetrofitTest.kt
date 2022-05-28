package foo.bar.example.foreretrofitkt.feature.fruit

import co.early.fore.kt.core.delegate.Fore
import co.early.fore.kt.core.delegate.TestDelegateDefault
import co.early.fore.kt.core.logging.SystemLogger
import co.early.fore.kt.net.InterceptorLogging
import co.early.fore.net.testhelpers.InterceptorStubbedService
import co.early.fore.net.testhelpers.StubbedServiceDefinition
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import foo.bar.example.foreretrofitkt.api.CommonServiceFailures
import foo.bar.example.foreretrofitkt.api.CustomGlobalErrorHandler
import foo.bar.example.foreretrofitkt.api.CustomRetrofitBuilder
import foo.bar.example.foreretrofitkt.api.fruits.FruitPojo
import foo.bar.example.foreretrofitkt.api.fruits.FruitService
import foo.bar.example.foreretrofitkt.feature.fruit.FruitFetcherMockRetrofitTest.SameThreadExecutorService
import foo.bar.example.foreretrofitkt.message.ErrorMessage
import gmk57.helpers.backgroundDispatcher
import io.mockk.MockKAnnotations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.mock.Calls
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit


/**
 * This is a slightly more end-to-end style of test, but without actually connecting to a network
 *
 * Similar to [FruitFetcherIntegrationTest], but using [MockRetrofit] instead of
 * [InterceptorStubbedService]. Differences:
 *
 * 1) Does not test Retrofit & OkHttp configuration (adapters, interceptors...). They're all mocked.
 *
 * 2) Can execute calls synchronously with the help of [SameThreadExecutorService]. This allows to
 * get `fruitFetcher.state.value` instead of waiting for non-busy state, but only for success case:
 * errors are still returned asynchronously by `suspendAndThrow` in `BehaviorDelegate.returning()`.
 *
 * 3) Fits nice for testing success cases: `returningResponse(ourPojo)` and network errors:
 * `returning(Calls.failure(exception))`, but not so nice for HTTP errors and JSON parsing errors.
 *
 * I wanted to reuse [StubbedServiceDefinition] for driving tests, this required re-implementing
 * parts of Retrofit logic in our test scaffolding. In general it should not be necessary.
 *
 * As usual for tests, we replace `Main` dispatcher with a `TestDispatcher`.
 *
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FruitFetcherMockRetrofitTest {

    private val logger = SystemLogger()
    private val interceptorLogging = InterceptorLogging(logger)
    private val errorHandler = CustomGlobalErrorHandler(logger)


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


    /**
     * Here we are making sure that the model correctly handles a successful server response
     * containing a list of fruit
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun fetchFruit_Success() = runTest(dispatchTimeoutMs = 1000) {
        logger.i("fetchFruit_Success started")

        //arrange
        val fruitService = stubbedRetrofitFruitService(stubbedSuccess)
        val fruitFetcher = FruitFetcher(fruitService, errorHandler, logger)


        //act
        fruitFetcher.fetchFruitsAsync()
        advanceUntilIdle()
        val fetcherState = fruitFetcher.state.value

        //assert
        assertEquals(false, fetcherState.isBusy)
        assertEquals(stubbedSuccess.expectedResult, fetcherState.currentFruit)
        assertEquals(Unit, fetcherState.success?.consume())
        assertNull(fetcherState.error)
        logger.i("fetchFruit_Success finished")
    }

    /**
     * Here we are making sure that the model correctly handles a server response indicating
     * that the user account has been locked
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun fetchFruit_Fail_UserLocked() = runTest(dispatchTimeoutMs = 1000) {
        logger.i("fetchFruit_Fail_UserLocked started")

        //arrange
        val fruitService = stubbedRetrofitFruitService(stubbedFailUserLocked)
        val fruitFetcher = FruitFetcher(fruitService, errorHandler, logger)


        //act
        fruitFetcher.fetchFruitsButFailAdvanced()
        advanceUntilIdle()
        val fetcherState = fruitFetcher.state.first { !it.isBusy }


        //assert
        assertEquals(false, fetcherState.isBusy)
        assertEquals(0, fetcherState.currentFruit.tastyPercentScore.toLong())
        assertNull(fetcherState.success)
        assertEquals(stubbedFailUserLocked.expectedResult, fetcherState.error?.consume())
        logger.i("fetchFruit_Fail_UserLocked finished")
    }

    /**
     * Here we are making sure that the model correctly handles a server response indicating
     * that the user account has not been enabled
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun fetchFruit_Fail_UserNotEnabled() = runTest(dispatchTimeoutMs = 1000) {
        logger.i("fetchFruit_Fail_UserNotEnabled started")

        //arrange
        val fruitService = stubbedRetrofitFruitService(stubbedFailureUserNotEnabled)
        val fruitFetcher = FruitFetcher(fruitService, errorHandler, logger)


        //act
        fruitFetcher.fetchFruitsButFailAdvanced()
        advanceUntilIdle()
        val fetcherState = fruitFetcher.state.first { !it.isBusy }


        //assert
        assertEquals(false, fetcherState.isBusy)
        assertEquals(0, fetcherState.currentFruit.tastyPercentScore.toLong())
        assertNull(fetcherState.success)
        assertEquals(stubbedFailureUserNotEnabled.expectedResult, fetcherState.error?.consume())
        logger.i("fetchFruit_Fail_UserNotEnabled finished")
    }


    /**
     * Here we are making sure that the model correctly handles common API failed responses
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun fetchFruit_CommonFailures() = runTest(dispatchTimeoutMs = 1000) {
        logger.i("fetchFruit_CommonFailures started")

        for (stubbedServiceDefinition in CommonServiceFailures()) {

            logger.i(
                "------- Common Service Failure: HTTP:"
                        + stubbedServiceDefinition.httpCode
                        + " res:" + stubbedServiceDefinition.resourceFileName
                        + " expect:" + stubbedServiceDefinition.expectedResult
                        + " --------"
            )

            //arrange
            val fruitService = stubbedRetrofitFruitService(stubbedServiceDefinition)
            val fruitFetcher = FruitFetcher(fruitService, errorHandler, logger)


            //act
            fruitFetcher.fetchFruitsAsync()
            advanceUntilIdle()
            val fetcherState = fruitFetcher.state.first { !it.isBusy }


            //assert
            assertEquals(false, fetcherState.isBusy)
            assertEquals(0, fetcherState.currentFruit.tastyPercentScore.toLong())
            assertNull(fetcherState.success)
            assertEquals(stubbedServiceDefinition.expectedResult, fetcherState.error?.consume())
            logger.i("------- Common Service Failure finished")
        }

        logger.i("fetchFruit_CommonFailures finished")
    }


    private fun stubbedRetrofitFruitService(stubbedServiceDefinition: StubbedServiceDefinition<*>): FruitService {
        val retrofit = CustomRetrofitBuilder.create {
            addInterceptor(interceptorLogging)
        }

        return MockRetrofit.Builder(retrofit)
            .networkBehavior(reliableBehavior())
            .backgroundExecutor(SameThreadExecutorService())
            .build()
            .create(FruitService::class.java)
            .returning(getResponse(stubbedServiceDefinition))
    }

    // default NetworkBehavior fails in 3% cases
    private fun reliableBehavior() = NetworkBehavior.create().apply {
        setDelay(0, TimeUnit.SECONDS)
        setVariancePercent(0)
        setFailurePercent(0)
    }

    private fun getResponse(definition: StubbedServiceDefinition<*>): Call<Any> {
        definition.ioException?.let { return Calls.failure(it) }

        val bodyString = readTestResourceFile(definition.resourceFileName)
        val mediaType = definition.mimeType.toMediaTypeOrNull()
        val responseBody = bodyString.toResponseBody(mediaType)

        return if (definition.httpCode in 200..299) {
            val targetType = object : TypeToken<ArrayList<FruitPojo>>() {}.type
            try {
                val result: List<FruitPojo>? =
                    Gson().fromJson<ArrayList<FruitPojo>>(bodyString, targetType)
                Calls.response(result)
            } catch (e: Exception) {
                Calls.failure(e)
            }
        } else {
            Calls.failure(HttpException(Response.error<Any>(definition.httpCode, responseBody)))
        }
    }

    private fun readTestResourceFile(fileName: String): String {
        val fileInputStream = javaClass.classLoader?.getResourceAsStream(fileName)
        return fileInputStream?.bufferedReader()?.readText() ?: ""
    }

    companion object {

        private val stubbedSuccess = StubbedServiceDefinition(
            200, //stubbed HTTP code
            "fruit/success.json", //stubbed body response
            FruitPojo("orange", true, 43) //expected result
        )

        private val stubbedFailUserLocked = StubbedServiceDefinition(
            401, //stubbed HTTP code
            "common/error_user_locked.json", //stubbed body response
            ErrorMessage.ERROR_FRUIT_USER_LOCKED //expected result
        )

        private val stubbedFailureUserNotEnabled = StubbedServiceDefinition(
            401, //stubbed HTTP code
            "common/error_user_not_enabled.json", //stubbed body response
            ErrorMessage.ERROR_FRUIT_USER_NOT_ENABLED //expected result
        )
    }

    class SameThreadExecutorService : AbstractExecutorService() {
        override fun execute(command: Runnable?) {
            command?.run()
        }

        override fun shutdown() {}

        override fun shutdownNow(): List<Runnable> = emptyList()

        override fun isShutdown(): Boolean = false

        override fun isTerminated(): Boolean = false

        override fun awaitTermination(timeout: Long, unit: TimeUnit?): Boolean = false
    }
}
