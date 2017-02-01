package com.example.xyzreader.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {

    //@formatter:off
    @BindView(R.id.toolbar) public Toolbar toolbar;
    @BindView(R.id.swipe_refresh_layout) public SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recycler_view) public RecyclerView recyclerView;
    //@formatter:on

    private boolean isRefreshing = false;
    private BroadcastReceiver refreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                isRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    //Lifecycle start
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        swipeRefreshLayout.setOnRefreshListener(this);
        getLoaderManager().initLoader(0, null, this);
        if (savedInstanceState == null) {
            refresh();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(refreshingReceiver, new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(refreshingReceiver);
    }
//Lifecycle end

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    private void updateRefreshingUI() {
        swipeRefreshLayout.setRefreshing(isRefreshing);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Adapter adapter = new Adapter(this, cursor);
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(sglm);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        recyclerView.setAdapter(null);
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private final String LOG_TAG = Adapter.class.getSimpleName();
        private final int materialGrey;
        private Cursor mCursor;
        private Context context;
        private SparseIntArray darkMutedColorMap;

        public Adapter(Context context, Cursor cursor) {
            mCursor = cursor;
            this.context = context;
            darkMutedColorMap = new SparseIntArray();
            materialGrey = context.getResources().getColor(R.color.material_grey);
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(ArticleLoader.Query._ID);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            mCursor.moveToPosition(position);
            holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            holder.subtitleView.setText(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR));
            final ImageView thumbnail = holder.thumbnailView;
            Picasso.with(context)
                    .load(mCursor.getString(ArticleLoader.Query.THUMB_URL))
                    .into(thumbnail, new Callback() {
                        @Override
                        public void onSuccess() {
                            int rgbColor = darkMutedColorMap.get(position);
                            if (rgbColor == 0) {
                                Bitmap bitmap = ((BitmapDrawable) thumbnail.getDrawable()).getBitmap();
                                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(Palette palette) {
                                        int color = palette.getDarkMutedColor(materialGrey);
                                        darkMutedColorMap.put(position, color);
                                        holder.cardView.setCardBackgroundColor(color);
                                    }
                                });
                            } else {
                                holder.cardView.setCardBackgroundColor(rgbColor);
                            }
                        }

                        @Override
                        public void onError() {
                            holder.cardView.setCardBackgroundColor(materialGrey);
                        }
                    });

        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //@formatter:off
        @BindView(R.id.list_item) public CardView cardView;
        @BindView(R.id.thumbnail) public ImageView thumbnailView;
        @BindView(R.id.article_title) public TextView titleView;
        @BindView(R.id.article_subtitle) public TextView subtitleView;
        //@formatter:on

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.list_item)
        public void onClick() {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    ItemsContract.Items.buildItemUri(getItemId())));
        }
    }
}
