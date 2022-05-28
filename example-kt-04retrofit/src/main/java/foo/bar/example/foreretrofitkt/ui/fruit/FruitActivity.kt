package foo.bar.example.foreretrofitkt.ui.fruit


import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import foo.bar.example.foreretrofitkt.OG
import foo.bar.example.foreretrofitkt.R
import foo.bar.example.foreretrofitkt.feature.fruit.FruitFetcher
import foo.bar.example.foreretrofitkt.feature.fruit.FruitFetcherState
import foo.bar.example.foreretrofitkt.message.ErrorMessage
import gmk57.helpers.observe
import kotlinx.android.synthetic.main.activity_fruit.*

class FruitActivity : FragmentActivity(R.layout.activity_fruit) {


    //models that we need to sync with
    private val fruitFetcher: FruitFetcher = OG[FruitFetcher::class.java]


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupButtonClickListeners()

        fruitFetcher.state.observe(this, ::syncView)
    }


    private fun setupButtonClickListeners() {
        fruit_fetchsuccess_btn.setOnClickListener { fruitFetcher.fetchFruitsAsync() }
        fruit_fetchchainsuccess_btn.setOnClickListener { fruitFetcher.chainedCall() }
        fruit_fetchfailbasic_btn.setOnClickListener { fruitFetcher.fetchFruitsButFail() }
        fruit_fetchfailadvanced_btn.setOnClickListener { fruitFetcher.fetchFruitsButFailAdvanced() }
    }


    //data binding stuff below

    private fun syncView(state: FruitFetcherState) {
        fruit_fetchsuccess_btn.isEnabled = !state.isBusy
        fruit_fetchchainsuccess_btn.isEnabled = !state.isBusy
        fruit_fetchfailbasic_btn.isEnabled = !state.isBusy
        fruit_fetchfailadvanced_btn.isEnabled = !state.isBusy
        fruit_name_textview.text = state.currentFruit.name
        fruit_citrus_img.setImageResource(
            if (state.currentFruit.isCitrus)
                R.drawable.lemon_positive else R.drawable.lemon_negative
        )
        fruit_tastyrating_tastybar.setTastyPercent(state.currentFruit.tastyPercentScore.toFloat())
        fruit_tastyrating_textview.text =
            getString(R.string.fruit_percent, state.currentFruit.tastyPercentScore)
        fruit_busy_progbar.isVisible = state.isBusy
        fruit_detailcontainer_linearlayout.isVisible = !state.isBusy

        state.success?.consume()?.let { showToast("Success!") }
        state.error?.consume()?.let { userMessage -> showToast(userMessage) }
    }
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.showToast(message: ErrorMessage) {
    Toast.makeText(this, message.localisedMessage, Toast.LENGTH_LONG).show()
}
