package com.moca.futbol.adaptersmoca;




import static com.moca.futbol.MocConf.LOAD_MORE;
import static com.moca.futbol.helpermoca.MocConstant.CATEGORY_GRID_2_COLUMN;
import static com.moca.futbol.helpermoca.MocConstant.CATEGORY_GRID_3_COLUMN;

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

import com.moca.futbol.R;
import com.moca.futbol.dataprefsmoca.prefs.SharedPrefs;
import com.moca.futbol.modelmoca.Category;
import com.moca.futbol.adsmoca.AdsNativeViewHolder;

import java.util.Iterator;
import java.util.List;

public class MocCategory extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 1;
    private final int VIEW_AD = 2;
    private List<Category> items;

    private boolean loading;
    private Context context;
    private OnItemClickListener mOnItemClickListener;
    SharedPrefs sharedPrefs_ibo;

    boolean scrolling = false;
    private OnLoadMoreListener onLoadMoreListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Category obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MocCategory(Context context, RecyclerView view, List<Category> items) {
        this.items = items;
        this.context = context;
        this.sharedPrefs_ibo = new SharedPrefs(context);
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

    public class OriginalViewHolder2 extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView category_name;
        public ImageView category_image;
        public LinearLayout lyt_parent;

        public OriginalViewHolder2(View v) {
            super(v);
            category_name = v.findViewById(R.id.category_name);
            category_image = v.findViewById(R.id.category_image);
            lyt_parent = v.findViewById(R.id.my_lyt_parent);
        }
    }

    public static class ProgressViewHolder2 extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;


        ProgressViewHolder2(View v) {
            super(v);
            progressBar = v.findViewById(R.id.loadMore);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            SharedPrefs sharedPref = new SharedPrefs(context);
            if (sharedPref.getCategoryViewType() == CATEGORY_GRID_2_COLUMN || sharedPref.getCategoryViewType() == CATEGORY_GRID_3_COLUMN) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pobr_item_category_grid, parent, false);
                vh = new OriginalViewHolder2(view);
            } else {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pobr_item_category, parent, false);
                vh = new OriginalViewHolder2(view);
            }
        }else if  (viewType == VIEW_AD){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.native_layout, parent, false);
            vh = new AdsNativeViewHolder(view);
        }else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_loading, parent, false);
            vh = new MocRecent.ProgressViewHolder(v);
        }
            return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof MocCategory.OriginalViewHolder2) {
            final MocCategory.OriginalViewHolder2 vItem = (MocCategory.OriginalViewHolder2) holder;

            final Category c = items.get(position);

            vItem.category_name.setText(c.category_name);

            Glide.with(context)
                    .load(sharedPrefs_ibo.getBaseUrl() + "/upload/category/" + c.category_image)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(vItem.category_image);

            vItem.lyt_parent.setOnClickListener(view -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, c, position);
                }
            });

        }else if (holder instanceof AdsNativeViewHolder) {

            final AdsNativeViewHolder vItem = (AdsNativeViewHolder) holder;
            vItem.loadNativeAd(context, MocFirstAds.category_native, MocSecAds.category_native);
        }else {
            ((ProgressViewHolder2) holder).progressBar.setIndeterminate(true);
        }

        if ( getItemViewType(position) == VIEW_AD) {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(true);
        } else {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(false);
        }

    }

    public void resetListData() {

        this.items.clear();
        notifyDataSetChanged();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnLoadMoreListener(MocCategory.OnLoadMoreListener onLoadMoreListener) {
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

    public void setLoading() {
        if (getItemCount() != 0) {
            this.items.add(null);
            loading = true;
        }
    }

    public void setLoaded() {
        loading = false;
        Iterator<Category> iterator = items.iterator();
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

    @Override
    public int getItemViewType(int position) {
        Category category = items.get(position);
        if (category != null) {
            if (category.category_name == null || category.category_name.equals("")) {
                return VIEW_AD;
            }
        }
        return VIEW_ITEM;
    }

    public void insertDataWithNativeAd(List<Category> items) {
        setLoaded();
        int positionStart = getItemCount();
        for (Category post : items) {
            Log.d("item", "TITLE: " + post.category_name);
        }

        int nativeindex=2;
        if (items.size() >= nativeindex) {
            if (sharedPrefs_ibo.getCategoryViewType() == CATEGORY_GRID_2_COLUMN) {
                items.add(2, new Category());
            } else if (sharedPrefs_ibo.getCategoryViewType() == CATEGORY_GRID_3_COLUMN) {
                items.add(3, new Category());
            } else {
                items.add(nativeindex, new Category());
            }
        }
        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

}