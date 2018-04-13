package com.renj.applicationtest.weight.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.renj.applicationtest.base.adapter.CustomViewHolder;

/**
 * ======================================================================
 * <p>
 * 作者：Renj
 * <p>
 * 创建时间：2017-10-18   17:29
 * <p>
 * 描述：包装的适配器：区分头、脚、正常的数据(默认为0)
 * <p>
 * 修订历史：
 * <p>
 * ======================================================================
 */
public class RefreshWrapAdapter extends RecyclerView.Adapter<CustomViewHolder> {
    //头布局
    private View mHeaderView;
    //脚布局
    private View mFooterView;
    //正常的适配器
    private RecyclerView.Adapter mAdapter;

    public RefreshWrapAdapter(View mHeaderView, View mFooterView, RecyclerView.Adapter mAdapter) {
        this.mHeaderView = mHeaderView;
        this.mFooterView = mFooterView;
        this.mAdapter = mAdapter;
    }

    @Override
    public int getItemViewType(int position) {
        // 头布局
        if (position == 0)
            return RefreshRecyclerView.ITEM_TYPE_HEADER;
        // 正常布局
        int tempPosition = position - 1;
        int normalCount = mAdapter.getItemCount();
        if (tempPosition < normalCount)
            return mAdapter.getItemViewType(tempPosition);
        // 尾布局
        return RefreshRecyclerView.ITEM_TYPE_FOOTER;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == RefreshRecyclerView.ITEM_TYPE_HEADER)
            return new HeaderViewHolder(mHeaderView);
        if (viewType == RefreshRecyclerView.ITEM_TYPE_FOOTER)
            return new FooterViewHolder(mFooterView);
        return (CustomViewHolder) mAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        // 头布局
        if (position == 0)
            return;

        // 正常布局
        int tempPosition = position - 1;
        int normalCount = mAdapter.getItemCount();
        if (tempPosition < normalCount)
            mAdapter.onBindViewHolder(holder, tempPosition);

    }

    @Override
    public int getItemCount() {
        return mAdapter.getItemCount() + 2;
    }

    // 头布局ViewHolder
    static class HeaderViewHolder extends CustomViewHolder {
        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    // 尾布局ViewHolder
    static class FooterViewHolder extends CustomViewHolder {
        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }
}
