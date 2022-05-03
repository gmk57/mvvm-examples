package foo.bar.example.foreadapterskt.ui.playlist.immutable

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import foo.bar.example.foreadapterskt.databinding.ActivityPlaylistsListitemBinding
import foo.bar.example.foreadapterskt.feature.playlist.Track
import foo.bar.example.foreadapterskt.feature.playlist.TrackMutable
import foo.bar.example.foreadapterskt.feature.playlist.immutable.ImmutablePlaylistModel
import gmk57.helpers.BoundHolder
import gmk57.helpers.viewBinding

/**
 * Copyright © 2015-2021 early.co. All rights reserved.
 */
class ImmutablePlaylistAdapter(private val immutablePlaylistModel: ImmutablePlaylistModel) :
    ListAdapter<Track, ImmutablePlaylistAdapter.Holder>(Differ()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(parent)

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position))

    inner class Holder(parent: ViewGroup) : BoundHolder<ActivityPlaylistsListitemBinding>(
        parent.viewBinding(ActivityPlaylistsListitemBinding::inflate)
    ) {
        private lateinit var item: Track

        init {
            binding.trackIncreaseplaysButton.setOnClickListener {
                immutablePlaylistModel.increasePlaysForTrack(item.id)
            }
            binding.trackDecreaseplaysButton.setOnClickListener {
                immutablePlaylistModel.decreasePlaysForTrack(item.id)
            }
            binding.trackRemoveButton.setOnClickListener {
                immutablePlaylistModel.removeTrack(item.id)
            }
        }

        fun bind(item: Track) {
            this.item = item
            binding.apply {
                root.setBackgroundResource(item.colourResource)
                trackPlaysrequestedText.text = "${item.numberOfPlaysRequested}"
                trackIncreaseplaysButton.isEnabled = item.canIncreasePlays()
                trackDecreaseplaysButton.isEnabled = item.canDecreasePlays()
                trackPercentVbar.setPercentDone(
                    item.id,
                    (item.numberOfPlaysRequested * 100 / TrackMutable.MAX_PLAYS_REQUESTED).toFloat()
                )

            }
        }
    }

    private class Differ : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track) = oldItem.itemsTheSame(newItem)
        override fun areContentsTheSame(oldItem: Track, newItem: Track) =
            oldItem.itemsLookTheSame(newItem)
    }
}
