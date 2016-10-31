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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.util.ArrayList;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.adapters.RequestsAdapter;
import jahirfiquitiva.iconshowcase.dialogs.ISDialogs;
import jahirfiquitiva.iconshowcase.models.RequestItem;
import jahirfiquitiva.iconshowcase.models.RequestList;
import jahirfiquitiva.iconshowcase.tasks.ZipFilesToRequest;
import jahirfiquitiva.iconshowcase.utilities.PermissionUtils;
import jahirfiquitiva.iconshowcase.utilities.Preferences;
import jahirfiquitiva.iconshowcase.utilities.Utils;
import jahirfiquitiva.iconshowcase.views.GridSpacingItemDecoration;


public class RequestsFragment extends Fragment implements PermissionUtils.OnPermissionResultListener {

    private static ProgressBar progressBar;
    public static RecyclerView mRecyclerView;
    private static RecyclerFastScroller fastScroller;
    public static RequestsAdapter requestsAdapter;
    private static FloatingActionButton fab;
    private static int maxApps = 0, minutesLimit = 0;
    public static ViewGroup layout;
    private static Preferences mPrefs;
    private Activity context;
    private static ArrayList<RequestItem> requestList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        int gridSpacing = getResources().getDimensionPixelSize(R.dimen.lists_padding);
        int columnsNumber = getResources().getInteger(R.integer.requests_grid_width);

        minutesLimit = getResources().getInteger(R.integer.limit_request_to_x_minutes);

        setHasOptionsMenu(true);
        context = getActivity();

        mPrefs = new Preferences(context);

        setupMaxApps();

        if (layout != null) {
            ViewGroup parent = (ViewGroup) layout.getParent();
            if (parent != null) {
                parent.removeView(layout);
            }
        }

        try {
            layout = (ViewGroup) inflater.inflate(R.layout.icon_request_section, container, false);
        } catch (InflateException e) {
            // Do nothing
        }

        fab = (FloatingActionButton) layout.findViewById(R.id.requests_fab);

        requestList = RequestList.getRequestList();

        if (requestList == null || requestList.size() <= 0) {
            fab.hide();
        } else {
            fab.show();
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PermissionUtils.canAccessStorage(getContext())) {
                    PermissionUtils.requestStoragePermission(getActivity(), RequestsFragment.this);
                } else {
                    startRequestProcess();
                }
            }
        });

        progressBar = (ProgressBar) layout.findViewById(R.id.requestProgress);
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.appsToRequestList);
        mRecyclerView.setLayoutManager(new GridLayoutManager(context, columnsNumber));
        mRecyclerView.addItemDecoration(
                new GridSpacingItemDecoration(columnsNumber,
                        gridSpacing,
                        true));
        fastScroller = (RecyclerFastScroller) layout.findViewById(R.id.rvFastScroller);
        hideStuff();

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    fab.hide();
                } else {
                    fab.show();
                }
            }
        });

        return layout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupContent(view, getActivity());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.requests, menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.collapseToolbar(getActivity());
        setupMaxApps();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RequestsAdapter adapter = ((RequestsAdapter) mRecyclerView.getAdapter());
        if (adapter != null) {
            adapter.stopAppIconFetching();
        }
    }

    public static void setupContent(View layout, Context context) {
        if (layout != null) {
            requestList = RequestList.getRequestList();
            if (requestList != null && requestList.size() > 0) {
                requestsAdapter = new RequestsAdapter(context, requestList, mPrefs);
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setItemAnimator(null);
                mRecyclerView.setAnimation(null);
                mRecyclerView.setAdapter(requestsAdapter);
                requestsAdapter.startIconFetching(mRecyclerView);
                fastScroller.attachRecyclerView(mRecyclerView);
                showStuff();
            }
        }
    }

    private static void showStuff() {
        if (progressBar.getVisibility() != View.GONE) {
            progressBar.setVisibility(View.GONE);
        }
        mRecyclerView.setVisibility(View.VISIBLE);
        fastScroller.setVisibility(View.VISIBLE);
        fab.show();
    }

    private void hideStuff() {
        if (progressBar.getVisibility() != View.VISIBLE) {
            progressBar.setVisibility(View.VISIBLE);
        }
        mRecyclerView.setVisibility(View.GONE);
        fastScroller.setVisibility(View.GONE);
    }

    private void showRequestsFilesCreationDialog(Context context) {

        if (requestsAdapter.getSelectedApps() > 0) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) {

                ISDialogs.showPermissionNotGrantedDialog(context);

            } else {
                if (getResources().getInteger(R.integer.max_apps_to_request) > -1) {
                    if (maxApps < 0) {
                        maxApps = 0;
                    }
                    if (requestsAdapter.getSelectedApps() <= mPrefs.getRequestsLeft()) {
                        final MaterialDialog dialog = ISDialogs.showBuildingRequestDialog(context);
                        dialog.show();

                        new ZipFilesToRequest((Activity) context, dialog,
                                ((RequestsAdapter) mRecyclerView.getAdapter()).appsList).execute();
                    } else {
                        ISDialogs.showRequestLimitDialog(context, maxApps);
                    }
                } else {
                    final MaterialDialog dialog = ISDialogs.showBuildingRequestDialog(context);
                    dialog.show();

                    new ZipFilesToRequest((Activity) context, dialog,
                            ((RequestsAdapter) mRecyclerView.getAdapter()).appsList).execute();
                }
            }
        } else {
            ISDialogs.showNoSelectedAppsDialog(context);
        }

    }

    @Override
    public void onStoragePermissionGranted() {
        showRequestsFilesCreationDialog(context);
    }

    private void startRequestProcess() {
        if (getResources().getInteger(R.integer.max_apps_to_request) > -1) {
            if (mPrefs.getRequestsLeft() <= 0) {
                if (requestsAdapter.getSelectedApps() < mPrefs.getRequestsLeft()) {
                    showRequestsFilesCreationDialog(context);
                } else if ((Utils.canRequestXApps(context, minutesLimit, mPrefs) != -2)
                        || (minutesLimit <= 0)) {
                    showRequestsFilesCreationDialog(context);
                } else {
                    ISDialogs.showRequestTimeLimitDialog(context, minutesLimit);
                }
            } else {
                showRequestsFilesCreationDialog(context);
            }
        } else {
            showRequestsFilesCreationDialog(context);
        }
    }

    private void setupMaxApps() {
        if (!mPrefs.getRequestsCreated()) {
            mPrefs.setRequestsLeft(context.getResources().getInteger(R.integer.max_apps_to_request));
        }
        maxApps = mPrefs.getRequestsLeft();
    }

}