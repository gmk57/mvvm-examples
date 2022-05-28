package foo.bar.example.foreretrofitkt.feature.fruit

import co.early.fore.kt.core.logging.Logger
import foo.bar.example.foreretrofitkt.api.CustomGlobalErrorHandler
import foo.bar.example.foreretrofitkt.api.fruits.FruitPojo
import foo.bar.example.foreretrofitkt.api.fruits.FruitService
import foo.bar.example.foreretrofitkt.api.fruits.FruitsCustomError
import foo.bar.example.foreretrofitkt.message.ErrorMessage
import gmk57.helpers.Event
import gmk57.helpers.appScope
import gmk57.helpers.backgroundDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


/**
 * gets a list of fruit from the network, selects one at random to be currentFruit
 */
class FruitFetcher(
    private val fruitService: FruitService,
    private val errorHandler: CustomGlobalErrorHandler,
    private val logger: Logger
) {

    private val _state = MutableStateFlow(FruitFetcherState())
    val state: StateFlow<FruitFetcherState> = _state.asStateFlow()


    fun fetchFruitsAsync() {

        logger.i("fetchFruitsAsync() t:" + Thread.currentThread())

        if (state.value.isBusy) {
            _state.update { it.copy(error = Event(ErrorMessage.ERROR_BUSY)) }
            return
        }

        _state.update { it.copy(isBusy = true) }

        appScope.launch(Dispatchers.Main.immediate) {

            logger.i("about to start network call t:" + Thread.currentThread())

            try {
                val result = withContext(backgroundDispatcher) {  // not really needed for Retrofit

                    logger.i("processing call t:" + Thread.currentThread())

                    fruitService.getFruitsSimulateOk()
                }

                handleSuccess(result)

            } catch (e: Exception) {
                logger.e("call failed: $e")
                handleFailure(errorHandler.handleError(e))
            }
        }

    }


    /**
     * identical to fetchFruitsAsync() but for demo purposes the URL we point to will give us an error,
     * we also don't specify a custom error class here
     */
    fun fetchFruitsButFail() {

        logger.i("fetchFruitsButFail()")

        if (state.value.isBusy) {
            _state.update { it.copy(error = Event(ErrorMessage.ERROR_BUSY)) }
            return
        }

        _state.update { it.copy(isBusy = true) }


        appScope.launch(Dispatchers.Main.immediate) {

            try {
                val result = fruitService.getFruitsSimulateNotAuthorised()
                handleSuccess(result)

            } catch (e: Exception) {
                logger.e("call failed: $e")
                handleFailure(errorHandler.handleError(e))
            }
        }
    }


    /**
     * identical to fetchFruitsAsync() but for demo purposes the URL we point to will give us an error,
     * here we specify a custom error class for more detail about the error than just an HTTP code can give us
     */
    fun fetchFruitsButFailAdvanced() {

        logger.i("fetchFruitsButFailAdvanced()")

        if (state.value.isBusy) {
            _state.update { it.copy(error = Event(ErrorMessage.ERROR_BUSY)) }
            return
        }

        _state.update { it.copy(isBusy = true) }

        appScope.launch(Dispatchers.Main.immediate) {

            logger.i("about to start network call t:" + Thread.currentThread())

            try {
                val result = fruitService.getFruitsSimulateNotAuthorised()
                handleSuccess(result)

            } catch (e: Exception) {
                logger.e("call failed: $e, thread: ${Thread.currentThread()}")
                handleFailure(errorHandler.handleError(e, FruitsCustomError::class.java))
            }
        }
    }


    /**
     * Demonstration of how to chain multiple connection requests together in a simple way
     */
    fun chainedCall() {

        logger.i("chainedCall()")


        if (state.value.isBusy) {
            _state.update { it.copy(error = Event(ErrorMessage.ERROR_BUSY)) }
            return
        }

        _state.update { it.copy(isBusy = true) }

        appScope.launch(Dispatchers.Main.immediate) {

            /*
             * For chaining multiple calls we could use fore's `carryOn()` or `Result.flatMap()`
             * (see https://gist.github.com/gmk57/a407c9ce03833268ff91155004a1ed07), but fortunately
             * Kotlin's suspend functions allow us to wrap asynchronous operations in plain old
             * try/catch, and that's probably the simplest & most readable solution in this case
             */
            try {
                logger.i("...create user...")
                val user = fruitService.createUser()

                logger.i("...create user ticket...")
                val ticketRef = fruitService.createUserTicket(user.userId).ticketRef

                logger.i("...get waiting time...")
                val waitingTime = fruitService.getEstimatedWaitingTime(ticketRef)

                if (waitingTime.minutesWait > 10) {
                    logger.i("...cancel ticket...")
                    fruitService.cancelTicket(ticketRef)
                } else {
                    logger.i("...confirm ticket...")
                    fruitService.confirmTicket(ticketRef)
                }

                logger.i("...claim free fruit!...")
                val result = fruitService.claimFreeFruit(ticketRef)
                handleSuccess(result)

            } catch (e: Exception) {
                logger.e("call failed: $e")
                handleFailure(errorHandler.handleError(e, FruitsCustomError::class.java))
            }
        }
    }

    private fun handleSuccess(
        successResponse: List<FruitPojo>
    ) {

        logger.i("handleSuccess() t:" + Thread.currentThread())

        _state.update {
            it.copy(
                currentFruit = selectRandomFruit(successResponse),
                success = Event(Unit)
            )
        }
        complete()
    }

    private fun handleFailure(failureMessage: ErrorMessage) {

        logger.i("handleFailure(), message: $failureMessage, t:" + Thread.currentThread())

        _state.update { it.copy(error = Event(failureMessage)) }
        complete()
    }

    private fun complete() {

        logger.i("complete() t:" + Thread.currentThread())

        _state.update { it.copy(isBusy = false) }
    }

    private fun selectRandomFruit(listOfFruits: List<FruitPojo>): FruitPojo {
        return listOfFruits[if (listOfFruits.size == 1) 0 else random.nextInt(listOfFruits.size - 1)]
    }

    companion object {
        private val random = Random()
    }
}

data class FruitFetcherState(
    val isBusy: Boolean = false,
    val currentFruit: FruitPojo = FruitPojo("(fruitless)", false, 0),
    val success: Event<Unit>? = null,
    val error: Event<ErrorMessage>? = null,
)
