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

package jahirfiquitiva.iconshowcase.models;

import java.util.ArrayList;

public class IconsLists {

    private ArrayList<IconItem> iconsArray = new ArrayList<>();

    public IconsLists(ArrayList<IconItem> iconsArray) {
        this.iconsArray = iconsArray;
    }

    public ArrayList<IconItem> getIconsArray() {
        return iconsArray.size() > 0 ? this.iconsArray : null;
    }

}