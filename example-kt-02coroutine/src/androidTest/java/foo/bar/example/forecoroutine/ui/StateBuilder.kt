package foo.bar.example.forecoroutine.ui

import androidx.test.rule.ActivityTestRule
import foo.bar.example.forecoroutine.OG
import foo.bar.example.forecoroutine.feature.counter.Counter
import foo.bar.example.forecoroutine.feature.counter.CounterState
import foo.bar.example.forecoroutine.feature.counter.CounterWithProgress
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow


/**
 * Copyright © 2019 early.co. All rights reserved.
 */
class StateBuilder internal constructor(
    private val mockCounter: Counter,
    private val mockCounterWithProgress: CounterWithProgress
) {
    private var mockCounterState = CounterState()
    private var mockCounterWithProgressState = CounterState()

    internal fun counterBasicIsBusy(busy: Boolean): StateBuilder {
        mockCounterState = mockCounterState.copy(isBusy = busy)
        return this
    }

    internal fun counterWithProgressIsBusy(busy: Boolean): StateBuilder {
        mockCounterWithProgressState = mockCounterWithProgressState.copy(isBusy = busy)
        return this
    }

    internal fun counterBasicCount(count: Int): StateBuilder {
        mockCounterState = mockCounterState.copy(count = count)
        return this
    }

    internal fun counterWithProgressCount(count: Int): StateBuilder {
        mockCounterWithProgressState = mockCounterWithProgressState.copy(count = count)
        return this
    }

    internal fun counterWithProgressProgressValue(value: Int): StateBuilder {
        mockCounterWithProgressState = mockCounterWithProgressState.copy(progress = value)
        return this
    }

    internal fun createRule(): ActivityTestRule<CounterActivity> {

        return object : ActivityTestRule<CounterActivity>(CounterActivity::class.java) {
            override fun beforeActivityLaunched() {

                every { mockCounter.state } returns MutableStateFlow(mockCounterState)
                every { mockCounterWithProgress.state } returns
                        MutableStateFlow(mockCounterWithProgressState)

                //inject our mocks so our UI layer will pick them up
                OG.putMock(Counter::class.java, mockCounter)
                OG.putMock(CounterWithProgress::class.java, mockCounterWithProgress)
            }

        }
    }

}
