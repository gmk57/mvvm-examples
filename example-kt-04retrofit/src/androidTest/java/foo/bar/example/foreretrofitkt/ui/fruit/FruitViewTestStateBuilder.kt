package foo.bar.example.foreretrofitkt.ui.fruit

import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.ActivityTestRule
import foo.bar.example.foreretrofitkt.App
import foo.bar.example.foreretrofitkt.OG
import foo.bar.example.foreretrofitkt.ProgressBarIdler
import foo.bar.example.foreretrofitkt.api.fruits.FruitPojo
import foo.bar.example.foreretrofitkt.feature.fruit.FruitFetcher
import foo.bar.example.foreretrofitkt.feature.fruit.FruitFetcherState
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow


class FruitViewTestStateBuilder internal constructor(private val mockFruitFetcher: FruitFetcher) {

    private var fruitFetcherState = FruitFetcherState()

    internal fun isBusy(busy: Boolean): FruitViewTestStateBuilder {
        fruitFetcherState = fruitFetcherState.copy(isBusy = busy)
        return this
    }

    internal fun hasFruit(fruitPojo: FruitPojo): FruitViewTestStateBuilder {
        fruitFetcherState = fruitFetcherState.copy(currentFruit = fruitPojo)
        return this
    }

    internal fun createRule(): ActivityTestRule<FruitActivity> {

        return object : ActivityTestRule<FruitActivity>(FruitActivity::class.java) {
            override fun beforeActivityLaunched() {

                every { mockFruitFetcher.state } returns MutableStateFlow(fruitFetcherState)

                //get hold of the application
                val app = ApplicationProvider.getApplicationContext() as App
                app.registerActivityLifecycleCallbacks(ProgressBarIdler())

                //inject our mocks so our UI layer will pick them up
                OG.setApplication(app)
                OG.putMock(FruitFetcher::class.java, mockFruitFetcher)
            }
        }
    }

}
