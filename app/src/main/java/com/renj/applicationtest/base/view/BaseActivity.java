package com.renj.applicationtest.base.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.renj.applicationtest.R;
import com.renj.applicationtest.utils.ResUtils;
import com.renj.applicationtest.utils.SoftKeyBoardUtils;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.yokeyword.fragmentation.SupportActivity;

/**
 * ======================================================================
 * 作者：Renj
 * <p>
 * 创建时间：2017-05-11   18:39
 * <p>
 * 描述：Activity的基类<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * 如果一个新的Activity不需要访问网络，那么就直接继承{@link BaseActivity}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * 如果一个新的Activity需要访问网络，那么可以继承{@link BasePresenterActivity}
 * <p>
 * 修订历史：
 * <p>
 * ======================================================================
 */
public abstract class BaseActivity extends SupportActivity implements IBaseView, View.OnClickListener {
    private Unbinder bind;
    private View backView;
    private TextView tvTitle;
    private ViewStub rightView;
    private ViewStub viewTitleBar;

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        // 初始化基本布局
        viewTitleBar = (ViewStub) findViewById(R.id.view_title_bar);
        ViewStub viewContent = (ViewStub) findViewById(R.id.view_content);
        viewContent.setLayoutResource(getLayoutId());
        viewContent.inflate();

        bind = ButterKnife.bind(this);
        initPresenter();
        initData();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isShouldHideSoftKeyBoard() && ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v != null && SoftKeyBoardUtils.isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    /**
     * 是否需要点击非 {@link android.widget.EditText} 控件之外的地方就隐藏软键盘。默认返回false：不是<br/>
     * 如果需要，子类重写并返回 true 即可
     *
     * @return tru：点击的位置在 {@link android.widget.EditText} 控件的范围，需要隐藏软键盘  false：点击的位置在非 {@link android.widget.EditText} 控件范围，需要隐藏
     */
    protected boolean isShouldHideSoftKeyBoard() {
        return false;
    }

    /**
     * 是否需要显示标题栏，子类可跟据需求重写该方法控制是否需要显示标题栏
     *
     * @return true：需要显示；false：不需要显示
     */
    protected boolean isShowTitleBar() {
        return true;
    }

    /**
     * 需要修改返回按钮的功能时，重写该方法即可<br/>
     * <b>注意：
     * 如果调用了 {@link #setTitleBarView(int)} 方法重置了整个标题栏，那么这个方法将失去意义</b>
     */
    protected void goBack() {
        finish();
    }

    /**
     * 自定义整个标题栏<br/>
     * <b>注意：
     * 如果调用了 {@link #setTitleBarTitle(String)}、{@link #setTitleBarTitle(String, boolean)}、{@link #setTitleBarRightViewText(String, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightViewText(String, int, OnTitleRightClickListener)}、{@link #setTitleBarRightViewText(String, float, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightViewText(String, float, int, OnTitleRightClickListener)}、{@link #setTitleBarRightViewImg(int, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightView(int)} 中的任何一个，方法 {@link #setTitleBarView(int)} 将失效，反之如此。</b>
     *
     * @param layoutId 标题栏布局id
     * @return 返回参数所表示的布局文件(参数布局文件的根布局对象)，<b>注意：如果 {@link #isShowTitleBar()} 方法返回 false或找不到布局ID，会返回 null</b>
     */
    @SuppressLint("ResourceType")
    protected View setTitleBarView(@LayoutRes int layoutId) {
        if (!isShowTitleBar() || viewTitleBar.getLayoutResource() != 0) return null;
        if (layoutId < 0) return null;

        viewTitleBar.setLayoutResource(layoutId);
        return viewTitleBar.inflate();
    }

