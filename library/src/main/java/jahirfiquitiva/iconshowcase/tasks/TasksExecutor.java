/*
 * Copyright (c) 2016. Jahir Fiquitiva. Android Developer. All rights reserved.
 */

package jahirfiquitiva.iconshowcase.tasks;

import android.content.Context;
import android.os.AsyncTask;

import jahirfiquitiva.iconshowcase.activities.ShowcaseActivity;
import jahirfiquitiva.iconshowcase.fragments.WallpapersFragment;
import jahirfiquitiva.iconshowcase.models.WallpapersList;
import jahirfiquitiva.iconshowcase.utilities.Preferences;

public class TasksExecutor {

    // Context is always useful for some reason.
    private final Context context;

    // For storing & reading read data
    private final Preferences mPrefs;

    // Global singleton instance
    private static TasksExecutor singleton = null;

    private boolean justIcons, justWallpapers;

    public static TasksExecutor with(Context context) {
        if (singleton == null)
            singleton = new Builder(context).build();
        return singleton;
    }

    public void loadJust(boolean loadIcons, boolean loadWallpapers) {
        justIcons = loadIcons;
        justWallpapers = loadWallpapers;
    }

    protected static TasksExecutor getInstance() {
        return singleton;
    }

    public static void setSingleton(TasksExecutor singleton) {
        TasksExecutor.singleton = singleton;
    }

    TasksExecutor(Context context, boolean justIcons, boolean justWallpapers) {
        this.context = context;
        this.mPrefs = new Preferences(context);
        this.justIcons = justIcons;
        this.justWallpapers = justWallpapers;
        executeTasks();
    }

    private void executeTasks() {
        if (justIcons) {
            new LoadIconsLists(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else if (justWallpapers) {
            loadWallsList();
        } else {
            new LoadIconsLists(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new LoadZooperWidgets(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new LoadKustomFiles(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            loadAppsForRequest();
            loadWallsList();
        }
    }

    private void loadAppsForRequest() {
        if (mPrefs.getAppsToRequestLoaded()) {
            mPrefs.setAppsToRequestLoaded(!mPrefs.getAppsToRequestLoaded());
        }
        new LoadAppsToRequest(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void loadWallsList() {
        if (mPrefs.getWallsListLoaded()) {
            WallpapersList.clearList();
            mPrefs.setWallsListLoaded(!mPrefs.getWallsListLoaded());
        }
        new WallpapersFragment.DownloadJSON(new ShowcaseActivity.WallsListInterface() {
            @Override
            public void checkWallsListCreation(boolean result) {
                mPrefs.setWallsListLoaded(result);
                if (WallpapersFragment.mSwipeRefreshLayout != null) {
                    WallpapersFragment.mSwipeRefreshLayout.setEnabled(false);
                    WallpapersFragment.mSwipeRefreshLayout.setRefreshing(false);
                }
                if (WallpapersFragment.mAdapter != null) {
                    WallpapersFragment.mAdapter.notifyDataSetChanged();
                }
            }
        }, context, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Fluent API for creating {@link TasksExecutor} instances.
     */
    public static class Builder {

        private final Context context;
        private boolean justIcons, justWallpapers;

        /**
         * Start building a new {@link TasksExecutor} instance.
         */
        public Builder(Context context) {
            if (context == null)
                throw new IllegalArgumentException("Context must not be null!");

            this.context = context.getApplicationContext();
        }

        public Builder justIcons(boolean justIcons) {
            this.justIcons = justIcons;
            return this;
        }

        public Builder justWallpapers(boolean justWallpapers) {
            this.justWallpapers = justWallpapers;
            return this;
        }

        /**
         * Creates a {@link TasksExecutor} instance.
         */
        public TasksExecutor build() {
            return new TasksExecutor(context, justIcons, justWallpapers);
        }
    }

}
