package com.example.xyzreader.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_ITEM_ID = "item_id";
    private static final String TAG = "ArticleDetailFragment";
    private static final String LOG_TAG = ArticleDetailFragment.class.getSimpleName();
    private static final int BYLINE_THRESHOLD = 33;
    //@formatter:off
    @BindView(R.id.photo) public ImageView photoView;
    @BindView(R.id.article_title) public TextView titleView;
    @BindView(R.id.article_byline) public TextView bylineView;
    @BindView(R.id.article_body) public TextView bodyView;
    @BindView(R.id.app_bar_layout) public AppBarLayout appBarLayout;
    @BindView(R.id.coordinator_layout) public CoordinatorLayout coordinatorLayout;
    @BindView(R.id.share_fab) public FloatingActionButton shareFab;
    @BindView(R.id.meta_bar) public LinearLayout metaBar;
    @BindColor(R.color.material_grey) public int materialGrey;
    @BindColor(R.color.accent) public int accent;
    public boolean isDark = false;
    @BindView(R.id.detailNestedScrollView) NestedScrollView nestedScrollView;
    //@formatter:on
    private View rootView;
    private Cursor cursor;
    private long itemId;
    private boolean isCard = false;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    //Lifecycle start
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            itemId = getArguments().getLong(ARG_ITEM_ID);
        }
        isCard = getResources().getBoolean(R.bool.detail_is_card);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        ButterKnife.bind(this, rootView);
        bindViews();
        appBarLayout.setExpanded(false);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }
    //Lifecycle end

    @OnClick(R.id.share_fab)
    public void onClick() {
        startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                .setType("text/plain")
                .setText("Some sample text")
                .getIntent(), getString(R.string.action_share)));
    }

    private void bindViews() {
        if (rootView == null) {
            return;
        }
        if (cursor != null) {
            rootView.setAlpha(0);
            rootView.setVisibility(View.VISIBLE);
            titleView.setText(cursor.getString(ArticleLoader.Query.TITLE));
            bylineView.setText(formatByLine(Utils.getModifiedByline(cursor, getContext())));
            bodyView.setText(Html.fromHtml(cursor.getString(ArticleLoader.Query.BODY)));
            bodyView.setMovementMethod(LinkMovementMethod.getInstance());
            photoView.setMaxHeight((int) (getContext().getResources().getDisplayMetrics().heightPixels * 0.8));
            Picasso.with(getContext())
                    .load(cursor.getString(ArticleLoader.Query.PHOTO_URL))
                    .placeholder(getContext().getResources().getDrawable(R.drawable.empty_detail))
                    .into(photoView, new Callback() {
                        @Override
                        public void onSuccess() {
                            final Bitmap bitmap = ((BitmapDrawable) photoView.getDrawable()).getBitmap();
                            Palette.from(bitmap)
                                    .maximumColorCount(3)
                                    .clearFilters()
                                    .generate(new Palette.PaletteAsyncListener() {
                                        @Override
                                        public void onGenerated(Palette palette) {
                                            @ColorUtils.Lightness int lightness = ColorUtils.isDark(palette);
                                            if (lightness == ColorUtils.LIGHTNESS_UNKNOWN) {
                                                isDark = ColorUtils.isDark(bitmap, bitmap.getWidth()/2, 0);
                                            } else {
                                                isDark = lightness == ColorUtils.IS_DARK;
                                            }
                                        }
                                    });
                        }

                        @Override
                        public void onError() {
                            Log.e(LOG_TAG, "onError: Couldn't load photo");
                        }
                    });
            rootView.animate().alpha(1);

        } else {
            rootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A");
            bodyView.setText("N/A");
        }
    }

    private String formatByLine(String modifiedByline) {
        if (modifiedByline.length() > BYLINE_THRESHOLD) {
            return new StringBuilder(modifiedByline).insert(modifiedByline.indexOf("by"), "\n").toString();
        }
        return modifiedByline;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), itemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        this.cursor = cursor;
        if (this.cursor != null && !this.cursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            this.cursor.close();
            this.cursor = null;
        }
        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        cursor = null;
        bindViews();
    }
}
