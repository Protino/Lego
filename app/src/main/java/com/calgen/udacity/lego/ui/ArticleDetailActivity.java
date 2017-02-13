package com.calgen.udacity.lego.ui;


import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.calgen.udacity.lego.R;
import com.calgen.udacity.lego.data.ArticleLoader;
import com.calgen.udacity.lego.data.ItemsContract;

import butterknife.BindBool;
import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, ViewPager.OnPageChangeListener {

    //@formatter:off
    @BindView(R.id.pager) public ViewPager pager;
    @BindView(R.id.upCaret) public ImageView upCaret;
    @BindDimen(R.dimen.keyline) public int keyLine;
    @BindBool(R.bool.is_land) public boolean isLand;
    //@formatter:on
    private Cursor cursor;
    private long startId;
    private MyPagerAdapter mPagerAdapter;

    //Lifecycle start
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportLoaderManager().initLoader(0, null, this);

        setContentView(R.layout.activity_article_detail);
        ButterKnife.bind(this);
        setUpCaretMargin();
        setUpPager();
        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                startId = ItemsContract.Items.getItemId(getIntent().getData());
            }
        }
        supportPostponeEnterTransition();
    }
    //Lifecycle end

    private void setUpCaretMargin() {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) upCaret.getLayoutParams();
        params.setMargins(0, isLand ? 0 : Utils.getStatusBarHeight(this), keyLine, keyLine);
        upCaret.setLayoutParams(params);
    }

    public void updateUpCaret(boolean isDark) {
        upCaret.setColorFilter(ContextCompat.getColor(this, isDark ? R.color.white : R.color.black));
        upCaret.setVisibility(View.VISIBLE);
        upCaret.animate().alpha(1f).setDuration(300);
    }

    private void setUpPager() {
        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(mPagerAdapter);
        pager.setPageMargin(Utils.dpToPx(1));
        pager.setPageMarginDrawable(new ColorDrawable(0x22000000));
        pager.setOnPageChangeListener(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        this.cursor = cursor;
        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (startId > 0) {
            // TODO: optimize
            //Maybe, this can be optimized pre-loading all articles
            //in list activity and then only pass the position. Not sure.
            for (this.cursor.moveToFirst(); !this.cursor.isAfterLast(); this.cursor.moveToNext()) {
                if (this.cursor.getLong(ArticleLoader.Query._ID) == startId) {
                    final int position = this.cursor.getPosition();
                    pager.setCurrentItem(position, false);
                    break;
                }
            }
            startId = 0;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        cursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (upCaret == null) return;
        upCaret.animate().alpha(state == ViewPager.SCROLL_STATE_IDLE ? 1f : 0f).setDuration(300);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (cursor != null) {
            cursor.moveToPosition(position);
        }
    }

    public void onUpCaretClick(View view) {
        onBackPressed();
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            cursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(cursor.getLong(ArticleLoader.Query._ID));
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            ArticleDetailFragment fragment = (ArticleDetailFragment) object;
            if (fragment != null) {
                updateUpCaret(fragment.isDark);
            }
        }

        @Override
        public int getCount() {
            return (cursor != null) ? cursor.getCount() : 0;
        }
    }
}
