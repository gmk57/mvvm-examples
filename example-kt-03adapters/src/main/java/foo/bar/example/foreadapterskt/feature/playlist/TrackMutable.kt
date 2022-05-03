package foo.bar.example.foreadapterskt.feature.playlist

import androidx.annotation.ColorRes
import co.early.fore.adapters.immutable.DeepCopyable
import co.early.fore.adapters.immutable.DiffComparator

// This is a renamed copy of old `Track` class to keep existing behavior in MutablePlaylistModel/Adapter & ListDifferPlaylistAdapter
class TrackMutable(
    @param:ColorRes @field:ColorRes
    val colourResource: Int,
    val id: Long,
    playsRequested: Int? = null
) : DiffComparator<TrackMutable>, DeepCopyable<TrackMutable> {

    var numberOfPlaysRequested: Int
        private set

    init {
        numberOfPlaysRequested = playsRequested?.let {
            it
        } ?: MIN_PLAYS_REQUESTED
    }

    fun increasePlaysRequested() {
        if (canIncreasePlays()) {
            numberOfPlaysRequested++
        }
    }

    fun decreasePlaysRequested() {
        if (canDecreasePlays()) {
            numberOfPlaysRequested--
        }
    }

    fun canIncreasePlays(): Boolean {
        return numberOfPlaysRequested < MAX_PLAYS_REQUESTED
    }

    fun canDecreasePlays(): Boolean {
        return numberOfPlaysRequested > MIN_PLAYS_REQUESTED
    }

    companion object {
        private const val MIN_PLAYS_REQUESTED = 1
        const val MAX_PLAYS_REQUESTED = 4
    }

    override fun itemsTheSame(other: TrackMutable?): Boolean {
        return if (other != null) {
            this.id == other.id
        } else false
    }

    override fun itemsLookTheSame(other: TrackMutable?): Boolean {
        return if (other != null) {
            this.numberOfPlaysRequested == other.numberOfPlaysRequested
                    && this.colourResource == other.colourResource
        } else false
    }

    override fun deepCopy(): TrackMutable {
        return TrackMutable(
            colourResource,
            id,
            numberOfPlaysRequested
        )
    }
}
