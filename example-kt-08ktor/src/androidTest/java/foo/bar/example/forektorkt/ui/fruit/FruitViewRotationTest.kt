
package foo.bar.example.forektorkt.ui.fruit

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import co.early.fore.kt.core.logging.Logger
import co.early.fore.kt.core.logging.SystemLogger
import co.early.fore.kt.core.callbacks.FailureWithPayload
import co.early.fore.kt.core.callbacks.Success
import co.early.fore.kt.core.Either
import co.early.fore.kt.net.ktor.CallProcessorKtor
import foo.bar.example.forektorkt.EspressoTestMatchers.withDrawable
import foo.bar.example.forektorkt.R
import foo.bar.example.forektorkt.api.fruits.FruitPojo
import foo.bar.example.forektorkt.api.fruits.FruitService
import foo.bar.example.forektorkt.feature.fruit.FruitFetcher
import foo.bar.example.forektorkt.message.ErrorMessage
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CompletableDeferred
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.ArrayList
import java.util.concurrent.CountDownLatch


/**
 * Here we make sure that when the view is rotated while a long running action is being
 * performed by the model, all the view elements still accurately represent the model's
 * state after rotation is complete, and those view elements are still updated as appropriate
 * when the model completes the long running action.
 */

@RunWith(AndroidJUnit4::class)
class FruitViewRotationTest {

    /**
     * Here we're testing with a real model,
     * and just mocking its dependencies.
     * This lets us realistically test the
     * interactions between the view
     * and the model during and after
     * a rotation
     */

    lateinit var fruitFetcher: FruitFetcher
    private var logger: Logger = SystemLogger()
    private var fruitPojo = FruitPojo("testFruit1", true, 45)

    @MockK
    private lateinit var mockSuccess: Success
    @MockK
    private lateinit var mockFailureWithPayload: FailureWithPayload<ErrorMessage>
    @MockK
    lateinit var mockCallProcessorKtor: CallProcessorKtor<ErrorMessage>
    @MockK
    private lateinit var mockFruitService: FruitService

    private lateinit var deferredResult: CompletableDeferred<Either<ErrorMessage, List<FruitPojo>>>
    private val countDownLatch = CountDownLatch(1)

    @Before
    fun setUp() {

        logger.i("setup()")

        MockKAnnotations.init(this, relaxed = true)

        //construct a real model with mock dependencies
        fruitFetcher = FruitFetcher(
            mockFruitService,
            mockCallProcessorKtor,
            logger
        )
    }

    @Test
    @Throws(Exception::class)
    fun stateSurvivesRotation() {

        logger.i("stateSurvivesRotation()")

        //arrange
        val activity = FruitViewRotationTestStateBuilder(this)
            .withDelayedCallProcessor()
            .createRule()
            .launchActivity(null)

        checkUIBeforeClick(activity)

        //act
        fruitFetcher.fetchFruitsAsync(mockSuccess, mockFailureWithPayload)

        checkUIWhenFetching(activity)

        swapOrientation(activity)

        checkUIWhenFetching(activity)

        completeDeferredResult()

        try {
            countDownLatch.await()
        } catch (e: InterruptedException) {
        }

        checkUIOnceComplete(activity)
    }


    fun completeDeferredResult() {

        logger.i("callSuccessOnCachedSuccessFailCallback()")

        val fruitList = ArrayList<FruitPojo>()
        fruitList.add(fruitPojo)

        //we need to be back on the UI thread for this
        getInstrumentation().runOnMainSync {
            logger.i("about to call success, id:" + Thread.currentThread().id)

            deferredResult.complete(Either.right(fruitList))
            countDownLatch.countDown()
        }
    }

    fun setDeferredResult(deferredResult: CompletableDeferred<Either<ErrorMessage, List<FruitPojo>>>) {
        logger.i("setDeferredResult()")
        this.deferredResult = deferredResult
    }


    @Synchronized
    private fun checkUIBeforeClick(activity: Activity) {
        logger.i("checkUIBeforeClick()")

        //assert
        onView(withId(R.id.fruit_busy_progbar)).check(matches(not<View>(isDisplayed())))
        onView(withId(R.id.fruit_fetchsuccess_btn)).check(matches(isEnabled()))
        onView(withId(R.id.fruit_fetchfailadvanced_btn)).check(matches(isEnabled()))
        onView(withId(R.id.fruit_fetchfailbasic_btn)).check(matches(isEnabled()))
        onView(withId(R.id.fruit_name_textview)).check(matches(withText("(fruitless)")))
        onView(withId(R.id.fruit_tastyrating_textview)).check(
            matches(
                withText(
                    activity.getString(R.string.fruit_percent, 0)
                )
            )
        )
        onView(withId(R.id.fruit_citrus_img)).check(matches(withDrawable(R.drawable.lemon_negative)))
    }

    @Synchronized
    private fun checkUIWhenFetching(activity: Activity) {
        logger.i("checkUIWhenFetching()")

        //assert
        onView(withId(R.id.fruit_busy_progbar)).check(matches(isDisplayed()))
        onView(withId(R.id.fruit_fetchsuccess_btn)).check(matches(not<View>(isEnabled())))
        onView(withId(R.id.fruit_fetchfailadvanced_btn)).check(matches(not<View>(isEnabled())))
        onView(withId(R.id.fruit_fetchfailbasic_btn)).check(matches(not<View>(isEnabled())))
        onView(withId(R.id.fruit_name_textview)).check(matches(not<View>(isDisplayed())))
        onView(withId(R.id.fruit_tastyrating_textview)).check(
            matches(
                withText(
                    activity.getString(R.string.fruit_percent, 0)
                )
            )
        )
        onView(withId(R.id.fruit_citrus_img)).check(matches(withDrawable(R.drawable.lemon_negative)))
    }

    @Synchronized
    private fun checkUIOnceComplete(activity: Activity) {
        logger.i("checkUIOnceComplete()")

        //assert
        onView(withId(R.id.fruit_busy_progbar)).check(matches(not<View>(isDisplayed())))
        onView(withId(R.id.fruit_fetchsuccess_btn)).check(matches(isEnabled()))
        onView(withId(R.id.fruit_fetchfailadvanced_btn)).check(matches(isEnabled()))
        onView(withId(R.id.fruit_fetchfailbasic_btn)).check(matches(isEnabled()))
        onView(withId(R.id.fruit_name_textview)).check(matches(withText(fruitPojo.name)))
        onView(withId(R.id.fruit_tastyrating_textview)).check(
            matches(
                withText(
                    activity.getString(R.string.fruit_percent, fruitPojo.tastyPercentScore)
                )
            )
        )
        onView(withId(R.id.fruit_citrus_img)).check(
            matches(
                withDrawable(
                    if (fruitPojo.isCitrus)
                        R.drawable.lemon_positive
                    else
                        R.drawable.lemon_negative
                )
            )
        )
    }

    private fun swapOrientation(activity: Activity) {
        activity.requestedOrientation = if (activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        else
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

}
