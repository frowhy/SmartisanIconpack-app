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

package jahirfiquitiva.iconshowcase.utilities.sort;

import android.content.Context;

import java.util.Comparator;

import jahirfiquitiva.iconshowcase.fragments.ApplyFragment;

/**
 * @author Aidan Follestad (afollestad)
 */
public class InstalledLauncherComparator implements Comparator<ApplyFragment.Launcher> {

    private final Context context;

    public InstalledLauncherComparator(Context context) {
        this.context = context;
    }

    @Override
    public int compare(ApplyFragment.Launcher lhs, ApplyFragment.Launcher rhs) {
        if (!lhs.isInstalled(context) && rhs.isInstalled(context)) {
            // Left is not installed, right is, push left down towards the bottom.
            return 1;
        } else if (lhs.isInstalled(context) && !rhs.isInstalled(context)) {
            // Left is installed, right isn't, pull left up towards the top.
            return -1;
        } else {
            // Sort alphabetically if they're at the same position.
            return lhs.name.compareTo(rhs.name);
        }
    }
}
