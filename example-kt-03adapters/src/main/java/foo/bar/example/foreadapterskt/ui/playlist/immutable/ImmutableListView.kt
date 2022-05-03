package foo.bar.example.foreadapterskt.ui.playlist.immutable

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.core.view.doOnAttach
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import co.early.fore.adapters.CrossFadeRemover
import co.early.fore.kt.core.logging.Logger
import foo.bar.example.foreadapterskt.OG
import foo.bar.example.foreadapterskt.feature.playlist.Track
import foo.bar.example.foreadapterskt.feature.playlist.immutable.ImmutablePlaylistModel
import gmk57.helpers.observe
import kotlinx.android.synthetic.main.view_playlists_immutable.view.*

/**
 * Copyright © 2015-2021 early.co. All rights reserved.
 */
class ImmutableListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    //models that we need to sync with
    private val immutablePlaylistModel: ImmutablePlaylistModel =
        OG[ImmutablePlaylistModel::class.java]
    private val logger: Logger = OG[Logger::class.java]

    private lateinit var immutablePlaylistAdapter: ImmutablePlaylistAdapter

    override fun onFinishInflate() {
        super.onFinishInflate()

        setupClickListeners()

        setupAdapters()

        doOnAttach {
            immutablePlaylistModel.state.observe(findViewTreeLifecycleOwner()!!) { syncView(it) }
        }
    }

    private fun setupClickListeners() {
        immutable_add_button.setOnClickListener { immutablePlaylistModel.addNTracks(1) }
        immutable_clear_button.setOnClickListener { immutablePlaylistModel.removeAllTracks() }
        immutable_add5_button.setOnClickListener { immutablePlaylistModel.addNTracks(5) }
        immutable_remove5_button.setOnClickListener { immutablePlaylistModel.removeNTracks(5) }
        immutable_add100_button.setOnClickListener { immutablePlaylistModel.addNTracks(100) }
        immutable_remove100_button.setOnClickListener { immutablePlaylistModel.removeNTracks(100) }
    }

    private fun setupAdapters() {
        immutablePlaylistAdapter = ImmutablePlaylistAdapter(immutablePlaylistModel)
        immutable_list_recycleview.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = immutablePlaylistAdapter
            itemAnimator = CrossFadeRemover()
            setHasFixedSize(true)
        }
    }

    private fun syncView(list: List<Track>) {
        logger.i("syncView()")
        immutable_totaltracks_textview.text = list.size.toString()
        immutable_clear_button.isEnabled = list.isNotEmpty()
        immutable_remove5_button.isEnabled = list.size >= 5
        immutable_remove100_button.isEnabled = list.size >= 100

        immutablePlaylistAdapter.submitList(list)
    }
}
