/*
 * Copyright (c) 2016.  Jahir Fiquitiva
 *
 * Licensed under the CreativeCommons Attribution-ShareAlike
 * 4.0 International License. You may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *    http://creativecommons.org/licenses/by-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Big thanks to the project contributors. Check them in the repository.
 *
 */

/*
 *
 */

package jahirfiquitiva.iconshowcase.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import java.util.ArrayList;
import java.util.Locale;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.activities.ShowcaseActivity;
import jahirfiquitiva.iconshowcase.fragments.base.FragmentStatePagerAdapter;
import jahirfiquitiva.iconshowcase.models.IconsCategory;
import jahirfiquitiva.iconshowcase.tasks.LoadIconsLists;
import jahirfiquitiva.iconshowcase.utilities.ThemeUtils;
import jahirfiquitiva.iconshowcase.utilities.Utils;
import jahirfiquitiva.iconshowcase.utilities.color.ToolbarColorizer;
import jahirfiquitiva.iconshowcase.utilities.color.ToolbarTinter;


@SuppressWarnings("ResourceAsColor")
public class PreviewsFragment extends Fragment {

    private int mLastSelected = 0;
    private ViewPager mPager;
    private String[] tabs;
    private ViewGroup layout;
    private TabLayout mTabs;
    private SearchView mSearchView;
    private ArrayList<IconsCategory> categories;
    private MenuItem mSearchItem;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (layout != null) {
            ViewGroup parent = (ViewGroup) layout.getParent();
            if (parent != null) {
                parent.removeView(layout);
            }
        }

        try {
            layout = (ViewGroup) inflater.inflate(R.layout.icons_preview_section, container, false);
        } catch (InflateException e) {
            //Do nothing
        }

        categories = LoadIconsLists.getIconsCategories();

        return layout;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int iconsColor = ThemeUtils.darkTheme ?
                ContextCompat.getColor(getActivity(), R.color.toolbar_text_dark) :
                ContextCompat.getColor(getActivity(), R.color.toolbar_text_light);

        if (getActivity() != null && mSearchItem != null) {
            ToolbarColorizer.tintSearchView(getActivity(),
                    ((ShowcaseActivity) getActivity()).getToolbar(), mSearchItem, mSearchView,
                    iconsColor);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Utils.collapseToolbar(getActivity());

        if (mPager == null) {
            mPager = (ViewPager) layout.findViewById(R.id.pager);
            mPager.setOffscreenPageLimit(getPageLimit());
            mPager.setAdapter(new IconsPagerAdapter(getChildFragmentManager()));
            createTabs();
        }

    }

    private int getPageLimit() {
        if (categories != null) {
            int categoriesNum = categories.size();
            int halfCategories = categoriesNum / 2;
            int limit = categoriesNum - halfCategories;
            return limit < 1 ? 1 : limit;
        } else {
            return 2;
        }
    }

    private void createTabs() {
        mTabs = (TabLayout) getActivity().findViewById(R.id.tabs);
        mTabs.setVisibility(View.VISIBLE);
        mTabs.setupWithViewPager(mPager);
        mTabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mPager.setCurrentItem(tab.getPosition());
                if (mLastSelected > -1) {
                    IconsFragment frag = (IconsFragment) getChildFragmentManager().findFragmentByTag("page:" + mLastSelected);
                    if (frag != null)
                        frag.performSearch(null);
                }
                mLastSelected = tab.getPosition();
                if (mSearchView != null && getActivity() != null)
                    mSearchView.setQueryHint(getString(R.string.search_x, tabs[mLastSelected]));
                if (getActivity() != null)
                    getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mTabs != null) mTabs.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search, menu);
        mSearchItem = menu.findItem(R.id.search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        mSearchView.setQueryHint(getString(R.string.search_x, tabs[mLastSelected]));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                search(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                search(s);
                return false;
            }

            private void search(String s) {
                IconsFragment frag =
                        (IconsFragment) getChildFragmentManager().findFragmentByTag("page:" +
                                mPager.getCurrentItem());
                if (frag != null)
                    frag.performSearch(s);
            }
        });

        mSearchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        int iconsColor = ThemeUtils.darkTheme ?
                ContextCompat.getColor(getActivity(), R.color.toolbar_text_dark) :
                ContextCompat.getColor(getActivity(), R.color.toolbar_text_light);

        ToolbarTinter.on(menu)
                .setIconsColor(iconsColor)
                .forceIcons()
                .reapplyOnChange(true)
                .apply(getActivity());

    }

    class IconsPagerAdapter extends FragmentStatePagerAdapter {

        public IconsPagerAdapter(FragmentManager fm) {
            super(fm);
            String[] tabsNames = new String[categories.size()];
            for (int i = 0; i < tabsNames.length; i++) {
                tabsNames[i] = categories.get(i).getCategoryName();
            }
            tabs = tabsNames;
        }

        @Override
        public Fragment getItem(int position) {
            return IconsFragment.newInstance(categories.get(position));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabs[position].toUpperCase(Locale.getDefault());
        }

        @Override
        public int getCount() {
            return tabs.length;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save current position
        savedInstanceState.putInt("lastSelected", mLastSelected);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore last selected position
            mLastSelected = savedInstanceState.getInt("lastSelected", 0);
        }
    }
}