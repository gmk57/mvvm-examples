package foo.bar.example.forecoroutine.ui


import android.os.Bundle
import androidx.core.view.isInvisible
import androidx.fragment.app.FragmentActivity
import foo.bar.example.forecoroutine.OG
import foo.bar.example.forecoroutine.R
import foo.bar.example.forecoroutine.feature.counter.CombinedState
import foo.bar.example.forecoroutine.feature.counter.Counter
import foo.bar.example.forecoroutine.feature.counter.CounterWithProgress
import gmk57.helpers.observe
import kotlinx.android.synthetic.main.activity_counter.*
import kotlinx.coroutines.flow.combine

/**
 * Copyright © 2019 early.co. All rights reserved.
 */
class CounterActivity : FragmentActivity(R.layout.activity_counter) {

    //models that we need to sync with
    private val counterWithProgress: CounterWithProgress = OG[CounterWithProgress::class.java]
    private val counter: Counter = OG[Counter::class.java]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupButtonClickListeners()

        counter.state.combine(counterWithProgress.state) { counterState, counterWithProgressState ->
            CombinedState(counterState, counterWithProgressState)
        }.observe(this, ::syncView)
    }

    private fun setupButtonClickListeners() {
        counter_increase_btn.setOnClickListener { counter.increaseBy20() }
        counterwprog_increase_btn.setOnClickListener { counterWithProgress.increaseBy20() }
    }


    //data binding stuff below

    fun syncView(state: CombinedState) = with(state) {
        counterwprog_increase_btn.isEnabled = !counterWithProgressState.isBusy
        counterwprog_busy_progress.isInvisible = !counterWithProgressState.isBusy
        counterwprog_progress_txt.text = "${counterWithProgressState.progress}"
        counterwprog_current_txt.text = "${counterWithProgressState.count}"

        counter_increase_btn.isEnabled = !counterState.isBusy
        counter_busy_progress.isInvisible = !counterState.isBusy
        counter_current_txt.text = "${counterState.count}"
    }
}
