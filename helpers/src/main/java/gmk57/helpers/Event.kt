package gmk57.helpers

/**
 * Wrapper for one-time event, allowing it to be exposed as state via StateFlow/LiveData.
 * See: https://medium.com/google-developers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150
 */
class Event<T>(private val content: T) {
    private var consumed = false

    fun peek(): T = content

    fun consume(): T? {
        return if (consumed) {
            null
        } else {
            consumed = true
            content
        }
    }
}
