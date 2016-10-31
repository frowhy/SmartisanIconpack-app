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

package jahirfiquitiva.iconshowcase.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.util.ArrayList;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.activities.ShowcaseActivity;
import jahirfiquitiva.iconshowcase.adapters.KustomAdapter;
import jahirfiquitiva.iconshowcase.dialogs.ISDialogs;
import jahirfiquitiva.iconshowcase.tasks.LoadKustomFiles;
import jahirfiquitiva.iconshowcase.utilities.Utils;
import jahirfiquitiva.iconshowcase.views.SectionedGridSpacingItemDecoration;


public class KustomFragment extends Fragment {

    private static ViewGroup layout;
    private static Context context;
    private FloatingActionButton kustomFAB;
    public RecyclerView mRecyclerView;
    public KustomAdapter kustomAdapter;
    private SectionedGridSpacingItemDecoration space;

    private final String KLWP_PKG = "org.kustom.wallpaper",
            KWGT_PKG = "org.kustom.widget",
            KOLORETTE_PKG = "com.arun.themeutil.kolorette";

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        context = getActivity();

        if (layout != null) {
            ViewGroup parent = (ViewGroup) layout.getParent();
            if (parent != null) {
                parent.removeView(layout);
            }
        }
        try {
            layout = (ViewGroup) inflater.inflate(R.layout.zooper_section, container, false);
        } catch (InflateException e) {
            //Do nothing
        }

        setupFAB();

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.collapseToolbar(getActivity());
        setupFAB();
        setupRV();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (kustomFAB != null) {
            kustomFAB.hide();
            kustomFAB.setVisibility(View.GONE);
        }
    }

    private void setupRV() {
        if (layout != null) {

            int gridSpacing = getResources().getDimensionPixelSize(R.dimen.lists_padding);
            final int columnsNumber = getResources().getInteger(R.integer.zooper_kustom_grid_width);

            mRecyclerView = (RecyclerView) layout.findViewById(R.id.zooper_rv);

            if (space != null) {
                mRecyclerView.removeItemDecoration(space);
            }

            GridLayoutManager gridManager = new GridLayoutManager(context, columnsNumber);

            RecyclerFastScroller fastScroller = (RecyclerFastScroller) layout.findViewById(R.id.rvFastScroller);

            kustomAdapter = new KustomAdapter(context,
                    LoadKustomFiles.komponents, LoadKustomFiles.wallpapers,
                    LoadKustomFiles.widgets, ShowcaseActivity.wallpaperDrawable);

            space = new SectionedGridSpacingItemDecoration(columnsNumber, gridSpacing, true, kustomAdapter);

            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (kustomAdapter.isHeader(position)) {
                        return columnsNumber;
                    } else {
                        return 1;
                    }
                }
            });

            mRecyclerView.addItemDecoration(space);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(gridManager);
            kustomAdapter.setLayoutManager(gridManager);
            mRecyclerView.setAdapter(kustomAdapter);
            fastScroller.attachRecyclerView(mRecyclerView);

        }
    }

    private boolean areAppsInstalled() {
        boolean installed = true;

        if ((context.getResources().getBoolean(R.bool.includes_kustom_wallpapers))) {
            installed = Utils.isAppInstalled(context, KLWP_PKG);
        }

        if ((context.getResources().getBoolean(R.bool.includes_kustom_widgets)) && installed) {
            installed = Utils.isAppInstalled(context, KWGT_PKG);
        }

        if ((context.getResources().getBoolean(R.bool.kustom_requires_kolorette)) && installed) {
            installed = Utils.isAppInstalled(context, KOLORETTE_PKG);
        }

        return installed;
    }

    private void setupFAB() {
        if (layout != null) {
            kustomFAB = (FloatingActionButton) layout.findViewById(R.id.kustom_fab);

            if (areAppsInstalled()) {
                kustomFAB.hide();
                kustomFAB.setVisibility(View.GONE);
            } else {
                kustomFAB.show();
                kustomFAB.setVisibility(View.VISIBLE);

                kustomFAB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<String> apps = new ArrayList<>();
                        if (context.getResources().getBoolean(R.bool.includes_kustom_wallpapers) &&
                                !Utils.isAppInstalled(context, KLWP_PKG)) {
                            apps.add("Kustom Live Wallpaper");
                        }
                        if (context.getResources().getBoolean(R.bool.includes_kustom_widgets) &&
                                !Utils.isAppInstalled(context, KWGT_PKG)) {
                            apps.add("Kustom Widget");
                        }
                        if (context.getResources().getBoolean(R.bool.kustom_requires_kolorette) &&
                                !Utils.isAppInstalled(context, KOLORETTE_PKG)) {
                            apps.add(Utils.getStringFromResources(context, R.string.kolorette_app));
                        }
                        if (apps.size() > 0) {
                            ISDialogs.showKustomAppsDownloadDialog(context, apps);
                        }
                    }
                });
            }
        }
    }

}