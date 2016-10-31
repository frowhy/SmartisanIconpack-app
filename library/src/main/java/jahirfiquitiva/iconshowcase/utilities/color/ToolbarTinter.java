/*
 * Copyright (C) 2015. Jared Rummler <jared.rummler@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package jahirfiquitiva.iconshowcase.utilities.color;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.widget.ActionMenuView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;

import java.lang.reflect.Method;


/**
 * <p>Apply colors and/or transparency to menu icons in a {@link Menu}.</p>
 * <p>
 * <p>Example usage:</p>
 * <p/>
 * <pre class="prettyprint">
 * public boolean onCreateOptionsMenu(Menu menu) {
 * ...
 * int color = getResources().getColor(R.color.your_awesome_color);
 * int alpha = 204; // 80% transparency
 * ToolbarTinter.on(menu).setMenuItemIconColor(color).setMenuItemIconAlpha(alpha).apply(this);
 * ...
 * }
 * </pre>
 */
public class ToolbarTinter {

    private static final String TAG = "ToolbarTinter";

    private static Method nativeIsActionButton;

    /**
     * Check if an item is showing (not in the overflow menu).
     *
     * @param item the MenuItem.
     *
     * @return {@code true} if the MenuItem is visible on the ActionBar.
     */
    public static boolean isActionButton(MenuItem item) {
        if (item instanceof MenuItemImpl) {
            return ((MenuItemImpl) item).isActionButton();
        }
        if (nativeIsActionButton == null) {
            try {
                Class<?> MenuItemImpl = Class.forName("com.android.internal.view.menu.MenuItemImpl");
                nativeIsActionButton = MenuItemImpl.getDeclaredMethod("isActionButton");
                if (!nativeIsActionButton.isAccessible()) {
                    nativeIsActionButton.setAccessible(true);
                }
            } catch (Exception ignored) {
            }
        }
        try {
            return (boolean) nativeIsActionButton.invoke(item, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Check if an item is in the overflow menu.
     *
     * @param item the MenuItem
     *
     * @return {@code true} if the MenuItem is in the overflow menu.
     *
     * @see #isActionButton(MenuItem)
     */
    public static boolean isInOverflow(MenuItem item) {
        return !isActionButton(item);
    }

    /**
     * Sets the color filter and/or the alpha transparency on a {@link MenuItem}'s icon.
     *
     * @param menuItem The {@link MenuItem} to theme.
     * @param color    The color to set for the color filter or {@code null} for no changes.
     */
    public static void colorMenuItem(MenuItem menuItem, Integer color, Integer alpha) {
        if (color == null) {
            return; // nothing to do.
        }
        Drawable drawable = menuItem.getIcon();
        if (drawable != null) {
            // If we don't mutate the drawable, then all drawables with this id will have the ColorFilter
            drawable.mutate();
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            if (alpha != null) {
                drawable.setAlpha(alpha);
            } else {
                drawable.setAlpha(255);
            }
        }
    }

    /**
     * Set the menu to show MenuItem icons in the overflow window.
     *
     * @param menu the menu to force icons to show
     */
    public static void forceMenuIcons(Menu menu) {
        try {
            Class<?> MenuBuilder = menu.getClass();
            Method setOptionalIconsVisible =
                    MenuBuilder.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            if (!setOptionalIconsVisible.isAccessible()) {
                setOptionalIconsVisible.setAccessible(true);
            }
            setOptionalIconsVisible.invoke(menu, true);
        } catch (Exception ignored) {
        }
    }

    public static Builder on(Menu menu) {
        return new Builder(menu);
    }

    /**
     * Apply a ColorFilter with the specified color to all icons in the menu.
     *
     * @param activity the Activity.
     * @param menu     the menu after items have been added.
     * @param color    the color for the ColorFilter.
     */
    public static void colorIcons(Activity activity, Menu menu, int color) {
        ToolbarTinter.on(menu).setIconsColor(color).apply(activity);
    }

    /**
     * @param activity the Activity
     *
     * @return the OverflowMenuButton or {@code null} if it doesn't exist.
     */
    public static ImageView getOverflowMenuButton(Activity activity) {
        return findOverflowMenuButton(activity, findActionBar(activity));
    }

    private static ImageView findOverflowMenuButton(Activity activity, ViewGroup viewGroup) {
        if (viewGroup == null) {
            return null;
        }
        ImageView overflow = null;
        for (int i = 0, count = viewGroup.getChildCount(); i < count; i++) {
            View v = viewGroup.getChildAt(i);
            if (v instanceof ImageView
                    && (v.getClass().getSimpleName().equals("OverflowMenuButton")
                    || v instanceof ActionMenuView.ActionMenuChildView)) {
                overflow = (ImageView) v;
            } else if (v instanceof ViewGroup) {
                overflow = findOverflowMenuButton(activity, (ViewGroup) v);
            }
            if (overflow != null) {
                break;
            }
        }
        return overflow;
    }

    private static ViewGroup findActionBar(Activity activity) {
        int id = activity.getResources().getIdentifier("action_bar", "id", "android");
        ViewGroup actionBar = null;
        if (id != 0) {
            actionBar = (ViewGroup) activity.findViewById(id);
        }
        if (actionBar == null) {
            actionBar = findToolbar((ViewGroup)
                    activity.findViewById(android.R.id.content).getRootView());
        }
        return actionBar;
    }

    private static ViewGroup findToolbar(ViewGroup viewGroup) {
        ViewGroup toolbar = null;
        for (int i = 0, len = viewGroup.getChildCount(); i < len; i++) {
            View view = viewGroup.getChildAt(i);
            if (view.getClass() == android.support.v7.widget.Toolbar.class
                    || view.getClass().getName().equals("android.widget.Toolbar")) {
                toolbar = (ViewGroup) view;
            } else if (view instanceof ViewGroup) {
                toolbar = findToolbar((ViewGroup) view);
            }
            if (toolbar != null) {
                break;
            }
        }
        return toolbar;
    }

    private final Menu menu;
    private final Integer originalIconsColor;
    private final Integer overflowDrawableId;
    private final boolean reApplyOnChange;
    private final boolean forceIcons;
    private Integer iconsColor;
    private Integer iconsAlpha;
    private ImageView overflowButton;
    private ViewGroup actionBarView;

    private ToolbarTinter(Builder builder) {
        menu = builder.menu;
        originalIconsColor = builder.originalIconsColor;
        iconsColor = builder.iconsColor;
        iconsAlpha = builder.iconsAlpha;
        overflowDrawableId = builder.overflowDrawableId;
        reApplyOnChange = builder.reApplyOnChange;
        forceIcons = builder.forceIcons;
    }

    /**
     * <p>Sets a ColorFilter and/or alpha on all the {@link MenuItem}s in the menu, including the
     * OverflowMenuButton.</p>
     * <p>
     * <p>Call this method after inflating/creating your menu in
     * {@link Activity#onCreateOptionsMenu(Menu)}.</p>
     * <p>
     * <p>Note: This is targeted for the native ActionBar/Toolbar, not AppCompat.</p>
     *
     * @param activity the activity to apply the menu tinting on.
     */
    public void apply(final Activity activity) {

        if (menu != null) {
            if (forceIcons) {
                forceMenuIcons(menu);
            }

            for (int i = 0, size = menu.size(); i < size; i++) {
                MenuItem item = menu.getItem(i);
                colorMenuItem(item, iconsColor, iconsAlpha);
                if (reApplyOnChange) {
                    View view = item.getActionView();
                    if (view != null) {
                        if (item instanceof MenuItemImpl) {
                            ((MenuItemImpl) item).setSupportOnActionExpandListener(
                                    new SupportActionExpandListener(this));
                        } else {
                            item.setOnActionExpandListener(new NativeActionExpandListener(this));
                        }
                    }
                }
            }
        }

        actionBarView = findActionBar(activity);
        if (actionBarView == null) {
            Log.w(TAG, "Could not find the ActionBar");
            return;
        }

        // We must wait for the view to be created to set a color filter on the drawables.
        actionBarView.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < actionBarView.getChildCount(); i++) {
                    final View v = actionBarView.getChildAt(i);

                    //Step 1 : Changing the color of back button (or open drawer button).
                    if (v instanceof ImageButton) {
                        //Action Bar back button
                        ((ImageButton) v).getDrawable().setColorFilter(
                                new PorterDuffColorFilter(iconsColor, PorterDuff.Mode.SRC_ATOP));
                    }
                }
                if (menu != null) {
                    for (int i = 0, size = menu.size(); i < size; i++) {
                        MenuItem menuItem = menu.getItem(i);
                        if (isInOverflow(menuItem)) {
                            colorMenuItem(menuItem, iconsColor, iconsAlpha);
                        }
                        if (menuItem.hasSubMenu()) {
                            SubMenu subMenu = menuItem.getSubMenu();
                            for (int j = 0; j < subMenu.size(); j++) {
                                colorMenuItem(subMenu.getItem(j), iconsColor, iconsAlpha);
                            }
                        }
                    }
                    if (iconsColor != null) {
                        overflowButton = findOverflowMenuButton(activity, actionBarView);
                        colorOverflowMenuItem(overflowButton);
                    }
                }
            }
        });
    }

    /**
     * <p>Sets a ColorFilter and/or alpha on all the {@link MenuItem}s in the menu, including the
     * OverflowMenuButton.</p>
     * <p>
     * <p>This should only be called after calling {@link #apply(Activity)}. It is useful for when
     * {@link MenuItem}s might be re-arranged due to an action view being collapsed or expanded.</p>
     */
    public void reapply() {

        if (menu != null) {
            for (int i = 0, size = menu.size(); i < size; i++) {
                MenuItem item = menu.getItem(i);
                if (isActionButton(item)) {
                    colorMenuItem(menu.getItem(i), iconsColor, iconsAlpha);
                }
            }
        }

        if (actionBarView == null) {
            return;
        }

        actionBarView.post(new Runnable() {

            @Override
            public void run() {
                if (menu != null) {
                    for (int i = 0, size = menu.size(); i < size; i++) {
                        MenuItem menuItem = menu.getItem(i);
                        if (isInOverflow(menuItem)) {
                            colorMenuItem(menuItem, iconsColor, iconsAlpha);
                        } else {
                            colorMenuItem(menu.getItem(i), iconsColor, iconsAlpha);
                        }
                        if (menuItem.hasSubMenu()) {
                            SubMenu subMenu = menuItem.getSubMenu();
                            for (int j = 0; j < subMenu.size(); j++) {
                                colorMenuItem(subMenu.getItem(j), iconsColor, iconsAlpha);
                            }
                        }
                    }
                }
                if (iconsColor != null || iconsAlpha != null) {
                    colorOverflowMenuItem(overflowButton);
                }
            }

        });
    }

    private void colorOverflowMenuItem(ImageView overflow) {
        if (overflow != null) {
            if (overflowDrawableId != null) {
                overflow.setImageResource(overflowDrawableId);
            }
            if (iconsColor != null) {
                overflow.setColorFilter(iconsColor);
            }
            if (iconsAlpha == null) {
                iconsAlpha = 255;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                overflow.setImageAlpha(iconsAlpha);
            } else {
                overflow.setAlpha(iconsAlpha);
            }
        }
    }

    public Menu getMenu() {
        return menu;
    }

    public ImageView getOverflowMenuButton() {
        return overflowButton;
    }

    public void setMenuItemIconColor(Integer color) {
        iconsColor = color;
    }

    public static class NativeActionExpandListener implements OnActionExpandListener {

        private final ToolbarTinter menuTint;

        public NativeActionExpandListener(ToolbarTinter menuTint) {
            this.menuTint = menuTint;
        }

        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            int color = menuTint.iconsColor != null ? menuTint.iconsColor :
                    menuTint.originalIconsColor;
            menuTint.setMenuItemIconColor(color);
            menuTint.reapply();
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            int color = menuTint.iconsColor != null ? menuTint.iconsColor :
                    menuTint.originalIconsColor;
            menuTint.setMenuItemIconColor(color);
            menuTint.reapply();
            return true;
        }

    }

    public static class SupportActionExpandListener implements
            MenuItemCompat.OnActionExpandListener {

        private final ToolbarTinter menuTint;

        public SupportActionExpandListener(ToolbarTinter menuTint) {
            this.menuTint = menuTint;
        }

        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            int color = menuTint.iconsColor != null ? menuTint.iconsColor :
                    menuTint.originalIconsColor;
            menuTint.setMenuItemIconColor(color);
            menuTint.reapply();
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            int color = menuTint.iconsColor != null ? menuTint.iconsColor :
                    menuTint.originalIconsColor;
            menuTint.setMenuItemIconColor(color);
            menuTint.reapply();
            return true;
        }

    }

    // --------------------------------------------------------------------------------------------

    public static final class Builder {

        private final Menu menu;
        private Integer iconsColor;
        private Integer iconsAlpha;
        private Integer overflowDrawableId;
        private Integer originalIconsColor;
        private boolean reApplyOnChange;
        private boolean forceIcons;

        private Builder(Menu menu) {
            this.menu = menu;
        }

        /**
         * <p>Sets an {@link OnActionExpandListener} on all {@link MenuItem}s with views, so when the
         * menu is updated, the colors will be also.</p>
         * <p>
         * <p>This is useful when the overflow menu is showing icons and {@link MenuItem}s might be
         * pushed to the overflow menu when a action view is expanded e.g. android.widget.SearchView.
         * </p>
         *
         * @param reapply {@code true} to set the listeners on all {@link MenuItem}s with action views.
         *
         * @return this Builder object to allow for chaining of calls to set methods
         */
        public Builder reapplyOnChange(boolean reapply) {
            reApplyOnChange = reapply;
            return this;
        }

        /**
         * Specify a color for visible MenuItem icons, including the OverflowMenuButton.
         *
         * @param color the color to apply on visible MenuItem icons, including the OverflowMenuButton.
         *
         * @return this Builder object to allow for chaining of calls to set methods
         */
        public Builder setIconsColor(int color) {
            iconsColor = color;
            return this;
        }

        /**
         * Specify a color that is applied when an action view is expanded or collapsed.
         *
         * @param color the color to apply on MenuItems when an action-view is expanded or collapsed.
         *
         * @return this Builder object to allow for chaining of calls to set methods
         */
        public Builder setOriginalIconsColor(int color) {
            originalIconsColor = color;
            return this;
        }

        /**
         * Set the drawable id to set on the OverflowMenuButton.
         *
         * @param drawableId the resource identifier of the drawable
         *
         * @return this Builder object to allow for chaining of calls to set methods
         */
        public Builder setOverflowDrawableId(int drawableId) {
            overflowDrawableId = drawableId;
            return this;
        }

        public Builder setIconsAlpha(int alpha) {
            iconsAlpha = alpha;
            return this;
        }

        /**
         * Set the menu to show MenuItem icons in the overflow window.
         *
         * @return this Builder object to allow for chaining of calls to set methods
         */
        public Builder forceIcons() {
            forceIcons = true;
            return this;
        }

        /**
         * <p>Sets a ColorFilter and/or alpha on all the MenuItems in the menu, including the
         * OverflowMenuButton.</p>
         * <p>
         * <p>Call this method after inflating/creating your menu in</p>
         * {@link Activity#onCreateOptionsMenu(Menu)}.</p>
         * <p>
         * <p>Note: This is targeted for the native ActionBar/Toolbar, not AppCompat.</p>
         */
        public ToolbarTinter apply(Activity activity) {
            ToolbarTinter theme = new ToolbarTinter(this);
            theme.apply(activity);
            return theme;
        }

        /**
         * <p>Creates a {@link ToolbarTinter} with the arguments supplied to this builder.</p>
         * <p>
         * <p>It does not apply the theme. Call {@link ToolbarTinter#apply(Activity)} to do so.</p>
         *
         * @see #apply(Activity)
         */
        public ToolbarTinter create() {
            return new ToolbarTinter(this);
        }

    }

    // --------------------------------------------------------------------------------------------

    /**
     * Auto collapses the SearchView when the soft keyboard is dismissed.
     */
    public static class SearchViewFocusListener implements View.OnFocusChangeListener {

        private final MenuItem item;

        public SearchViewFocusListener(MenuItem item) {
            this.item = item;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus && item != null) {
                item.collapseActionView();
                if (v instanceof SearchView) {
                    ((SearchView) v).setQuery("", false);
                }
            }
        }

    }

}