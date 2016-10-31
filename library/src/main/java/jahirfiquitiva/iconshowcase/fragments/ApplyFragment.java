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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.adapters.LaunchersAdapter;
import jahirfiquitiva.iconshowcase.dialogs.ISDialogs;
import jahirfiquitiva.iconshowcase.utilities.LauncherIntents;
import jahirfiquitiva.iconshowcase.utilities.Preferences;
import jahirfiquitiva.iconshowcase.utilities.Utils;
import jahirfiquitiva.iconshowcase.utilities.sort.InstalledLauncherComparator;
import jahirfiquitiva.iconshowcase.views.GridSpacingItemDecoration;

public class ApplyFragment extends Fragment {

    private static final String MARKET_URL = "http://a.app.qq.com/o/simple.jsp?pkgname=";

    private String intentString;
    private final List<Launcher> launchers = new ArrayList<>();
    private RecyclerView recyclerView;

    private Preferences mPrefs;

    private ViewGroup layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (layout != null) {
            ViewGroup parent = (ViewGroup) layout.getParent();
            if (parent != null) {
                parent.removeView(layout);
            }
        }
        try {
            layout = (ViewGroup) inflater.inflate(R.layout.apply_section, container, false);
        } catch (InflateException e) {
            //Do nothing
        }

        mPrefs = new Preferences(getActivity());

        showApplyAdviceDialog(getActivity());

        recyclerView = (RecyclerView) layout.findViewById(R.id.launchersList);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                getResources().getInteger(R.integer.launchers_grid_width)));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(
                new GridSpacingItemDecoration(getResources().getInteger(R.integer.launchers_grid_width),
                        getResources().getDimensionPixelSize(R.dimen.lists_padding),
                        true));

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.collapseToolbar(getActivity());
        updateLaunchersList();
    }

    private void updateLaunchersList() {

        launchers.clear();

        // Splits all launcher  arrays by the | delimiter {name}|{package}
        final String[] launcherArray = getResources().getStringArray(R.array.launchers);
        final int[] launcherColors = getResources().getIntArray(R.array.launcher_colors);
        for (int i = 0; i < launcherArray.length; i++) {
            launchers.add(new Launcher(launcherArray[i].split("\\|"), launcherColors[i]));
        }
        Collections.sort(launchers, new InstalledLauncherComparator(getActivity()));

        LaunchersAdapter adapter = new LaunchersAdapter(getActivity(), launchers,
                new LaunchersAdapter.ClickListener() {
                    @Override
                    public void onClick(int position) {
                        if (launchers.get(position).name.equals("Google Now")) {
                            gnlDialog();
                        } else if (launchers.get(position).name.equals("LG Home")) {
                            if (Utils.isAppInstalled(getActivity(), launchers.get(position).packageName)) {
                                openLauncher(launchers.get(position).name);
                            } else {
                                new MaterialDialog.Builder(getActivity())
                                        .content(R.string.lg_dialog_content)
                                        .positiveText(android.R.string.ok)
                                        .show();
                            }
                        } else if (launchers.get(position).name.equals("CM Theme Engine")) {
                            if (Utils.isAppInstalled(getActivity(), "com.cyngn.theme.chooser")) {
                                openLauncher("CM Theme Engine");
                            } else if (Utils.isAppInstalled(getActivity(), launchers.get(position).packageName)) {
                                openLauncher(launchers.get(position).name);
                            }
                        } else if (Utils.isAppInstalled(getActivity(), launchers.get(position).packageName)) {
                            openLauncher(launchers.get(position).name);
                        } else {
                            openInPlayStore(launchers.get(position));
                        }
                    }
                });
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        RecyclerFastScroller fastScroller = (RecyclerFastScroller) layout.findViewById(R.id.rvFastScroller);
        fastScroller.attachRecyclerView(recyclerView);
    }

    private void openLauncher(String name) {
        final String launcherName = Character.toUpperCase(name.charAt(0))
                + name.substring(1).toLowerCase().replace(" ", "").replace("launcher", "");
        new LauncherIntents(getActivity(), launcherName);
    }

    private void openInPlayStore(final Launcher launcher) {
        intentString = MARKET_URL + launcher.packageName;
        final String LauncherName = launcher.name;
        final String cmName = "CM Theme Engine";
        String dialogContent;
        if (LauncherName.equals(cmName)) {
            dialogContent = getResources().getString(R.string.cm_dialog_content, launcher.name);
            intentString = "http://download.cyanogenmod.org/";
        } else {
            dialogContent = getResources().getString(R.string.lni_content, launcher.name);
            intentString = MARKET_URL + launcher.packageName;
        }
        ISDialogs.showOpenInPlayStoreDialog(getContext(), launcher.name, dialogContent, new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(intentString));
                startActivity(intent);
            }
        });
    }

    public class Launcher {

        public final String name;
        public final String packageName;
        public final int launcherColor;
        private int isInstalled = -1;

        public Launcher(String[] values, int color) {
            name = values[0];
            packageName = values[1];
            launcherColor = color;
        }

        public boolean isInstalled(Context context) {
            if (isInstalled == -1) {
                if (packageName.equals("org.cyanogenmod.theme.chooser")) {
                    if (Utils.isAppInstalled(context, "org.cyanogenmod.theme.chooser")
                            || Utils.isAppInstalled(context, "com.cyngn.theme.chooser")) {
                        isInstalled = 1;
                    }
                } else {
                    isInstalled = Utils.isAppInstalled(context, packageName) ? 1 : 0;
                }
            }

            // Caches this value, checking if a launcher is installed is intensive on processing
            return isInstalled == 1;
        }

    }

    private void gnlDialog() {
        final String appLink = MARKET_URL + getResources().getString(R.string.extraapp);
        ISDialogs.showGoogleNowLauncherDialog(getContext(), new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(appLink));
                startActivity(intent);
            }
        });
    }

    private void showApplyAdviceDialog(Context dialogContext) {
        if (!mPrefs.getApplyDialogDismissed()) {
            MaterialDialog.SingleButtonCallback singleButtonCallback = new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    if (which.equals(DialogAction.POSITIVE)) {
                        mPrefs.setApplyDialogDismissed(false);
                    } else if (which.equals(DialogAction.NEUTRAL)) {
                        mPrefs.setApplyDialogDismissed(true);
                    }
                }
            };
            ISDialogs.showApplyAdviceDialog(dialogContext, singleButtonCallback);
        }
    }

}