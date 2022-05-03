package foo.bar.example.foreadapterskt.ui.playlist

import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isNotEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import foo.bar.example.foreadapterskt.R
import foo.bar.example.foreadapterskt.feature.playlist.TrackMutable
import foo.bar.example.foreadapterskt.feature.playlist.immutable.ImmutablePlaylistModel
import foo.bar.example.foreadapterskt.feature.playlist.mutable.MutablePlaylistModel
import foo.bar.example.foreadapterskt.ui.EspressoTestMatchers
import foo.bar.example.foreadapterskt.ui.EspressoTestMatchers.onRecyclerViewItem
import foo.bar.example.foreadapterskt.ui.EspressoTestMatchers.scrollToPosition
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Here we make sure that the view elements accurately reflect the state of the models
 * and that clicking the buttons results in the correct action being performed
 */
@RunWith(AndroidJUnit4::class)
class PlaylistsViewTest {


    @MockK
    private lateinit var mockImmutablePlaylistModel: ImmutablePlaylistModel
    @MockK
    private lateinit var mockMutablePlaylistModel: MutablePlaylistModel


    @Before
    fun setUp() = MockKAnnotations.init(this, relaxed = true)


    @Test
    @Throws(Exception::class)
    fun emptyPlaylists() {

        //arrange
        StateBuilder(mockMutablePlaylistModel, mockImmutablePlaylistModel)
            .withUpdatablePlaylistHavingTracks(0)
            .withImmutablePlaylistHavingTracks(0)
            .createRule()
            .launchActivity(null)

        //act

        //assert
        onView(withId(R.id.updatable_add_button)).check(matches(isEnabled()))
        onView(withId(R.id.updatable_add5_button)).check(matches(isEnabled()))
        onView(withId(R.id.updatable_remove5_button)).check(matches(not<View>(isEnabled())))
        onView(withId(R.id.updatable_clear_button)).check(matches(not<View>(isEnabled())))
        onView(withId(R.id.updatable_totaltracks_textview)).check(matches(withText("0")))

        onView(withId(R.id.immutable_add_button)).check(matches(isEnabled()))
        onView(withId(R.id.immutable_add5_button)).check(matches(isEnabled()))
        onView(withId(R.id.immutable_remove5_button)).check(matches(not<View>(isEnabled())))
        onView(withId(R.id.immutable_clear_button)).check(matches(not<View>(isEnabled())))
        onView(withId(R.id.immutable_totaltracks_textview)).check(matches(withText("0")))

        onView(withId(R.id.updatable_list_recycleview))
            .check(matches(EspressoTestMatchers.withRecyclerViewItems(0)))
        onView(withId(R.id.immutable_list_recycleview))
            .check(matches(EspressoTestMatchers.withRecyclerViewItems(0)))
    }


    @Test
    @Throws(Exception::class)
    fun with3ItemsInEachPlaylist() {

        //arrange
        StateBuilder(mockMutablePlaylistModel, mockImmutablePlaylistModel)
            .withUpdatablePlaylistHavingTracks(3)
            .withImmutablePlaylistHavingTracks(3)
            .withPlaylistsContainingTrack(TrackMutable(R.color.pastel1, 123))
            .createRule()
            .launchActivity(null)

        //act

        //assert
        onView(withId(R.id.updatable_add_button)).check(matches(isEnabled()))
        onView(withId(R.id.immutable_add_button)).check(matches(isEnabled()))
        onView(withId(R.id.updatable_clear_button)).check(matches(isEnabled()))
        onView(withId(R.id.immutable_clear_button)).check(matches(isEnabled()))

        onView(withId(R.id.updatable_add5_button)).check(matches(isEnabled()))
        onView(withId(R.id.immutable_add5_button)).check(matches(isEnabled()))
        onView(withId(R.id.updatable_remove5_button)).check(matches(not<View>(isEnabled())))
        onView(withId(R.id.immutable_remove5_button)).check(matches(not<View>(isEnabled())))

        onView(withId(R.id.updatable_totaltracks_textview)).check(matches(withText("3")))
        onView(withId(R.id.immutable_totaltracks_textview)).check(matches(withText("3")))

        onView(withId(R.id.updatable_list_recycleview))
            .check(matches(EspressoTestMatchers.withRecyclerViewItems(3)))
        onView(withId(R.id.immutable_list_recycleview))
            .check(matches(EspressoTestMatchers.withRecyclerViewItems(3)))
    }


    @Test
    @Throws(Exception::class)
    fun with5ItemsInEachPlaylist() {

        //arrange
        StateBuilder(mockMutablePlaylistModel, mockImmutablePlaylistModel)
            .withUpdatablePlaylistHavingTracks(5)
            .withImmutablePlaylistHavingTracks(5)
            .withPlaylistsContainingTrack(TrackMutable(R.color.pastel1, 123))
            .createRule()
            .launchActivity(null)

        //act

        //assert
        onView(withId(R.id.updatable_add_button)).check(matches(isEnabled()))
        onView(withId(R.id.immutable_add_button)).check(matches(isEnabled()))
        onView(withId(R.id.updatable_clear_button)).check(matches(isEnabled()))
        onView(withId(R.id.immutable_clear_button)).check(matches(isEnabled()))

        onView(withId(R.id.updatable_add5_button)).check(matches(isEnabled()))
        onView(withId(R.id.immutable_add5_button)).check(matches(isEnabled()))
        onView(withId(R.id.updatable_remove5_button)).check(matches(isEnabled()))
        onView(withId(R.id.immutable_remove5_button)).check(matches(isEnabled()))

        onView(withId(R.id.updatable_totaltracks_textview)).check(matches(withText("5")))
        onView(withId(R.id.immutable_totaltracks_textview)).check(matches(withText("5")))

        onView(withId(R.id.updatable_list_recycleview))
            .check(matches(EspressoTestMatchers.withRecyclerViewItems(5)))
        onView(withId(R.id.immutable_list_recycleview))
            .check(matches(EspressoTestMatchers.withRecyclerViewItems(5)))
    }