    /**
     * 初始化标题栏
     */
    private void initTitleBar() {
        if (viewTitleBar.getLayoutResource() == 0) {
            viewTitleBar.setLayoutResource(R.layout.default_title_bar);
            View titleView = viewTitleBar.inflate();
            backView = titleView.findViewById(R.id.title_bar_tv_bck);
            tvTitle = (TextView) titleView.findViewById(R.id.title_bar_title);
            rightView = (ViewStub) titleView.findViewById(R.id.title_bar_right_view);

            // 返回按钮设置监听
            backView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goBack();
                }
            });
        }
    }

    /**
     * 设置标题内容，前提是 {@link #isShowTitleBar()} 方法返回 true，默认返回按钮显示<br/>
     * <b>注意：
     * 如果调用了 {@link #setTitleBarTitle(String)}、{@link #setTitleBarTitle(String, boolean)}、{@link #setTitleBarRightViewText(String, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightViewText(String, int, OnTitleRightClickListener)}、{@link #setTitleBarRightViewText(String, float, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightViewText(String, float, int, OnTitleRightClickListener)}、{@link #setTitleBarRightViewImg(int, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightView(int)} 中的任何一个，方法 {@link #setTitleBarView(int)} 将失效，反之如此。</b>
     *
     * @param titleMsg 标题显示内容
     */
    public void setTitleBarTitle(@NonNull String titleMsg) {
        setTitleBarTitle(titleMsg, true);
    }

    /**
     * 设置标题内容和是否显示返回按钮，前提是 {@link #isShowTitleBar()} 方法返回 true<br/>
     * <b>注意：
     * 如果调用了 {@link #setTitleBarTitle(String)}、{@link #setTitleBarTitle(String, boolean)}、{@link #setTitleBarRightViewText(String, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightViewText(String, int, OnTitleRightClickListener)}、{@link #setTitleBarRightViewText(String, float, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightViewText(String, float, int, OnTitleRightClickListener)}、{@link #setTitleBarRightViewImg(int, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightView(int)} 中的任何一个，方法 {@link #setTitleBarView(int)} 将失效，反之如此。</b>
     *
     * @param titleMsg   标题显示内容
     * @param isShowBack 是否需要显示返回按钮 true 显示；false 不显示
     */
    public void setTitleBarTitle(@NonNull String titleMsg, @NonNull boolean isShowBack) {
        if (isShowTitleBar()) {
            initTitleBar();

            if (tvTitle != null)
                tvTitle.setText(titleMsg);

            if (backView != null)
                backView.setVisibility(isShowBack ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 设置标题栏右边展示文字信息，前提是 {@link #isShowTitleBar()} 方法返回 true<br/>
     * <b>注意：
     * 如果调用了 {@link #setTitleBarTitle(String)}、{@link #setTitleBarTitle(String, boolean)}、{@link #setTitleBarRightViewText(String, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightViewText(String, int, OnTitleRightClickListener)}、{@link #setTitleBarRightViewText(String, float, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightViewText(String, float, int, OnTitleRightClickListener)}、{@link #setTitleBarRightViewImg(int, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightView(int)} 中的任何一个，方法 {@link #setTitleBarView(int)} 将失效，反之如此。</b>
     *
     * @param rightMsg 右边显示的文字
     * @param listener 文字监听，不需要事件可以传null
     */
    public void setTitleBarRightViewText(@NonNull String rightMsg, OnTitleRightClickListener listener) {
        setTitleBarRightViewText(rightMsg, ResUtils.getDimens(R.dimen.title_bar_right_size), ResUtils.getColor(R.color.title_bar_right_color), listener);
    }

    /**
     * 设置标题栏右边展示文字信息，前提是 {@link #isShowTitleBar()} 方法返回 true<br/>
     * <b>注意：
     * 如果调用了 {@link #setTitleBarTitle(String)}、{@link #setTitleBarTitle(String, boolean)}、{@link #setTitleBarRightViewText(String, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightViewText(String, int, OnTitleRightClickListener)}、{@link #setTitleBarRightViewText(String, float, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightViewText(String, float, int, OnTitleRightClickListener)}、{@link #setTitleBarRightViewImg(int, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightView(int)} 中的任何一个，方法 {@link #setTitleBarView(int)} 将失效，反之如此。</b>
     *
     * @param rightMsg 右边显示的文字
     * @param textSize 右边显示的文字大小，传入的参数单位为 px，默认15sp
     * @param listener 文字监听，不需要事件可以传null
     */
    public void setTitleBarRightViewText(@NonNull String rightMsg, @NonNull float textSize, OnTitleRightClickListener listener) {
        setTitleBarRightViewText(rightMsg, textSize, ResUtils.getColor(R.color.title_bar_right_color), listener);
    }

    /**
     * 设置标题栏右边展示文字信息，前提是 {@link #isShowTitleBar()} 方法返回 true<br/>
     * <b>注意：
     * 如果调用了 {@link #setTitleBarTitle(String)}、{@link #setTitleBarTitle(String, boolean)}、{@link #setTitleBarRightViewText(String, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightViewText(String, int, OnTitleRightClickListener)}、{@link #setTitleBarRightViewText(String, float, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightViewText(String, float, int, OnTitleRightClickListener)}、{@link #setTitleBarRightViewImg(int, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightView(int)} 中的任何一个，方法 {@link #setTitleBarView(int)} 将失效，反之如此。</b>
     *
     * @param rightMsg  右边显示的文字
     * @param textColor 右边显示的文字颜色，默认  <color name="title_bar_right_color">#FF333333</color>
     * @param listener  文字监听，不需要事件可以传null
     */
    public void setTitleBarRightViewText(@NonNull String rightMsg, @NonNull int textColor, OnTitleRightClickListener listener) {
        setTitleBarRightViewText(rightMsg, ResUtils.getDimens(R.dimen.title_bar_right_size), textColor, listener);
    }

    /**
     * 设置标题栏右边展示文字信息，前提是 {@link #isShowTitleBar()} 方法返回 true<br/>
     * <b>注意：
     * 如果调用了 {@link #setTitleBarTitle(String)}、{@link #setTitleBarTitle(String, boolean)}、{@link #setTitleBarRightViewText(String, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightViewText(String, int, OnTitleRightClickListener)}、{@link #setTitleBarRightViewText(String, float, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightViewText(String, float, int, OnTitleRightClickListener)}、{@link #setTitleBarRightViewImg(int, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightView(int)} 中的任何一个，方法 {@link #setTitleBarView(int)} 将失效，反之如此。</b>
     *
     * @param rightMsg  右边显示的文字
     * @param textSize  右边显示的文字大小，传入的参数单位为 px，默认15sp
     * @param textColor 右边显示的文字颜色，默认  <color name="title_bar_right_color">#FF333333</color>
     * @param listener  文字监听，不需要事件可以传null
     */
    public void setTitleBarRightViewText(@NonNull String rightMsg, @NonNull float textSize, @NonNull int textColor, final OnTitleRightClickListener listener) {
        if (isShowTitleBar()) {
            initTitleBar();
            if (rightView != null) {
                rightView.setLayoutResource(R.layout.default_title_bar_right_text);
                final TextView textView = (TextView) rightView.inflate();

                textView.setText(rightMsg);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                textView.setTextColor(textColor);

                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null)
                            listener.onRightViewClick(textView);
                    }
                });
            }
        }
    }

    /**
     * 设置标题栏右边展示图片，前提是 {@link #isShowTitleBar()} 方法返回 true<br/>
     * <b>注意：
     * 如果调用了 {@link #setTitleBarTitle(String)}、{@link #setTitleBarTitle(String, boolean)}、{@link #setTitleBarRightViewText(String, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightViewText(String, int, OnTitleRightClickListener)}、{@link #setTitleBarRightViewText(String, float, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightViewText(String, float, int, OnTitleRightClickListener)}、{@link #setTitleBarRightViewImg(int, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightView(int)} 中的任何一个，方法 {@link #setTitleBarView(int)} 将失效，反之如此。</b>
     *
     * @param resId    右边显示的图片ID
     * @param listener 文字监听，不需要事件可以传null
     */
    public void setTitleBarRightViewImg(@DrawableRes int resId, final OnTitleRightClickListener listener) {
        if (isShowTitleBar()) {
            initTitleBar();
            if (rightView != null) {
                rightView.setLayoutResource(R.layout.default_title_bar_right_img);

                final ImageView imageView = (ImageView) rightView.inflate();
                imageView.setBackgroundResource(resId);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null)
                            listener.onRightViewClick(imageView);
                    }
                });
            }
        }
    }

    /**
     * 设置标题栏右边自定义布局，前提是 {@link #isShowTitleBar()} 方法返回 true，<b>注意：控件里面的id必须使用findViewById()方式获得，不能使用注解的方式，会出现异常</b><br/>
     * <b>注意：
     * 如果调用了 {@link #setTitleBarTitle(String)}、{@link #setTitleBarTitle(String, boolean)}、{@link #setTitleBarRightViewText(String, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightViewText(String, int, OnTitleRightClickListener)}、{@link #setTitleBarRightViewText(String, float, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightViewText(String, float, int, OnTitleRightClickListener)}、{@link #setTitleBarRightViewImg(int, OnTitleRightClickListener)}、
     * {@link #setTitleBarRightView(int)} 中的任何一个，方法 {@link #setTitleBarView(int)} 将失效，反之如此。</b>
     *
     * @param layoutId 自定义布局的id
     * @return 返回参数所表示的布局文件(参数布局文件的根布局对象)，<b>注意：如果 {@link #isShowTitleBar()} 方法返回 false，那么这里会返回 null</b>
     */
    public View setTitleBarRightView(@LayoutRes int layoutId) {
        if (isShowTitleBar()) {
            initTitleBar();
            if (rightView != null) {
                rightView.setLayoutResource(layoutId);
                return rightView.inflate();
            }
        }
        return null;
    }

    /**
     * 初始化Presenter，在{@link BasePresenterActivity}中重写
     */
    void initPresenter() {

    }

    @Override
    public void onClick(View v) {
        handlerClick(v, v.getId());
    }

    /**
     * 处理点击事件时，只需要重写该方法即可，不需要再实现{@link View.OnClickListener}接口
     *
     * @param v   控件
     * @param vId
     */
    protected void handlerClick(View v, int vId) {

    }

    @Override
    public void showContentPage() {

    }

    @Override
    public void showLoadingPage() {

    }

    @Override
    public void showEmptyDataPage() {

    }

    @Override
    public void showNetWorkErrorPage() {

    }

    @Override
    public void showErrorPage(Throwable e) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bind.unbind();
    }

    public interface OnTitleRightClickListener {
        void onRightViewClick(View view);
    }
}
