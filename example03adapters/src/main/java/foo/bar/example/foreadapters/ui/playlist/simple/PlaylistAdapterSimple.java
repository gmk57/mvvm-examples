package foo.bar.example.foreadapters.ui.playlist.simple;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.early.fore.core.Affirm;
import foo.bar.example.foreadapters.R;
import foo.bar.example.foreadapters.feature.playlist.PlaylistSimpleModel;
import foo.bar.example.foreadapters.feature.playlist.Track;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

/**
 *
 */
public class PlaylistAdapterSimple extends RecyclerView.Adapter<PlaylistAdapterSimple.ViewHolder>{

    private static final String TAG = PlaylistAdapterSimple.class.getSimpleName();

    private final PlaylistSimpleModel playlistSimpleModel;

    public PlaylistAdapterSimple(PlaylistSimpleModel playlistSimpleModel) {
        this.playlistSimpleModel = Affirm.notNull(playlistSimpleModel);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_playlists_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.itemView.setTag(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final Track item = playlistSimpleModel.getTrack(position);

        holder.increase.setOnClickListener(v -> {
            //if you tap very fast on different rows removing them
            //while you are using adapter animations you will crash unless
            //you check for this
            int betterPosition = holder.getAdapterPosition();
            if (betterPosition != NO_POSITION) {
                playlistSimpleModel.increasePlaysForTrack(betterPosition);
            }
        });

        holder.decrease.setOnClickListener(v -> {
            int betterPosition = holder.getAdapterPosition();
            if (betterPosition != NO_POSITION) {
                playlistSimpleModel.decreasePlaysForTrack(betterPosition);
            }
        });

        holder.remove.setOnClickListener(v -> {
            int betterPosition = holder.getAdapterPosition();
            if (betterPosition != NO_POSITION) {
                playlistSimpleModel.removeTrack(betterPosition);
            }
        });

        holder.itemView.setBackgroundResource(item.getColourResource());
        holder.playsRequested.setText("" + item.getNumberOfPlaysRequested());
        holder.increase.setEnabled(item.canIncreasePlays());
        holder.decrease.setEnabled(item.canDecreasePlays());
    }

    @Override
    public int getItemCount() {
        return playlistSimpleModel.getTrackListSize();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.track_playsrequested_text)
        protected TextView playsRequested;

        @BindView(R.id.track_increaseplays_button)
        protected Button increase;

        @BindView(R.id.track_decreaseplays_button)
        protected Button decrease;

        @BindView(R.id.track_remove_button)
        protected Button remove;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
