package foo.bar.example.forereactiveuikt.feature.wallet

import co.early.fore.kt.core.logging.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Copyright © 2015-2020 early.co. All rights reserved.
 */
class Wallet(private val logger: Logger) {

    private val _state = MutableStateFlow(WalletState())
    val state: StateFlow<WalletState> = _state

    fun increaseMobileWallet() = _state.update { currentState ->
        if (currentState.canIncrease()) {
            logger.i("Increasing mobile wallet to:${currentState.mobileWalletAmount + 1}")
            currentState.copy(mobileWalletAmount = (currentState.mobileWalletAmount + 1))
        } else currentState
    }

    fun decreaseMobileWallet() = _state.update { currentState ->
        if (currentState.canDecrease()) {
            logger.i("Decreasing mobile wallet to:${currentState.mobileWalletAmount - 1}")
            currentState.copy(mobileWalletAmount = (currentState.mobileWalletAmount - 1))
        } else currentState
    }
}

data class WalletState(
    val totalDollarsAvailable: Int = 10,
    val mobileWalletAmount: Int = 0
) {
    val savingsWalletAmount: Int
        get() = totalDollarsAvailable - mobileWalletAmount

    fun canIncrease(): Boolean = mobileWalletAmount < totalDollarsAvailable

    fun canDecrease(): Boolean = mobileWalletAmount > 0
}
