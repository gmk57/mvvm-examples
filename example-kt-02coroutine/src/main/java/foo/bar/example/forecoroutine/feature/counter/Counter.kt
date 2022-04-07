package foo.bar.example.forecoroutine.feature.counter


import co.early.fore.kt.core.logging.Logger
import gmk57.helpers.appScope
import gmk57.helpers.backgroundDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Copyright © 2019 early.co. All rights reserved.
 */
class Counter(
    private val logger: Logger
) {

    private val _state = MutableStateFlow(CounterState())
    val state: StateFlow<CounterState> = _state


    fun increaseBy20() {

        logger.i("increaseBy20() t:" + Thread.currentThread())

        if (state.value.isBusy) {
            return
        }

        _state.update { it.copy(isBusy = true) }

        appScope.launch(Dispatchers.Main.immediate) {

            val result = withContext(backgroundDispatcher) {
                doStuffInBackground(20)
            }

            doThingsWithTheResult(result)
        }
    }


    private suspend fun doStuffInBackground(countTo: Int): Int {

        logger.i("doStuffInBackground() t:" + Thread.currentThread())

        var totalIncrease = 0

        for (ii in 1..countTo) {

            delay(200)

            ++totalIncrease

            logger.i("-tick- t:" + Thread.currentThread())
        }

        return totalIncrease
    }


    private fun doThingsWithTheResult(result: Int) {

        logger.i("doThingsWithTheResult() t:" + Thread.currentThread())

        _state.update {
            it.copy(count = it.count + result, isBusy = false)
        }
    }
}
