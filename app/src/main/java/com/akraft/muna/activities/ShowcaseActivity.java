package com.akraft.muna.activities;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.akraft.muna.R;
import com.akraft.muna.models.wrappers.ShowcasePage;
import com.viewpagerindicator.CirclePageIndicator;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ShowcaseActivity extends AppCompatActivity {

    @InjectView(R.id.view_pager)
    ViewPager viewPager;
    @InjectView(R.id.indicator)
    CirclePageIndicator pageIndicator;
    @InjectView(R.id.caption)
    TextView captionText;
    @InjectView(R.id.skip_showcase)
    ImageButton skipButton;

    public static final int FIRST_LAUNCH = 0;
    public static final int MARK_CREATION = 1;

    private ShowcasePage markCreationPage;
    private ShowcaseAdapter adapter;

    private ShowcasePage[] pages;
    private ShowcasePage page;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showcase);
        ButterKnife.inject(this);

        int type = getIntent().getIntExtra("type", -1);
        if (type == -1)
            finish();

        initPages();

        page = pages[type];
        captionText.setText(page.getCaptions()[0]);

        adapter = new ShowcaseAdapter(page);
        viewPager.setAdapter(adapter);
        pageIndicator.setViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                captionText.setText(page.getCaptions()[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initPages() {
        pages = new ShowcasePage[]{null,
                new ShowcasePage(new int[]{R.drawable.sc_1_start, R.drawable.sc_1_thinkup,
                        R.drawable.sc_1_write, R.drawable.sc_1_place, R.drawable.sc_1_earn}, getResources().getStringArray(R.array.sc_mark_creation))};
    }

    @OnClick(R.id.skip_showcase)
    public void skipShowcase() {
        finish();
    }


    private class ShowcaseAdapter extends PagerAdapter {
        private int[] images;

        public ShowcaseAdapter(ShowcasePage page) {
            this.images = page.getImages();
        }

        @Override
        public int getCount() {
            return images.length;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = (ImageView) View.inflate(getApplicationContext(), R.layout.showcase_item, null);
            container.addView(imageView);
            imageView.setImageResource(images[position]);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((ImageView) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }


}
