/*
 * Copyright (c) 2016. Jahir Fiquitiva. Android Developer. All rights reserved.
 */

/*
 *
 */

package jahirfiquitiva.iconshowcase.views;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import jahirfiquitiva.iconshowcase.adapters.KustomAdapter;


public class SectionedGridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private int spanCount, spacing;
    private boolean includeEdge;
    private KustomAdapter adapter;

    public SectionedGridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge,
                                              KustomAdapter adapter) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
        this.adapter = adapter;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
        int headersBeforeItemPosition = adapter.getHeadersBeforePosition(position);

        //int position = parent.getChildAdapterPosition(view); // item position

        position -= headersBeforeItemPosition;

        int column = position % spanCount; // item column

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)
            if (position > spanCount) { // top edge // test position > spanCount
                outRect.top = spacing;
            }
            outRect.bottom = spacing; // item bottom

        } else {
            outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
            outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
            if (position >= spanCount) {
                outRect.top = spacing; // item top
            }
        }
    }

}