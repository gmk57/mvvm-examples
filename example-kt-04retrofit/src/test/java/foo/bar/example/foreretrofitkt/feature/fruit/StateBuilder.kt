package foo.bar.example.foreretrofitkt.feature.fruit

import co.early.fore.net.MessageProvider
import foo.bar.example.foreretrofitkt.api.CustomGlobalErrorHandler
import foo.bar.example.foreretrofitkt.api.fruits.FruitPojo
import foo.bar.example.foreretrofitkt.api.fruits.FruitService
import foo.bar.example.foreretrofitkt.message.ErrorMessage
import io.mockk.coEvery
import io.mockk.every

/**
 *
 */
class StateBuilder internal constructor(
    private val fruitService: FruitService,
    private val errorHandler: CustomGlobalErrorHandler
) {

    internal fun getFruitSuccess(fruitPojo: FruitPojo): StateBuilder {

        coEvery {
            fruitService.getFruitsSimulateOk()
        } returns listOf(fruitPojo)

        return this
    }

    internal fun getFruitFail(errorMessage: ErrorMessage): StateBuilder {

        coEvery {
            fruitService.getFruitsSimulateNotAuthorised()
        } throws Exception()

        every {
            errorHandler.handleError(any(), any<Class<MessageProvider<ErrorMessage>>>())
        } returns errorMessage

        return this
    }
}
