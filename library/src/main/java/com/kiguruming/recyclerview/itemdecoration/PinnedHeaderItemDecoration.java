package com.kiguruming.recyclerview.itemdecoration;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * ItemDecoration for Pinned Header.
 *
 * porting from https://github.com/beworker/pinned-section-listview
 * @author takahr@gmail.com
 */
public class PinnedHeaderItemDecoration extends RecyclerView.ItemDecoration {
    private final static String TAG = PinnedHeaderItemDecoration.class.getSimpleName();

    public interface PinnedHeaderAdapter {
        boolean isPinnedViewType(int viewType);
    }

    RecyclerView.Adapter mAdapter = null;

    // cached data
    // pinned header view
    View mPinnedHeaderView = null;
    int mHeaderPosition = -1;

    Map<Integer, Boolean> mPinnedViewTypes = new HashMap<Integer, Boolean>();

    private int mPinnedHeaderTop;
    private Rect mClipBounds = new Rect();

    private Paint mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int mAlpha = 120;
    private Rect mShadowRect = new Rect();
    private int mShadowSize = 0;

    /*
    Used to end the gradient before the bottom of the rectangle to draw.
     */
    private float mShadowSizeOffset;
    
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        createPinnedHeader(parent);

        if (mPinnedHeaderView != null) {
			c.save();
			
            // check overlap section view.
            //TODO support only vertical header currently.
            final int headerEndAt = mPinnedHeaderView.getTop() + mPinnedHeaderView.getHeight();
            final View v = parent.findChildViewUnder(c.getWidth() / 2, headerEndAt);

            if (isHeaderView(parent, v)) {
                mPinnedHeaderTop = v.getTop() - mPinnedHeaderView.getHeight();
            } else {
                mPinnedHeaderTop = 0;
            }

			if (mClipBounds.isEmpty() {
			    mClipBounds = c.getClipBounds();
                mClipBounds.top = mPinnedHeaderTop + mPinnedHeaderView.getHeight();
                c.clipRect(mClipBounds);
			}
			
			c.restore();
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mPinnedHeaderView != null) {
            c.save();
            
			if (mShadowSize > 0) drawShadow(c, parent);

            mClipBounds.top = 0;
            c.clipRect(mClipBounds);
            c.translate(0, mPinnedHeaderTop);
            mPinnedHeaderView.draw(c);

            c.restore();
        }
    }

    private void drawShadow(Canvas c, RecyclerView parent) {
        if (mAdapter.getItemCount() > mHeaderPosition) {
            final int yPositionWithinHeader = mPinnedHeaderView.getTop() + mPinnedHeaderView.getHeight();
            final int xCenterCanvas = c.getWidth() >> 1;

            View child = parent.findChildViewUnder(xCenterCanvas, yPositionWithinHeader);
            if (child != null) {
                int childAdapterPosition = parent.getChildAdapterPosition(child);

                int viewType = mAdapter.getItemViewType(childAdapterPosition);
                if (!((PinnedHeaderAdapter) mAdapter).isPinnedViewType(viewType)){
                    if (childAdapterPosition > mHeaderPosition){
                        // the Decorator hides an item.
                        int shadowSize = mShadowSize;

                        // check, if an pinned item is incoming
                        if (mAdapter.getItemCount() > (childAdapterPosition+1)) {
                            View nextChild = parent.findChildViewUnder(xCenterCanvas, yPositionWithinHeader+mShadowSize);
                            if (nextChild != null) {
                                final int nextChildPosition = parent.getChildAdapterPosition(nextChild);
                                int nextViewType = mAdapter.getItemViewType(nextChildPosition);
                                if (((PinnedHeaderAdapter) mAdapter).isPinnedViewType(nextViewType)){
                                    int maxShadowSize = nextChild.getTop() - mPinnedHeaderView.getHeight();
                                    if (maxShadowSize <= mShadowSize) {
                                        shadowSize = maxShadowSize;
                                        mShadowPaint.setAlpha(mAlpha / mShadowSize * maxShadowSize);
                                    }
                                }
                            }
                        }

                        // if we are the first child after the decorator, we will fade the shadow in
                        if (childAdapterPosition-1 == mHeaderPosition) {
                            int maxShadowSizeGrowing = mPinnedHeaderView.getHeight() - child.getTop();
                            if (maxShadowSizeGrowing <= mShadowSize && maxShadowSizeGrowing >= 0) {
                                mShadowPaint.setAlpha(mAlpha / mShadowSize * maxShadowSizeGrowing);
                                shadowSize = Math.min(maxShadowSizeGrowing+1, mShadowSize);
                            }
                        }


                        // set the rect for the shadow to be in
                        if (mShadowRect.isEmpty()){
                            mShadowRect.left = mPinnedHeaderView.getLeft();
                            mShadowRect.top = mClipBounds.top;
                            mShadowRect.right = mPinnedHeaderView.getRight();
                        }

                        // size needs to be set every time.
                        mShadowRect.bottom = mShadowRect.top + shadowSize;

                        // fast scrolling can skip the necessary alpha
                        if (shadowSize == mShadowSize){
                            mShadowPaint.setAlpha(mAlpha);
                        }

                        float xPos = 1f;
                        // TODO: update the gradient and stop reallocating
                        Shader shadowGradient = new LinearGradient(
                                xPos, mShadowRect.top-mShadowSize,
                                xPos, mShadowRect.bottom-mShadowSizeOffset, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);

                        mShadowPaint.setShader(shadowGradient);

                        c.drawRect(mShadowRect, mShadowPaint);
                    }
                }
            }
        }
    }

