package foo.bar.example.forereactiveuikt.ui.wallet

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import foo.bar.example.forereactiveuikt.OG
import foo.bar.example.forereactiveuikt.R
import foo.bar.example.forereactiveuikt.feature.wallet.Wallet
import foo.bar.example.forereactiveuikt.feature.wallet.WalletState
import gmk57.helpers.observe
import kotlinx.android.synthetic.main.activity_wallet.*

/**
 * Copyright © 2015-2020 early.co. All rights reserved.
 */
class WalletsActivity : FragmentActivity(R.layout.activity_wallet) {

    //models that we need to sync with
    private val wallet: Wallet = OG[Wallet::class.java]


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupButtonClickListeners()

        wallet.state.observe(this, ::syncView)
    }

    private fun setupButtonClickListeners() {
        wallet_increase_btn.setOnClickListener {
            wallet.increaseMobileWallet() //notice how the reactive ui takes care of updating the view for you
        }
        wallet_decrease_btn.setOnClickListener {
            wallet.decreaseMobileWallet() //notice how the reactive ui takes care of updating the view for you
        }
    }

    //reactive UI stuff below
    private fun syncView(state: WalletState) {
        wallet_increase_btn.isEnabled = state.canIncrease()
        wallet_decrease_btn.isEnabled = state.canDecrease()
        wallet_mobileamount_txt.text = state.mobileWalletAmount.toString()
        wallet_savingsamount_txt.text = state.savingsWalletAmount.toString()
    }
}
