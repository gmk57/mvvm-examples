package foo.bar.example.foreretrofitkt.feature.fruit

import co.early.fore.kt.core.delegate.Fore
import co.early.fore.kt.core.delegate.TestDelegateDefault
import co.early.fore.kt.core.logging.SystemLogger
import co.early.fore.kt.net.InterceptorLogging
import co.early.fore.net.testhelpers.InterceptorStubbedService
import co.early.fore.net.testhelpers.StubbedServiceDefinition
import foo.bar.example.foreretrofitkt.api.CommonServiceFailures
import foo.bar.example.foreretrofitkt.api.CustomGlobalErrorHandler
import foo.bar.example.foreretrofitkt.api.CustomRetrofitBuilder
import foo.bar.example.foreretrofitkt.api.fruits.FruitPojo
import foo.bar.example.foreretrofitkt.api.fruits.FruitService
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
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit


/**
 * This is a slightly more end-to-end style of test, but without actually connecting to a network
 *
 * Similar to [FruitFetcherIntegrationTest], but using [MockWebServer] instead of
 * [InterceptorStubbedService]. Differences:
 *
 * 1) A bit closer to real life: requests pass unintercepted, mocking happens on the "server" side
 *
 * 2) MockWebServer can serve multiple (different) responses, emulate network delays, save requests
 * made by an app for verification, etc
 *
 * 3) There is no natural way to throw specific `IOException`s, but `SocketTimeoutException` can be
 * emulated by skipping `mockWebServer.enqueue(response)`.
 *
 * 4) To test timeouts efficiently we can decrease OkHttp timeout settings from its default 10 sec.
 *
 * As usual for tests, we replace `Main` dispatcher with a `TestDispatcher`.
 *
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FruitFetcherMockWebServerTest {

    private val logger = SystemLogger()
    private val interceptorLogging = InterceptorLogging(logger)
    private val errorHandler = CustomGlobalErrorHandler(logger)
    private lateinit var mockWebServer: MockWebServer


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

    @After
    fun tearDown() {
        mockWebServer.shutdown()
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
        val retrofit = stubbedRetrofit(stubbedSuccess)
        val fruitFetcher = FruitFetcher(
            retrofit.create(FruitService::class.java),
            errorHandler,
            logger
        )


        //act
        fruitFetcher.fetchFruitsAsync()
        advanceUntilIdle()  // safeguard in case isBusy=true is set asynchronously

        // see https://stackoverflow.com/a/64971215
        val fetcherState = fruitFetcher.state.first { !it.isBusy }

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
        val retrofit = stubbedRetrofit(stubbedFailUserLocked)
        val fruitFetcher = FruitFetcher(
            retrofit.create(FruitService::class.java),
            errorHandler,
            logger
        )


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
        val retrofit = stubbedRetrofit(stubbedFailureUserNotEnabled)
        val fruitFetcher = FruitFetcher(
            retrofit.create(FruitService::class.java),
            errorHandler,
            logger
        )


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
    fun fetchFruit_CommonFailures() = runTest(dispatchTimeoutMs = 5000) {
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
            val retrofit = stubbedRetrofit(stubbedServiceDefinition)
            val fruitFetcher = FruitFetcher(
                retrofit.create(FruitService::class.java),
                errorHandler,
                logger
            )


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

            @Suppress("BlockingMethodInNonBlockingContext")
            mockWebServer.shutdown()
        }

        logger.i("fetchFruit_CommonFailures finished")
    }


    private fun stubbedRetrofit(definition: StubbedServiceDefinition<*>): Retrofit {
        // new MockWebServer for each iteration in fetchFruit_CommonFailures,
        // otherwise responses get messed up
        mockWebServer = MockWebServer()
        mockWebServer.start()

        if (definition.successfullyConnected()) {
            val bodyString = readTestResourceFile(definition.resourceFileName)
            val response = MockResponse()
                .setResponseCode(definition.httpCode)
                .setBody(bodyString)
                .addHeader("Content-Type", definition.mimeType)
            mockWebServer.enqueue(response)
        }

        val url: String = mockWebServer.url("/").toString()
        return CustomRetrofitBuilder.create(url) {
            addInterceptor(interceptorLogging)
            readTimeout(500, TimeUnit.MILLISECONDS)
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

}
