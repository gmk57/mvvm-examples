package foo.bar.example.foreadapterskt.feature.playlist

import co.early.fore.kt.core.delegate.Fore
import co.early.fore.kt.core.delegate.TestDelegateDefault
import co.early.fore.kt.core.logging.SystemLogger
import foo.bar.example.foreadapterskt.feature.playlist.immutable.ImmutablePlaylistModel
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test


/**
 *
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ImmutablePlaylistModelTest {

    private val logger = SystemLogger()
    private lateinit var immutablePlaylistModel: ImmutablePlaylistModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        // make the code run synchronously, reroute Log.x to
        // System.out.println() so we see it in the test log
        Fore.setDelegate(TestDelegateDefault())

        immutablePlaylistModel = ImmutablePlaylistModel(logger)
    }

    @Test
    @Throws(Exception::class)
    fun initialConditions() {

        //arrange

        //act

        //assert
        val list = immutablePlaylistModel.state.value
        Assert.assertEquals(0, list.size)
    }


    @Test
    @Throws(Exception::class)
    fun addNewTrack() {

        //arrange

        //act
        immutablePlaylistModel.addNTracks(1)

        //assert
        val list = immutablePlaylistModel.state.value
        Assert.assertEquals(1, list.size)
        Assert.assertEquals(1, list[0].numberOfPlaysRequested)
    }


    @Test
    @Throws(Exception::class)
    fun removeTrack() {

        //arrange
        immutablePlaylistModel.addNTracks(1)

        //act
        immutablePlaylistModel.removeTrack(immutablePlaylistModel.state.value[0].id)

        //assert
        val list = immutablePlaylistModel.state.value
        Assert.assertEquals(0, list.size)
    }


    @Test
    @Throws(Exception::class)
    fun add5NewTracks() {

        //arrange

        //act
        immutablePlaylistModel.addNTracks(5)

        //assert
        val list = immutablePlaylistModel.state.value
        Assert.assertEquals(5, list.size)
        Assert.assertEquals(1, list[4].numberOfPlaysRequested)
    }


    @Test
    @Throws(Exception::class)
    fun remove5Tracks() {

        //arrange
        immutablePlaylistModel.addNTracks(5)

        //act
        immutablePlaylistModel.removeNTracks(5)

        //assert
        val list = immutablePlaylistModel.state.value
        Assert.assertEquals(0, list.size)
    }


    @Test
    @Throws(Exception::class)
    fun increasePlays() {

        //arrange
        immutablePlaylistModel.addNTracks(1)
        val firstTrackId = immutablePlaylistModel.state.value[0].id

        //act
        immutablePlaylistModel.increasePlaysForTrack(firstTrackId)

        //assert
        val list = immutablePlaylistModel.state.value
        Assert.assertEquals(2, list[0].numberOfPlaysRequested)
    }


    @Test
    @Throws(Exception::class)
    fun decreasePlays() {

        //arrange
        immutablePlaylistModel.addNTracks(1)
        val firstTrackId = immutablePlaylistModel.state.value[0].id
        immutablePlaylistModel.increasePlaysForTrack(firstTrackId)

        //act
        immutablePlaylistModel.decreasePlaysForTrack(firstTrackId)

        //assert
        val list = immutablePlaylistModel.state.value
        Assert.assertEquals(1, list[0].numberOfPlaysRequested)
    }


    @Test
    @Throws(Exception::class)
    fun removeAllTracks() {

        //arrange
        immutablePlaylistModel.addNTracks(5)
        immutablePlaylistModel.addNTracks(1)
        immutablePlaylistModel.addNTracks(1)

        //act
        immutablePlaylistModel.removeAllTracks()

        //assert
        val list = immutablePlaylistModel.state.value
        Assert.assertEquals(0, list.size)
    }


    /**
     *
     * NB all we are checking here is that observers are called AT LEAST twice (first time should
     * happen immediately after subscription to StateFlow, so "at least once" test would not verify
     * anything about `addNTracks`).
     *
     * We don't really want tie our tests (OR any observers in production code)
     * to an expected number of times this method might be called. (This would be
     * testing an implementation detail and make the tests unnecessarily brittle)
     *
     * Using UnconfinedTestDispatcher is recommended for this case, see
     * https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-test/MIGRATION.md#testcoroutinedispatcher-for-testing-intermediate-emissions
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun observersNotifiedAtLeastOnceForAddTrack() = runTest(dispatchTimeoutMs = 1000) {

        //arrange
        val mockObserver: FlowCollector<List<Track>> = mockk(relaxed = true)
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            immutablePlaylistModel.state.collect(mockObserver)
        }

        //act
        immutablePlaylistModel.addNTracks(1)
        job.cancel()  // prevents leaking infinite collection of StateFlow

        //assert
        coVerify(atLeast = 2) {
            mockObserver.emit(any())
        }
    }

    /**
     * Another approach would be to test that state has changed after calling `addNTracks`.
     *
     * If state has changed, but our observers weren't called, this is probably a bug either in
     * registering them or in StateFlow, but not in ImmutablePlaylistModel.
     */
    @Test
    fun stateChangedForAddTrack() = runTest {

        //arrange
        val initialState = immutablePlaylistModel.state.value

        //act
        immutablePlaylistModel.addNTracks(1)

        //assert
        Assert.assertNotEquals(initialState, immutablePlaylistModel.state.value)
    }


    @Test
    @Throws(Exception::class)
    fun observersNotifiedAtLeastOnceForIncreasePlays() = runTest(dispatchTimeoutMs = 1000) {

        //arrange
        immutablePlaylistModel.addNTracks(1)
        val mockObserver: FlowCollector<List<Track>> = mockk(relaxed = true)
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            immutablePlaylistModel.state.collect(mockObserver)
        }
        val firstTrackId = immutablePlaylistModel.state.value[0].id

        //act
        immutablePlaylistModel.increasePlaysForTrack(firstTrackId)
        job.cancel()  // prevents leaking infinite collection of StateFlow

        //assert
        coVerify(atLeast = 2) {
            mockObserver.emit(any())
        }
    }

    @Test
    fun stateChangedForIncreasePlay() = runTest {

        //arrange
        immutablePlaylistModel.addNTracks(1)
        val initialState = immutablePlaylistModel.state.value
        val firstTrackId = initialState[0].id

        //act
        immutablePlaylistModel.increasePlaysForTrack(firstTrackId)

        //assert
        Assert.assertNotEquals(initialState, immutablePlaylistModel.state.value)
    }
}
