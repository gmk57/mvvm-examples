package foo.bar.example.foreadapterskt.feature.playlist.immutable

import co.early.fore.kt.core.logging.Logger
import foo.bar.example.foreadapterskt.feature.playlist.RandomStuffGeneratorUtil.generateRandomColourResource
import foo.bar.example.foreadapterskt.feature.playlist.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Example model based on **immutable** list data
 *
 * Copyright © 2015-2021 early.co. All rights reserved.
 */
class ImmutablePlaylistModel(private val logger: Logger) {
    private val _state = MutableStateFlow(emptyList<Track>())
    val state: StateFlow<List<Track>> = _state
    private var lastId: Long = 0

    fun removeTrack(id: Long) {
        logger.i("removeTrack() $id")
        _state.update { list ->
            list.filter { it.id != id }
        }
    }

    fun removeAllTracks() {
        logger.i("removeAllTracks()")
        _state.update { emptyList() }
    }

    fun increasePlaysForTrack(id: Long) {
        logger.i("increasePlaysForTrack() $id")
        _state.update { list ->
            list.map {
                if (it.id == id) it.increasePlaysRequested() else it
            }
        }
    }

    fun decreasePlaysForTrack(id: Long) {
        logger.i("decreasePlaysForTrack() $id")
        _state.update { list ->
            list.map {
                if (it.id == id) it.decreasePlaysRequested() else it
            }
        }
    }

    fun addNTracks(n: Int) {
        logger.i("addNTracks() n:$n")

        val newTracks = List(n) {
            Track(generateRandomColourResource(), lastId + it)
        }
        lastId += n
        _state.update { list ->
            list + newTracks
        }
    }

    fun removeNTracks(n: Int) {
        logger.i("removeNTracks() n:$n")
        _state.update { list ->
            list.drop(n)
        }
    }
}
