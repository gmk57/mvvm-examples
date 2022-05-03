package foo.bar.example.foreadapterskt.feature.playlist

import androidx.annotation.ColorRes
import co.early.fore.adapters.immutable.DiffComparator

// Generally we could make Track a data class and use `equals()` instead of `itemsLookTheSame()` &
// `DiffComparator`, but then `copy()` method could violate constructor validation in `invoke()`.
// `increasePlaysRequested/decreasePlaysRequested` would be required for readability anyway.
class Track private constructor(
    @param:ColorRes @field:ColorRes
    val colourResource: Int,
    val id: Long,
    val numberOfPlaysRequested: Int
) : DiffComparator<Track> {

    fun increasePlaysRequested() = invoke(colourResource, id, numberOfPlaysRequested + 1)

    fun decreasePlaysRequested() = invoke(colourResource, id, numberOfPlaysRequested - 1)

    fun canIncreasePlays(): Boolean {
        return numberOfPlaysRequested < MAX_PLAYS_REQUESTED
    }

    fun canDecreasePlays(): Boolean {
        return numberOfPlaysRequested > MIN_PLAYS_REQUESTED
    }

    override fun itemsTheSame(other: Track?): Boolean {
        return if (other != null) {
            this.id == other.id
        } else false
    }

    override fun itemsLookTheSame(other: Track?): Boolean {
        return if (other != null) {
            this.numberOfPlaysRequested == other.numberOfPlaysRequested
                    && this.colourResource == other.colourResource
        } else false
    }

    companion object {
        private const val MIN_PLAYS_REQUESTED = 1
        const val MAX_PLAYS_REQUESTED = 4

        // overloading invoke() function for constructor validation
        operator fun invoke(
            @ColorRes colourResource: Int,
            id: Long,
            numberOfPlaysRequested: Int = MIN_PLAYS_REQUESTED
        ) = Track(
            colourResource,
            id,
            numberOfPlaysRequested.coerceIn(MIN_PLAYS_REQUESTED, MAX_PLAYS_REQUESTED)
        )
    }
}