    @Test
    @Throws(Exception::class)
    fun withDifferentItemsInEachPlaylist() {

        //arrange
        StateBuilder(mockMutablePlaylistModel, mockImmutablePlaylistModel)
            .withImmutablePlaylistHavingTracks(0)
            .withUpdatablePlaylistHavingTracks(5)
            .withPlaylistsContainingTrack(TrackMutable(R.color.pastel1, 123))
            .createRule()
            .launchActivity(null)

        //act

        //assert
        onView(withId(R.id.immutable_clear_button)).check(matches(not<View>(isEnabled())))
        onView(withId(R.id.updatable_clear_button)).check(matches(isEnabled()))

        onView(withId(R.id.immutable_remove5_button)).check(matches(not<View>(isEnabled())))
        onView(withId(R.id.updatable_remove5_button)).check(matches(isEnabled()))

        onView(withId(R.id.immutable_totaltracks_textview)).check(matches(withText("0")))
        onView(withId(R.id.updatable_totaltracks_textview)).check(matches(withText("5")))

        onView(withId(R.id.immutable_list_recycleview))
            .check(matches(EspressoTestMatchers.withRecyclerViewItems(0)))
        onView(withId(R.id.updatable_list_recycleview))
            .check(matches(EspressoTestMatchers.withRecyclerViewItems(5)))
    }


    @Test
    @Throws(Exception::class)
    fun stateMaintainedAfterRotation() {

        //arrange
        val activity = StateBuilder(mockMutablePlaylistModel, mockImmutablePlaylistModel)
            .withUpdatablePlaylistHavingTracks(3)
            .withImmutablePlaylistHavingTracks(3)
            .withPlaylistsContainingTrack(TrackMutable(R.color.pastel1, 123))
            .createRule()
            .launchActivity(null)
        activity.requestedOrientation = SCREEN_ORIENTATION_LANDSCAPE

        onView(withId(R.id.updatable_add5_button)).check(matches(isEnabled()))
        onView(withId(R.id.updatable_remove5_button)).check(matches(not<View>(isEnabled())))
        onView(withId(R.id.updatable_totaltracks_textview)).check(matches(withText("3")))

        //act
        activity.requestedOrientation = SCREEN_ORIENTATION_PORTRAIT

        //assert
        onView(withId(R.id.updatable_add5_button)).check(matches(isEnabled()))
        onView(withId(R.id.updatable_remove5_button)).check(matches(not<View>(isEnabled())))
        onView(withId(R.id.updatable_totaltracks_textview)).check(matches(withText("3")))
    }

    @Test
    fun listItemReflectsTrackState() {

        //arrange
        StateBuilder(mockMutablePlaylistModel, mockImmutablePlaylistModel)
            .withUpdatablePlaylistHavingTracks(50)
            .withImmutablePlaylistHavingTracks(50)
            .withPlaylistsContainingTrack(TrackMutable(R.color.pastel1, 123, 4))
            .createRule()
            .launchActivity(null)

        //act
        onView(withId(R.id.updatable_list_recycleview)).perform(scrollToPosition(40))
        onView(withId(R.id.immutable_list_recycleview)).perform(scrollToPosition(40))

        //assert
        onView(withId(R.id.updatable_list_recycleview))
            .check(matches(EspressoTestMatchers.withRecyclerViewItems(50)))
        onView(withId(R.id.immutable_list_recycleview))
            .check(matches(EspressoTestMatchers.withRecyclerViewItems(50)))

        onRecyclerViewItem(R.id.updatable_list_recycleview, 40, R.id.track_playsrequested_text)
            .check(matches(withText("4")))
        onRecyclerViewItem(R.id.updatable_list_recycleview, 40, R.id.track_decreaseplays_button)
            .check(matches(isEnabled()))
        onRecyclerViewItem(R.id.updatable_list_recycleview, 40, R.id.track_increaseplays_button)
            .check(matches(isNotEnabled()))

        onRecyclerViewItem(R.id.immutable_list_recycleview, 40, R.id.track_playsrequested_text)
            .check(matches(withText("4")))
        onRecyclerViewItem(R.id.immutable_list_recycleview, 40, R.id.track_decreaseplays_button)
            .check(matches(isEnabled()))
        onRecyclerViewItem(R.id.immutable_list_recycleview, 40, R.id.track_increaseplays_button)
            .check(matches(isNotEnabled()))
    }

    @Test
    @Throws(Exception::class)
    fun clickAddTrackImmutableCallsModel() {
        //arrange
        StateBuilder(mockMutablePlaylistModel, mockImmutablePlaylistModel)
            .withUpdatablePlaylistHavingTracks(0)
            .withImmutablePlaylistHavingTracks(0)
            .createRule()
            .launchActivity(null)

        //act
        onView(withId(R.id.immutable_add_button)).perform(click())

        //assert
        verify(exactly = 1) {
            mockImmutablePlaylistModel.addNTracks(1)
        }
    }


    @Test
    @Throws(Exception::class)
    fun clickAddTrackUpdatableCallsModel() {
        //arrange
        StateBuilder(mockMutablePlaylistModel, mockImmutablePlaylistModel)
            .withUpdatablePlaylistHavingTracks(0)
            .withImmutablePlaylistHavingTracks(0)
            .createRule()
            .launchActivity(null)

        //act
        onView(withId(R.id.updatable_add_button)).perform(click())

        //assert
        verify(exactly = 1) {
            mockMutablePlaylistModel.addNTracks(1)
        }
    }

}
