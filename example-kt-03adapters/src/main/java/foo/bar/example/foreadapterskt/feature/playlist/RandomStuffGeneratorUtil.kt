package foo.bar.example.foreadapterskt.feature.playlist

import foo.bar.example.foreadapterskt.R
import java.util.*


object RandomStuffGeneratorUtil {

    private val random = Random()

    private val colours = intArrayOf(R.color.pastel1, R.color.pastel2, R.color.pastel3, R.color.pastel4, R.color.pastel5)

    fun generateRandomColourResource(): Int {
        return colours.random()
    }

    fun randomLong(): Long {
        return random.nextLong()
    }
}
