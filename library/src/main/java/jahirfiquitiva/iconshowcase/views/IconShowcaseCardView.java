/*
 * Copyright (c) 2016. Jahir Fiquitiva. Android Developer. All rights reserved.
 */

package jahirfiquitiva.iconshowcase.views;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.utilities.ThemeUtils;


public class IconShowcaseCardView extends CardView {

    private int rightCardColor;

    public IconShowcaseCardView(Context context) {
        super(context);
        setupRightCardColor(context);
    }

    public IconShowcaseCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupRightCardColor(context);
    }

    public IconShowcaseCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupRightCardColor(context);
    }

    @Override
    public void setCardBackgroundColor(int ignoredColor) {
        super.setCardBackgroundColor(rightCardColor);
    }

    private void setupRightCardColor(Context context) {
        if (ThemeUtils.darkTheme) {
            if (ThemeUtils.transparent) {
                rightCardColor = ContextCompat.getColor(context, R.color.card_clear_background);
            } else {
                rightCardColor = ContextCompat.getColor(context, R.color.card_dark_background);
            }
        } else {
            rightCardColor = ContextCompat.getColor(context, R.color.card_light_background);
        }
    }

}