    private void setShadow(int size) {
        mShadowSize = size;
        mShadowSizeOffset = size*0.2f;
    }

    private void createPinnedHeader(RecyclerView parent) {
        checkCache(parent);

        // get LinearLayoutManager.
        final LinearLayoutManager linearLayoutManager;
        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            linearLayoutManager = (LinearLayoutManager) layoutManager;
        } else {
            return;
        }

        final int firstVisiblePosition = linearLayoutManager.findFirstVisibleItemPosition();
        final int headerPosition = findPinnedHeaderPosition(firstVisiblePosition);

        if (headerPosition >= 0 && mHeaderPosition != headerPosition) {
            mHeaderPosition = headerPosition;
            final int viewType = mAdapter.getItemViewType(headerPosition);

            final RecyclerView.ViewHolder pinnedViewHolder = mAdapter.createViewHolder(parent, viewType);
            mAdapter.bindViewHolder(pinnedViewHolder, headerPosition);
            mPinnedHeaderView = pinnedViewHolder.itemView;

            // read layout parameters
            ViewGroup.LayoutParams layoutParams = mPinnedHeaderView.getLayoutParams();
            if (layoutParams == null) {
                layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mPinnedHeaderView.setLayoutParams(layoutParams);
            }

            int heightMode = View.MeasureSpec.getMode(layoutParams.height);
            int heightSize = View.MeasureSpec.getSize(layoutParams.height);

            if (heightMode == View.MeasureSpec.UNSPECIFIED) {
                heightMode = View.MeasureSpec.EXACTLY;
            }

            final int maxHeight = parent.getHeight() - parent.getPaddingTop() - parent.getPaddingBottom();
            if (heightSize > maxHeight) {
                heightSize = maxHeight;
            }

            // measure & layout
            final int ws = View.MeasureSpec.makeMeasureSpec(parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight(), View.MeasureSpec.EXACTLY);
            final int hs = View.MeasureSpec.makeMeasureSpec(heightSize, heightMode);
            mPinnedHeaderView.measure(ws, hs);
            mPinnedHeaderView.layout(0, 0, mPinnedHeaderView.getMeasuredWidth(), mPinnedHeaderView.getMeasuredHeight());
        }
    }

    private int findPinnedHeaderPosition(int fromPosition) {
        if (fromPosition > mAdapter.getItemCount()) {
            return -1;
        }

        for (int position = fromPosition; position >= 0; position--) {
            final int viewType = mAdapter.getItemViewType(position);
            if (isPinnedViewType(viewType)) {
                return position;
            }
        }

        return -1;
    }

    private boolean isPinnedViewType(int viewType) {
        if (!mPinnedViewTypes.containsKey(viewType)) {
            mPinnedViewTypes.put(viewType, ((PinnedHeaderAdapter) mAdapter).isPinnedViewType(viewType));
        }

        return mPinnedViewTypes.get(viewType);
    }

    private boolean isHeaderView(RecyclerView parent, View v) {
        final int position = parent.getChildPosition(v);
        if (position == RecyclerView.NO_POSITION) {
            return false;
        }
        final int viewType = mAdapter.getItemViewType(position);

        return isPinnedViewType(viewType);
    }

    private void checkCache(RecyclerView parent) {
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (mAdapter != adapter) {
            disableCache();
            if (adapter instanceof PinnedHeaderAdapter) {
                mAdapter = adapter;
            } else {
                mAdapter = null;
            }
        }
    }

    private void disableCache() {
        mPinnedHeaderView = null;
        mHeaderPosition = -1;
        mPinnedViewTypes.clear();
    }

}
