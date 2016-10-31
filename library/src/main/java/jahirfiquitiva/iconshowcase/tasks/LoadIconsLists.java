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

package jahirfiquitiva.iconshowcase.tasks;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.models.IconItem;
import jahirfiquitiva.iconshowcase.models.IconsCategory;
import jahirfiquitiva.iconshowcase.models.IconsLists;
import jahirfiquitiva.iconshowcase.utilities.Utils;

public class LoadIconsLists extends AsyncTask<Void, String, Boolean> {

    private final WeakReference<Context> context;
    private static ArrayList<IconsLists> iconsLists;
    private static ArrayList<IconsCategory> categories;
    private long startTime;
    private static final String TASK = "IconsLists";

    public LoadIconsLists(Context context) {
        this.context = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        startTime = System.currentTimeMillis();
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        boolean worked = false;

        Resources r = context.get().getResources();
        String p = context.get().getPackageName();

        int iconResId = 0;

        iconsLists = new ArrayList<>();

        String[] newIcons = r.getStringArray(R.array.changelog_icons);
        List<String> newIconsL = sortList(newIcons);

        ArrayList<IconItem> changelogIconsArray = new ArrayList<>();
        for (String icon : newIconsL) {
            iconResId = Utils.getIconResId(context.get(), r, p, icon, TASK);
            if (iconResId != 0) {
                changelogIconsArray.add(new IconItem(icon, iconResId));
            }
        }
        iconsLists.add(new IconsLists(changelogIconsArray));

        String[] prev = r.getStringArray(R.array.preview);
        List<String> previewIconsL = sortList(prev);

        ArrayList<IconItem> previewIconsArray = new ArrayList<>();
        for (String icon : previewIconsL) {
            iconResId = Utils.getIconResId(context.get(), r, p, icon, TASK);
            if (iconResId != 0) {
                previewIconsArray.add(new IconItem(icon, iconResId));
            }
        }
        iconsLists.add(new IconsLists(previewIconsArray));

        String[] tabsNames = r.getStringArray(R.array.tabs);
        categories = new ArrayList<>();
        ArrayList<IconItem> allIcons = new ArrayList<>();

        for (String tabName : tabsNames) {

            int arrayId = r.getIdentifier(tabName, "array", p);
            String[] icons = null;

            try {
                icons = r.getStringArray(arrayId);
            } catch (Resources.NotFoundException e) {
                Utils.showLog(context.get(), "Couldn't find array: " + tabName);
            }

            if (icons != null && icons.length > 0) {

                List<String> iconsList = sortList(icons);

                ArrayList<IconItem> iconsArray = new ArrayList<>();

                for (int j = 0; j < iconsList.size(); j++) {
                    iconResId = Utils.getIconResId(context.get(), r, p, iconsList.get(j), TASK);
                    if (iconResId != 0) {
                        iconsArray.add(new IconItem(iconsList.get(j), iconResId));
                        if (context.get().getResources().getBoolean(R.bool.auto_generate_all_icons)) {
                            allIcons.add(new IconItem(iconsList.get(j), iconResId));
                        }
                    }
                }

                categories.add(new IconsCategory(Utils.makeTextReadable(tabName), iconsArray));
                worked = true;
            } else {
                worked = false;
            }
        }

        if (context.get().getResources().getBoolean(R.bool.auto_generate_all_icons)) {
            ArrayList<IconItem> allTheIcons = getAllIconsList(r, p, allIcons);
            if (allTheIcons.size() > 0) {
                categories.add(new IconsCategory("All", allTheIcons));
                worked = true;
            } else {
                worked = false;
            }
        } else {
            String[] allIconsArray = r.getStringArray(R.array.icon_pack);
            if (allIconsArray.length > 0) {
                categories.add(new IconsCategory("All", sortAndOrganizeList(r, p, allIconsArray)));
                worked = true;
            } else {
                worked = false;
            }
        }

        return worked;
    }

    @Override
    protected void onPostExecute(Boolean worked) {
        long endTime = System.currentTimeMillis();
        if (worked) {
            Utils.showLog(context.get(),
                    "Load of icons task completed successfully in: " +
                            String.valueOf((endTime - startTime)) + " millisecs.");
        }
        /* TODO Check if it's secure enough
        if (categories.get(categories.size() - 1).getCategoryName().equals("All")) {
            ShowcaseActivity.SHOW_LOAD_ICONS_DIALOG = false;
            if (ShowcaseActivity.loadIcons != null) {
                ShowcaseActivity.loadIcons.dismiss();
            }
        }
        */
    }

    private List<String> sortList(String[] array) {
        List<String> list = new ArrayList<>(Arrays.asList(array));
        Collections.sort(list);
        return list;
    }

    private ArrayList<IconItem> sortAndOrganizeList(Resources r, String p, String[] array) {

        List<String> list = new ArrayList<>(Arrays.asList(array));
        Collections.sort(list);

        /*
        Set<String> noDuplicates = new HashSet<>();
        noDuplicates.addAll(list);
        list.clear();
        list.addAll(noDuplicates);
        Collections.sort(list);
        */

        ArrayList<IconItem> sortedListArray = new ArrayList<>();

        int resId;

        for (int j = 0; j < list.size(); j++) {
            resId = Utils.getIconResId(context.get(), r, p, list.get(j), TASK);
            if (resId != 0) {
                sortedListArray.add(new IconItem(list.get(j), resId));
            }
        }

        return sortedListArray;
    }

    private ArrayList<IconItem> getAllIconsList(Resources r, String p,
                                                ArrayList<IconItem> initialList) {

        String[] allIconsNames = new String[initialList.size()];

        for (int i = 0; i < initialList.size(); i++) {
            allIconsNames[i] = initialList.get(i).getName();
        }

        List<String> list = new ArrayList<>(Arrays.asList(allIconsNames));
        Collections.sort(list);

        Set<String> noDuplicates = new HashSet<>();
        noDuplicates.addAll(list);
        list.clear();
        list.addAll(noDuplicates);
        Collections.sort(list);

        ArrayList<IconItem> sortedListArray = new ArrayList<>();

        int resId;

        for (int j = 0; j < list.size(); j++) {
            resId = Utils.getIconResId(context.get(), r, p, list.get(j), TASK);
            if (resId != 0) {
                sortedListArray.add(new IconItem(list.get(j), resId));
            }
        }

        return sortedListArray;
    }

    public static ArrayList<IconsLists> getIconsLists() {
        return iconsLists.size() > 0 ? iconsLists : null;
    }

    public static ArrayList<IconsCategory> getIconsCategories() {
        return categories.size() > 0 ? categories : null;
    }

}