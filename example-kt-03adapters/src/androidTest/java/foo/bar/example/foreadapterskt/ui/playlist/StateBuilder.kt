package foo.bar.example.foreadapterskt.ui.playlist

import androidx.test.rule.ActivityTestRule
import co.early.fore.adapters.mutable.UpdateSpec
import co.early.fore.kt.core.delegate.Fore
import co.early.fore.kt.core.delegate.TestDelegateDefault
import foo.bar.example.foreadapterskt.OG
import foo.bar.example.foreadapterskt.feature.playlist.RandomStuffGeneratorUtil.generateRandomColourResource
import foo.bar.example.foreadapterskt.feature.playlist.Track
import foo.bar.example.foreadapterskt.feature.playlist.TrackMutable
import foo.bar.example.foreadapterskt.feature.playlist.immutable.ImmutablePlaylistModel
import foo.bar.example.foreadapterskt.feature.playlist.mutable.MutablePlaylistModel
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update


/**
 *
 */
class StateBuilder internal constructor(
    private val mockMutablePlaylistModel: MutablePlaylistModel,
    private val mockImmutablePlaylistModel: ImmutablePlaylistModel
) {
    private val tracks = MutableStateFlow(listOf<Track>())

    init {

        val updateSpec = UpdateSpec(UpdateSpec.UpdateType.FULL_UPDATE, 0, 0, mockk(relaxed = true))

        every {
            mockMutablePlaylistModel.getAndClearLatestUpdateSpec(any())
        } returns updateSpec

    }

    internal fun withUpdatablePlaylistHavingTracks(numberOfTracks: Int): StateBuilder {

        every {
            mockMutablePlaylistModel.itemCount
        } returns numberOfTracks

        every {
            mockMutablePlaylistModel.isEmpty()
        } returns (numberOfTracks == 0)

        val slot = CapturingSlot<Int>()
        every {
            mockMutablePlaylistModel.hasAtLeastNItems(capture(slot))
        } answers { numberOfTracks >= slot.captured }

        return this
    }

    internal fun withImmutablePlaylistHavingTracks(numberOfTracks: Int): StateBuilder {

        tracks.value = List(numberOfTracks) {
            Track(generateRandomColourResource(), it.toLong())
        }

        every {
            mockImmutablePlaylistModel.state
        } returns tracks

        return this
    }

    internal fun withPlaylistsContainingTrack(track: TrackMutable): StateBuilder {

        every {
            mockMutablePlaylistModel.getItem(any())
        } returns track

        tracks.update {
            it.map { Track(track.colourResource, track.id, track.numberOfPlaysRequested) }
        }

        return this
    }

    internal fun createRule(): ActivityTestRule<PlaylistsActivity> {

        return object : ActivityTestRule<PlaylistsActivity>(PlaylistsActivity::class.java) {
            override fun beforeActivityLaunched() {

                Fore.setDelegate(TestDelegateDefault())

                //inject our mocks so our UI layer will pick them up
                OG.putMock(MutablePlaylistModel::class.java, mockMutablePlaylistModel)
                OG.putMock(ImmutablePlaylistModel::class.java, mockImmutablePlaylistModel)
            }
        }
    }

}
