/*
 * Copyright 2016 Gurupad Mamadapur
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.calgen.udacity.lego.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.calgen.udacity.lego.R;
import com.calgen.udacity.lego.data.ArticleLoader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindBool;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets !Yet-To be implemented! ) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_ITEM_ID = "item_id";
    private static final String TAG = "ArticleDetailFragment";
    private static final String LOG_TAG = ArticleDetailFragment.class.getSimpleName();
    //@formatter:off
    @BindView(R.id.photo) public ImageView photoView;
    @BindView(R.id.article_title) public TextView titleView;
    @BindView(R.id.article_byline) public TextView bylineView;
    @BindView(R.id.article_body) public TextView bodyView;
    @Nullable @BindView(R.id.app_bar_layout) public AppBarLayout appBarLayout;
    @Nullable @BindView(R.id.toolbar) public Toolbar toolbar;
    @Nullable @BindView(R.id.detail_content) public RelativeLayout detailContent;
    @BindView(R.id.share_fab) public FloatingActionButton shareFab;
    @BindView(R.id.meta_bar) public LinearLayout metaBar;
    @BindColor(R.color.material_grey) public int materialGrey;
    @BindColor(R.color.primary_dark) public int primaryDark;
    @BindDimen(R.dimen.keyline) public int keyLine;
    @BindBool(R.bool.is_land) public boolean isLand;
    @BindDrawable(R.drawable.empty_detail) public Drawable placeholder;
    public boolean isDark = false;
    private int bylineThreshold = 33;
    //@formatter:on
    private View rootView;
    private Cursor cursor;
    private long itemId;
    private int overLapTopMargin;
    private int extraSideMargin;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
        //required
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        ButterKnife.bind(this, rootView);
        calculateMargins();
        bindViews();
        if (!isLand) {
            CollapsingToolbarLayout.LayoutParams params =
                    new CollapsingToolbarLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, overLapTopMargin);
            toolbar.setLayoutParams(params);
        } else {
            RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(extraSideMargin, overLapTopMargin, extraSideMargin, keyLine);
            detailContent.setLayoutParams(params);
        }
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

    private void calculateMargins() {
        Pair<Integer, Integer> pair = Utils.getScreenWidthAndHeight(getContext());
        overLapTopMargin = (int) (pair.second * 0.4); // 40% of the height
        extraSideMargin = (int) (pair.first * 0.1);  // 10% of the width

        // The following uses dark magic
        bylineThreshold = (Utils.pxToDp(pair.first) - 48) / 9;
    }

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
            final String photoUrl = cursor.getString(ArticleLoader.Query.PHOTO_URL);
            Picasso.with(getContext())
                    .load(photoUrl)
                    .placeholder(placeholder)
                    .into(photoView, new Callback() {
                        @Override
                        public void onSuccess() {
                            playAnimation(photoView);
                            final Bitmap bitmap = ((BitmapDrawable) photoView.getDrawable()).getBitmap();
                            Palette.from(bitmap)
                                    .clearFilters()
                                    .generate(new Palette.PaletteAsyncListener() {
                                        @Override
                                        public void onGenerated(Palette palette) {
                                            @ColorUtils.Lightness int lightness = ColorUtils.isDark(palette);
                                            if (lightness == ColorUtils.LIGHTNESS_UNKNOWN) {
                                                isDark = ColorUtils.isDark(bitmap, bitmap.getWidth() / 2, 0);
                                            } else {
                                                isDark = lightness == ColorUtils.IS_DARK;
                                            }
                                            int extraDarkMutedColor = ColorUtils.scrimify(
                                                    palette.getDarkMutedColor(primaryDark),
                                                    true, 0.7f); //70% Darker
                                            metaBar.setBackgroundColor(extraDarkMutedColor);
                                            if (isLand && getActivity() != null
                                                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                (getActivity()).getWindow()
                                                        .setStatusBarColor(extraDarkMutedColor);
                                            }
                                        }
                                    });
                        }

                        @Override
                        public void onError() {
                            Log.e(LOG_TAG, "onError: Couldn't load photo - " + photoUrl);
                        }
                    });
            if (appBarLayout != null) {
                appBarLayout.setExpanded(false, true);
            }
            rootView.animate().alpha(1).setDuration(300);
            getActivity().supportStartPostponedEnterTransition();
        } else {
            rootView.setVisibility(View.GONE);
        }
    }

    private void playAnimation(ImageView photoView) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setDuration(500);
        photoView.startAnimation(fadeIn);
        shareFab.setVisibility(View.VISIBLE);
        shareFab.startAnimation(fadeIn);
    }

    private String formatByLine(String modifiedByline) {
        if (modifiedByline.length() > bylineThreshold) {
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
