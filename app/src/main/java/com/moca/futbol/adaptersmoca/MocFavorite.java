package com.moca.futbol.adaptersmoca;



import static com.moca.futbol.helpermoca.MocConstant.CHANNEL_GRID_2_COLUMN;
import static com.moca.futbol.helpermoca.MocConstant.CHANNEL_GRID_3_COLUMN;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.moca.futbol.MocConf;
import com.moca.futbol.R;
import com.moca.futbol.dataprefsmoca.prefs.SharedPrefs;
import com.moca.futbol.modelmoca.Channel;


import java.util.List;

public class MocFavorite extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;
    private List<Channel> items;
    private boolean loading;
    private OnItemClickListener mOnItemClickListener;
    private OnItemOverflowClickListener mOnItemOverflowClickListener;
    SharedPrefs mySharedPref;

    public interface OnItemClickListener {
        void onItemClick(View view, Channel obj, int position);
    }

    public interface OnItemOverflowClickListener {
        void onItemOverflowClick(View view, Channel obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public void setOnItemOverflowClickListener(final OnItemOverflowClickListener mItemOverflowClickListener) {
        this.mOnItemOverflowClickListener = mItemOverflowClickListener;
    }

    public MocFavorite(Context context, RecyclerView view, List<Channel> items) {
        this.items = items;
        this.context = context;
        this.mySharedPref = new SharedPrefs(context);
        lastItemViewDetector(view);
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public TextView channel_name;
        public TextView channel_category;
        public ImageView channel_image;
        public LinearLayout lyt_parent;
        public ImageView overflow;

        private OriginalViewHolder(View v) {
            super(v);
            channel_name = v.findViewById(R.id.my_channel_name);
            channel_category = v.findViewById(R.id.my_channel_category);
            channel_image = v.findViewById(R.id.my_channel_image);
            lyt_parent = v.findViewById(R.id.my_lyt_parent);
            overflow = v.findViewById(R.id.overflow);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.loadMore);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_ITEM:
                SharedPrefs sharedPref = new SharedPrefs(context);
                if (sharedPref.getChannelViewType() == CHANNEL_GRID_2_COLUMN || sharedPref.getChannelViewType() == CHANNEL_GRID_3_COLUMN) {
                    View menuItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.pobr_item_post_grid, parent, false);
                    return new OriginalViewHolder(menuItemView);
                } else {
                    View menuItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.pobr_item_post, parent, false);
                    return new OriginalViewHolder(menuItemView);
                }
            case VIEW_PROG:
                // fall through
            default:
                View loadMoreView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_loading, parent, false);
                return new ProgressViewHolder(loadMoreView);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_ITEM:
                final Channel p = items.get(position);
                final OriginalViewHolder vItem = (OriginalViewHolder) holder;

                vItem.channel_name.setText(p.channel_name);

                vItem.channel_category.setText(p.category_name);
                if (MocConf.ENABLE_CHANNEL_LIST_CATEGORY_NAME) {
                    vItem.channel_category.setVisibility(View.VISIBLE);
                } else {
                    vItem.channel_category.setVisibility(View.GONE);
                }


                Glide.with(context)
                        .load(mySharedPref.getBaseUrl() + "/upload/" + p.channel_image.replace(" ", "%20"))
                        .placeholder(R.drawable.ic_thumbnail)
                        .override(context.getResources().getDimensionPixelSize(R.dimen.list_image_width), context.getResources().getDimensionPixelSize(R.dimen.list_image_height))
                        .centerCrop()
                        .into(vItem.channel_image);


                vItem.lyt_parent.setOnClickListener(view -> {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, p, position);
                    }
                });

                vItem.overflow.setOnClickListener(view -> {
                    if (mOnItemOverflowClickListener != null) {
                        mOnItemOverflowClickListener.onItemOverflowClick(view, p, position);
                    }
                });

                break;
            case VIEW_PROG:
                //fall through
            default:
                ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {

        if (items.get(position) != null) {
            return VIEW_ITEM;
        } else {
            return VIEW_PROG;
        }

    }

    public void insertData(List<Channel> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (items.get(i) == null) {
                items.remove(i);
                notifyItemRemoved(i);
            }
        }
    }


    public void resetListData() {
        this.items.clear();
        notifyDataSetChanged();
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastPos = layoutManager.findLastVisibleItemPosition();
                    if (!loading && lastPos == getItemCount() - 1) {

                        loading = true;
                    }
                }
            });
        }
    }


}