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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.activities.ShowcaseActivity;
import jahirfiquitiva.iconshowcase.adapters.HomeListAdapter;
import jahirfiquitiva.iconshowcase.models.HomeCard;
import jahirfiquitiva.iconshowcase.utilities.LauncherIntents;
import jahirfiquitiva.iconshowcase.utilities.Utils;
import jahirfiquitiva.iconshowcase.views.DividerItemDecoration;


public class MainFragment extends Fragment {

    private Context context;
    private ViewGroup layout;

    private boolean themeMode, cm, cyngn, rro; //to store theme engine installation status

    private FloatingActionButton fab;
    private final ArrayList<HomeCard> homeCards = new ArrayList<>();
    private boolean hasAppsList = false;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        String[] appsNames = getResources().getStringArray(R.array.apps_titles);
        String[] appsDescriptions = getResources().getStringArray(R.array.apps_descriptions);
        String[] appsIcons = getResources().getStringArray(R.array.apps_icons);
        String[] appsPackages = getResources().getStringArray(R.array.apps_packages);

        int names = appsNames.length, descs = appsDescriptions.length, icons = appsIcons.length,
                packs = appsPackages.length;

        if (names > 0 && names == descs && names == icons && names == packs) {
            hasAppsList = true;
        }

        context = getActivity();

        if (layout != null) {
            ViewGroup parent = (ViewGroup) layout.getParent();
            if (parent != null) {
                parent.removeView(layout);
            }
        }
        try {
            layout = (ViewGroup) inflater.inflate(R.layout.main_section, container, false);
        } catch (InflateException e) {
            //Do nothing
        }

        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

        themeMode = getResources().getBoolean(R.bool.theme_mode);
        RecyclerView mRecyclerView = (RecyclerView) layout.findViewById(R.id.home_rv);

