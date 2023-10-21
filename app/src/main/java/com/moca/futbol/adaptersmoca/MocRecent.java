package com.moca.futbol.adaptersmoca;


import static com.moca.futbol.MocConf.LOAD_MORE;
import static com.moca.futbol.helpermoca.MocConstant.CHANNEL_GRID_2_COLUMN;
import static com.moca.futbol.helpermoca.MocConstant.CHANNEL_GRID_3_COLUMN;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.moca.futbol.modelmoca.MocFirstAds;
import com.moca.futbol.modelmoca.MocSecAds;
import com.bumptech.glide.Glide;

import com.moca.futbol.MocConf;
import com.moca.futbol.R;
import com.moca.futbol.dataprefsmoca.prefs.SharedPrefs;
import com.moca.futbol.modelmoca.Channel;
import com.moca.futbol.adsmoca.AdsNativeViewHolder;

import java.util.Iterator;
import java.util.List;

public class MocRecent extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_PROG = 0;
    private final int VIEW_ITEM = 1;
    private final int VIEW_AD = 2;
    private final List<Channel> items;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    Context context;
    private OnItemClickListener mOnItemClickListener;
    private OnItemOverflowClickListener mOnItemOverflowClickListener;
    boolean scrolling = false;
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

    public MocRecent(Context context, RecyclerView view, List<Channel> items) {
        this.items = items;
        this.context = context;
        this.mySharedPref = new SharedPrefs(context);

        lastItemViewDetector(view);
        view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    scrolling = true;
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    scrolling = false;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    public static class OriginalViewHolder extends RecyclerView.ViewHolder {

        public TextView myChannel_name;
        public TextView myChannel_category;
        public ImageView myChannel_image;
        public LinearLayout myLyt_parent;
        public ImageView overflow;

        private OriginalViewHolder(View v) {
            super(v);
            myChannel_name = v.findViewById(R.id.my_channel_name);
            myChannel_category = v.findViewById(R.id.my_channel_category);
            myChannel_image = v.findViewById(R.id.my_channel_image);
            myLyt_parent = v.findViewById(R.id.my_lyt_parent);
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
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            if (mySharedPref.getChannelViewType() == CHANNEL_GRID_2_COLUMN || mySharedPref.getChannelViewType() == CHANNEL_GRID_3_COLUMN) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pobr_item_post_grid, parent, false);
                vh = new OriginalViewHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pobr_item_post, parent, false);
                vh = new OriginalViewHolder(v);
            }
        } else if (viewType == VIEW_AD) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.native_layout, parent, false);
            vh = new AdsNativeViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_loading, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;

    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final Channel p = (Channel) items.get(position);
            final OriginalViewHolder vItem = (OriginalViewHolder) holder;

            vItem.myChannel_name.setText(p.channel_name);

            vItem.myChannel_category.setText(p.category_name);
            if (MocConf.ENABLE_CHANNEL_LIST_CATEGORY_NAME) {
                vItem.myChannel_category.setVisibility(View.VISIBLE);
            } else {
                vItem.myChannel_category.setVisibility(View.GONE);
            }


            Glide.with(context)
                    .load(mySharedPref.getBaseUrl() + "/upload/" + p.channel_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .override(context.getResources().getDimensionPixelSize(R.dimen.list_image_width),
                            context.getResources().getDimensionPixelSize(R.dimen.list_image_height))
                    .centerCrop()
                    .into(vItem.myChannel_image);


            vItem.myLyt_parent.setOnClickListener(view -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, p, position);
                }
            });

            vItem.overflow.setOnClickListener(view -> {
                if (mOnItemOverflowClickListener != null) {
                    mOnItemOverflowClickListener.onItemOverflowClick(view, p, position);
                }
            });

        } else if (holder instanceof AdsNativeViewHolder) {

            final AdsNativeViewHolder vItem = (AdsNativeViewHolder) holder;
            vItem.loadNativeAd(context, MocFirstAds.frag_recent_native, MocSecAds.frag_recent_native);
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }

        if (getItemViewType(position) == VIEW_PROG || getItemViewType(position) == VIEW_AD) {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(true);
        } else {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(false);
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        Channel channel = items.get(position);
        if (channel != null) {
            if (channel.channel_name == null || channel.channel_name.equals("")) {
                return VIEW_AD;
            }
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

    public void insertDataWithNativeAd(List<Channel> items) {
        setLoaded();
        int positionStart = getItemCount();
        for (Channel post : items) {
            Log.d("item", "TITLE: " + post.channel_name);
        }

        int nativeindex=2;
        if (items.size() >= nativeindex) {
            if (mySharedPref.getChannelViewType() == CHANNEL_GRID_2_COLUMN) {
                items.add(2, new Channel());
            } else if (mySharedPref.getChannelViewType() == CHANNEL_GRID_3_COLUMN) {
                items.add(3, new Channel());
            } else {
                items.add(nativeindex, new Channel());
            }
        }
        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }


    public void setLoaded() {
        loading = false;
        Iterator<Channel> iterator = items.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            if (iterator.next() == null) {
                iterator.remove();
                notifyItemRemoved(i);
            } else {
                i++;
            }
        }
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            this.items.add(null);
            loading = true;
        }
    }

    public void resetListData() {
        this.items.clear();
        notifyDataSetChanged();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {

        if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            final StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastPos = getLastVisibleItem(layoutManager.findLastVisibleItemPositions(null));
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        int current_page = getItemCount() / (LOAD_MORE);
                        onLoadMoreListener.onLoadMore(current_page);
                    }
                }
            });
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }
    private int getLastVisibleItem(int[] into) {
        int last_idx = into[0];
        for (int i : into) {
            if (last_idx < i) last_idx = i;
        }
        return last_idx;
    }
}