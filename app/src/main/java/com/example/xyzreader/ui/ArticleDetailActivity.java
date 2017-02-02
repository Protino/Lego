package com.example.xyzreader.ui;


import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.widget.ImageView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

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
    //@formatter:on

    private Cursor cursor;
    private long startId;
    private MyPagerAdapter mPagerAdapter;

    //Lifecycle start
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);
        ButterKnife.bind(this);
        getSupportLoaderManager().initLoader(0, null, this);
        setUpPager();
        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                startId = ItemsContract.Items.getItemId(getIntent().getData());
            }
        }
    }

    //Lifecycle end
    public void onUpCaretClick() {
        onBackPressed();
    }

    private void setUpPager() {
        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(mPagerAdapter);
        pager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
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
        upCaret.animate()
                .alpha((state == ViewPager.SCROLL_STATE_IDLE) ? 1f : 0f)
                .setDuration(300);
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
        public int getCount() {
            return (cursor != null) ? cursor.getCount() : 0;
        }
    }
}