        if (!themeMode) {
            setupAndAnimateIcons(true, true, 600);

            GridLayout iconsRow = (GridLayout) getActivity().findViewById(R.id.iconsRow);

            iconsRow.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ShowcaseActivity.SHUFFLE = true;
                    setupAndAnimateIcons(true, true, 0);
                }
            });

        }

        if (hasAppsList) {
            for (int i = 0; i < appsNames.length; i++) {
                try {
                    if (appsPackages[i].indexOf("http") != -1) { //checks if package is a site
                        homeCards.add(new HomeCard.Builder()
                                .context(getActivity())
                                .title(appsNames[i])
                                .description(appsDescriptions[i])
                                .icon(ContextCompat.getDrawable(context,
                                        Utils.getIconResId(context, getResources(),
                                                context.getPackageName(), appsIcons[i],
                                                null)))
                                .onClickLink(appsPackages[i], false, false, null)
                                .build());
                        continue;
                    }
                    Intent intent;
                    boolean isInstalled = Utils.isAppInstalled(context, appsPackages[i]);
                    if (isInstalled) {
                        PackageManager pm = context.getPackageManager();
                        intent = pm.getLaunchIntentForPackage(appsPackages[i]);
                        if (intent != null) {
                            try {
                                homeCards.add(new HomeCard.Builder()
                                        .context(getActivity())
                                        .title(appsNames[i])
                                        .description(appsDescriptions[i])
                                        .icon(ContextCompat.getDrawable(context,
                                                Utils.getIconResId(context, getResources(),
                                                        context.getPackageName(), appsIcons[i],
                                                        null)))
                                        .onClickLink(appsPackages[i], true, true, intent)
                                        .build());
                            } catch (Resources.NotFoundException e) {
                                if (ShowcaseActivity.DEBUGGING)
                                    Utils.showLog(context, "There's no icon that matches name: "
                                            + appsIcons[i]);
                                homeCards.add(new HomeCard.Builder()
                                        .context(getActivity())
                                        .title(appsNames[i])
                                        .description(appsDescriptions[i])
                                        .icon(ContextCompat.getDrawable(context,
                                                Utils.getIconResId(context, getResources(),
                                                        context.getPackageName(), "ic_na_launcher",
                                                        null)))
                                        .onClickLink(appsPackages[i], true, true, intent)
                                        .build());
                            }
                        }
                    } else {
                        try {
                            homeCards.add(new HomeCard.Builder()
                                    .context(getActivity())
                                    .title(appsNames[i])
                                    .description(appsDescriptions[i])
                                    .icon(ContextCompat.getDrawable(context,
                                            Utils.getIconResId(context, getResources(),
                                                    context.getPackageName(), appsIcons[i],
                                                    null)))
                                    .onClickLink(appsPackages[i], true, false, null)
                                    .build());
                        } catch (Resources.NotFoundException e) {
                            if (ShowcaseActivity.DEBUGGING)
                                Utils.showLog(context, "There's no icon that matches name: " + appsIcons[i]);
                            homeCards.add(new HomeCard.Builder()
                                    .context(getActivity())
                                    .title(appsNames[i])
                                    .description(appsDescriptions[i])
                                    .icon(ContextCompat.getDrawable(context,
                                            Utils.getIconResId(context, getResources(),
                                                    context.getPackageName(), "ic_na_launcher",
                                                    null)))
                                    .onClickLink(appsPackages[i], true, false, null)
                                    .build());
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    hasAppsList = false;
                    if (ShowcaseActivity.DEBUGGING)
                        Utils.showLog(context, "Apps Cards arrays are inconsistent. Fix them.");
                }
            }
        }

        setupFAB();

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                null, getResources().getDimensionPixelSize(R.dimen.dividers_height), false, true));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);

        HomeListAdapter mAdapter = new HomeListAdapter(homeCards, context, hasAppsList);
        mRecyclerView.setAdapter(mAdapter);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupFAB();
        if (ShowcaseActivity.iconsPicker || ShowcaseActivity.wallsPicker) {
            Utils.collapseToolbar(getActivity());
        } else {
            Utils.expandToolbar(getActivity());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if (fab != null && ShowcaseActivity.currentItem != 1) {
            fab.hide();
            fab.setVisibility(View.GONE);
        }
    }

    private void setupFAB() {
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

        if (themeMode) {
            modifyFABIcon();
            fab.show();
            fab.setVisibility(View.VISIBLE);
        } else {
            if (!hasAppsList) {
                fab.show();
                fab.setVisibility(View.VISIBLE);
            } else {
                fab.hide();
                fab.setVisibility(View.GONE);
            }
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (themeMode) {
                    if (cm || cyngn) {
                        new LauncherIntents(getActivity(), "Cmthemeengine");
                    } else if (rro) {
                        new LauncherIntents(getActivity(), "Layers");
                    } else {
                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.NTED_title)
                                .content(R.string.NTED_message)
                                .show();
                    }
                } else {
                    Intent rate = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://a.app.qq.com/o/simple.jsp?pkgname=" +
                                    context.getPackageName()));
                    context.startActivity(rate);
                }
            }
        });

    }

    private void modifyFABIcon() {
        cm = Utils.isAppInstalled(context, "org.cyanogenmod.theme.chooser");
        cyngn = Utils.isAppInstalled(context, "com.cyngn.theme.chooser");

        //don't enable rro before lollipop, it didn't exist before that
        rro = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                Utils.isAppInstalled(context, "com.lovejoy777.rroandlayersmanager");

        if (cm || cyngn) {
            fab.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_apply_cm));
        } else if (rro) {
            fab.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_apply_layers));
        } else {
            fab.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_apply));
        }
    }

    private void setupAndAnimateIcons(boolean justSetup, boolean animate, int delay) {
        if (justSetup) {
            ((ShowcaseActivity) getActivity()).setupIcons();
        }
        if (animate) {
            ((ShowcaseActivity) getActivity()).animateIcons(delay);
        }
    }

}
