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

    fun increaseMobileWallet() = updateMobileWallet { it + 1 }

    fun decreaseMobileWallet() = updateMobileWallet { it - 1 }

    private fun updateMobileWallet(action: (Int) -> Int) = _state.update {
        it.copy(
            mobileWalletAmount = action(it.mobileWalletAmount)
                .coerceIn(0..it.totalDollarsAvailable)
        ).also { logger.i("Updating mobile wallet to:${it.mobileWalletAmount}") }
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
