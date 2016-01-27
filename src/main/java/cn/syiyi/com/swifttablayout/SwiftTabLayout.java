package cn.syiyi.com.swifttablayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * LightChat...........
 * cn.syiyi.com.lightchat.common...........
 * Created by lintao.song on 2016/1/20.
 */
public class SwiftTabLayout extends ViewGroup {
    private List<ImageView> mIcons;
    private List<TextView> mTitles;
    private ImageView mSplit;
    private final static float mMinHeight = 54;
    private final static int mMinIconHeight = 26;
    private float mIconHeight = mMinIconHeight;
    private float defaultIconPaddingTop = 6;
    private float defaultTitlePaddingBottom = 3;
    private float defaultTitleSize = 12;
    private float defaultSplitHight = 0.6f;
    private int unSelectedTextColor = Color.BLACK;
    private int SelectedTextColor = Color.GRAY;
    private int splitColor = Color.LTGRAY;
    private int mCurrentSelectedPos = -1;
    private int mTouchSlop;
    private ViewPager mViewPage;

    public SwiftTabLayout(Context context) {
        super(context);
        init(context, null);

    }

    public SwiftTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

    }

    public SwiftTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);

    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SwiftTabLayout);
        //未选中的字体颜色
        unSelectedTextColor = ta.getColor(R.styleable.SwiftTabLayout_unSelectedTextColor, unSelectedTextColor);
        //选中的字体颜色
        SelectedTextColor = ta.getColor(R.styleable.SwiftTabLayout_SelectedTextColor, SelectedTextColor);
        ta.recycle();
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledPagingTouchSlop();
        setClickable(true);


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int minHeight = height;
        if (mSplit != null) {
            //分割线
            int splitWMes = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            int splitHMes = MeasureSpec.makeMeasureSpec(dpi2px(defaultSplitHight), MeasureSpec.EXACTLY);
            mSplit.measure(splitWMes, splitHMes);
            int hMode = MeasureSpec.getMode(heightMeasureSpec);
            if (hMode == MeasureSpec.EXACTLY) {
                //MatchParent,40dp
                minHeight = Math.max(dpi2px(mMinHeight), height);
            } else {
                minHeight = dpi2px(mMinHeight);
            }
            int minIconHeight = dpi2px(mIconHeight);
            for (int i = 0; i < mIcons.size(); i++) {
                View img = mIcons.get(i);
                int wIconMeasureSpec = MeasureSpec.makeMeasureSpec(width / mTitles.size(), MeasureSpec.EXACTLY);
                int hIconMeasureSpec = MeasureSpec.makeMeasureSpec(minIconHeight, MeasureSpec.EXACTLY);
                img.measure(wIconMeasureSpec, hIconMeasureSpec);
            }
            for (int i = 0; i < mTitles.size(); i++) {
                View title = mTitles.get(i);
                int wIconMeasureSpec = MeasureSpec.makeMeasureSpec(width / mTitles.size(), MeasureSpec.EXACTLY);
                int hIconMeasureSpec = MeasureSpec.makeMeasureSpec(minHeight, MeasureSpec.AT_MOST);
                title.measure(wIconMeasureSpec, hIconMeasureSpec);
            }
            int hIconMeasureSpec = MeasureSpec.makeMeasureSpec(minHeight + mSplit.getMeasuredHeight(), MeasureSpec.EXACTLY);
            int wIconMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            setMeasuredDimension(wIconMeasureSpec, hIconMeasureSpec);
        } else {
            int hIconMeasureSpec = MeasureSpec.makeMeasureSpec(dpi2px(mMinHeight), MeasureSpec.EXACTLY);
            int wIconMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            setMeasuredDimension(wIconMeasureSpec, hIconMeasureSpec);
        }


    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        if (childCount > 1) {
            int iconBottom = mIcons.get(0).getMeasuredHeight() + dpi2px(defaultIconPaddingTop);
            int iconTop = dpi2px(defaultIconPaddingTop) + mSplit.getMeasuredHeight();

            for (int i = 0; i < mIcons.size(); i++) {
                View icon = mIcons.get(i);
                icon.layout(icon.getMeasuredWidth() * i, iconTop, icon.getMeasuredWidth() * (i + 1), iconBottom);
            }

            int titleBottom = getMeasuredHeight() - dpi2px(defaultTitlePaddingBottom);
            int titleTop = titleBottom - mTitles.get(0).getMeasuredHeight();
            for (int i = 0; i < mTitles.size(); i++) {
                View title = mTitles.get(i);
                title.layout(title.getMeasuredWidth() * i, titleTop, title.getMeasuredWidth() * (i + 1), titleBottom);
            }
            mSplit.layout(0, 0, r, mSplit.getMeasuredHeight());
        }
    }

    private float downX;
    private float downY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                //先判断是否是点击事件
                if ((isClickable() || isLongClickable())
                        && ((event.getX() - downX) < mTouchSlop || (event.getY() - downY) < mTouchSlop)) {
                    //这里我们得到了一个点击事件
                    if (isFocusable() && isFocusableInTouchMode() && !isFocused())
                        requestFocus();
                    if (event.getEventTime() - event.getDownTime() >= ViewConfiguration.getLongPressTimeout() && isLongClickable()) {
                        //是一个长按事件
//                        performLongClick();
                        Log.i("actiontype", "longClick");
                    } else {
                        //点击事件
                        performClick(event.getX());
                    }
                }
                break;

        }
        return true;


    }

    private void performClick(float x) {
        int downX = (int) x;
        int cellWidth = getMeasuredWidth() / mIcons.size();
        for (int i = 0; i < mIcons.size(); i++) {
            if (i * cellWidth < downX && downX < Math.min((i + 1) * cellWidth, getMeasuredWidth())) {
                setTabSelected(i, true);
                break;
            }
        }
    }

    public void setupWithViewPager(@NonNull ViewPager viewPager) {
        mViewPage = viewPager;
        final PagerAdapter adapter = viewPager.getAdapter();
        if (adapter == null) {
            throw new IllegalArgumentException("ViewPager does not have a PagerAdapter set");
        }
        setTabsFromPagerAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setTabSelected(position, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setTabsFromPagerAdapter(@NonNull PagerAdapter adapter) {
        removeAllTabs();
        for (int i = 0, count = adapter.getCount(); i < count; i++) {
            addTab(adapter.getPageTitle(i), getPageDrawable(adapter, i));
        }
        mSplit = new ImageView(getContext());
        mSplit.setBackgroundColor(splitColor);
        addView(mSplit,new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        invalidate();
        mCurrentSelectedPos = -1;
        setTabSelected(0, true);
    }

    private void setTabSelected(int position, boolean isSelected) {
        if (getChildCount() > 0 && mCurrentSelectedPos != position) {
            //设置点击前一个的
            if (mCurrentSelectedPos != -1) {
                ImageView icon = mIcons.get(mCurrentSelectedPos);
                TextView titile = mTitles.get(mCurrentSelectedPos);
                if (icon != null) {
                    icon.setSelected(false);
                }
                if (titile != null) {
                    titile.setTextColor(unSelectedTextColor);
                }
            }
            //设置点击后的
            ImageView icon = mIcons.get(position);
            TextView titile = mTitles.get(position);
            if (icon != null) {
                icon.setSelected(isSelected);
            }
            if (titile != null) {
                titile.setTextColor(isSelected ? SelectedTextColor : unSelectedTextColor);
            }
            mViewPage.setCurrentItem(position, false);
            mCurrentSelectedPos = position;
        }
    }


    private void addTab(CharSequence pageTitle, int pageDrawable) {
        TextView title = new TextView(getContext());
        ImageView icon = new ImageView(getContext());
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setTextSize(defaultTitleSize);
        title.setText(pageTitle);
        title.setTextColor(unSelectedTextColor);
        icon.setImageDrawable(getResources().getDrawable(pageDrawable));
        mIcons.add(icon);
        mTitles.add(title);
        addView(title, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addView(icon, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

    }

    private int getPageDrawable(@NonNull PagerAdapter adapter, int position) {
        try {
            Method method = adapter.getClass().getMethod("getPageDrawable", int.class);
            return (int) method.invoke(adapter, position);
        } catch (Exception e) {
            throw new RuntimeException("public int getPageDrawable(int postiion) Method not impl!");
        }
    }

    /**
     * Remove all tabs from the action bar and deselect the current tab.
     */
    public void removeAllTabs() {
        // Remove all the views
        if (mIcons != null) {
            mIcons.clear();
        }
        if (mTitles != null) {
            mTitles.clear();
        }
        mIcons = new ArrayList<>();
        mTitles = new ArrayList<>();
        removeAllViews();

    }

    private int dpi2px(float dpi) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dpi * density);
    }

}
