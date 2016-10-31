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

package jahirfiquitiva.iconshowcase.fragments.base;

//package android.support.v4.preference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * A PreferenceFragment for the support library. Based on the platform's code with some removed features and a basic ListView layout.
 *
 * @author Christophe Beyls
 */

public abstract class PreferenceFragment extends Fragment {

    private static final int FIRST_REQUEST_CODE = 100;
    private static final int MSG_BIND_PREFERENCES = 1;
    private static final int MSG_REQUEST_FOCUS = 2;
    private static final String PREFERENCES_TAG = "android:preferences";
    private static double HC_HORIZONTAL_PADDING = 0.8; //5.33

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_BIND_PREFERENCES:
                    bindPreferences();
                    break;
                case MSG_REQUEST_FOCUS:
                    mList.focusableViewAvailable(mList);
                    break;
            }
        }
    };

    private boolean mHavePrefs;
    private boolean mInitDone;
    private ListView mList;
    private PreferenceManager mPreferenceManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Constructor<PreferenceManager> c = PreferenceManager.class.getDeclaredConstructor(Activity.class, int.class);
            c.setAccessible(true);
            mPreferenceManager = c.newInstance(this.getActivity(), FIRST_REQUEST_CODE);
        } catch (Exception ignored) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        ListView listView = new ListView(getActivity());
        listView.setId(android.R.id.list);
        listView.setDividerHeight(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
                HC_HORIZONTAL_PADDING = 5.33;
            }
            final int horizontalPadding = (int) (HC_HORIZONTAL_PADDING * getResources().getDisplayMetrics().density);
            listView.setPadding(horizontalPadding, 0, horizontalPadding, 0);
        }
        return listView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mHavePrefs) {
            bindPreferences();
        }

        mInitDone = true;

        if (savedInstanceState != null) {
            Bundle container = savedInstanceState.getBundle(PREFERENCES_TAG);
            if (container != null) {
                final PreferenceScreen preferenceScreen = getPreferenceScreen();
                if (preferenceScreen != null) {
                    preferenceScreen.restoreHierarchyState(container);
                }
            }
        }
    }

    public void onStop() {
        super.onStop();
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("dispatchActivityStop");
            m.setAccessible(true);
            m.invoke(mPreferenceManager);
        } catch (Exception ignored) {
        }
    }

    public void onDestroyView() {
        mList = null;
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }

    public void onDestroy() {
        super.onDestroy();
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("dispatchActivityDestroy");
            m.setAccessible(true);
            m.invoke(mPreferenceManager);
        } catch (Exception ignored) {
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            Bundle container = new Bundle();
            preferenceScreen.saveHierarchyState(container);
            outState.putBundle(PREFERENCES_TAG, container);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("dispatchActivityResult", int.class, int.class, Intent.class);
            m.setAccessible(true);
            m.invoke(mPreferenceManager, requestCode, resultCode, data);
        } catch (Exception ignored) {
        }
    }

    public PreferenceManager getPreferenceManager() {
        return mPreferenceManager;
    }

    public void setPreferenceScreen(PreferenceScreen screen) {
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("setPreferences", PreferenceScreen.class);
            m.setAccessible(true);
            boolean result = (Boolean) m.invoke(mPreferenceManager, screen);
            if (result && (screen != null)) {
                mHavePrefs = true;
                if (mInitDone) {
                    postBindPreferences();
                }
            }
        } catch (Exception ignored) {
        }
    }

    public PreferenceScreen getPreferenceScreen() {
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("getPreferenceScreen");
            m.setAccessible(true);
            return (PreferenceScreen) m.invoke(mPreferenceManager);
        } catch (Exception e) {
            return null;
        }
    }

    public void addPreferencesFromIntent(Intent intent) {
        requirePreferenceManager();
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("inflateFromIntent", Intent.class, PreferenceScreen.class);
            m.setAccessible(true);
            PreferenceScreen screen = (PreferenceScreen) m.invoke(mPreferenceManager, intent, getPreferenceScreen());
            setPreferenceScreen(screen);
        } catch (Exception ignored) {
        }
    }

    public void addPreferencesFromResource(int resId) {
        requirePreferenceManager();
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("inflateFromResource", Context.class, int.class, PreferenceScreen.class);
            m.setAccessible(true);
            PreferenceScreen screen = (PreferenceScreen) m.invoke(mPreferenceManager, getActivity(), resId, getPreferenceScreen());
            setPreferenceScreen(screen);
        } catch (Exception ignored) {
        }
    }

    public Preference findPreference(CharSequence key) {
        if (mPreferenceManager == null) {
            return null;
        }
        return mPreferenceManager.findPreference(key);
    }

    private void requirePreferenceManager() {
        if (this.mPreferenceManager == null) {
            throw new RuntimeException("This should be called after super.onCreate.");
        }
    }

    private void postBindPreferences() {
        if (!mHandler.hasMessages(MSG_BIND_PREFERENCES)) {
            mHandler.sendEmptyMessage(MSG_BIND_PREFERENCES);
        }
    }

    private void bindPreferences() {
        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            preferenceScreen.bind(getListView());
        }
    }

    public ListView getListView() {
        ensureList();
        return mList;
    }

    private void ensureList() {
        if (mList != null) {
            return;
        }
        View layout = getView();
        if (layout == null) {
            throw new IllegalStateException("Content view not yet created");
        }
        View rawListView = layout.findViewById(android.R.id.list);
        if (rawListView == null) {
            throw new RuntimeException("Your content must have a ListView whose id attribute is 'android.R.id.list'");
        }
        if (!(rawListView instanceof ListView)) {
            throw new RuntimeException("Content has view with id attribute 'android.R.id.list' that is not a ListView class");
        }
        mList = (ListView) rawListView;
        mHandler.sendEmptyMessage(MSG_REQUEST_FOCUS);
    }
}