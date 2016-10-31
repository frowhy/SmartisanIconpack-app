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

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class IconsCategory implements Parcelable {

    private final String name;
    private ArrayList<IconItem> iconsArray = new ArrayList<>();

    public IconsCategory(String name, ArrayList<IconItem> iconsArray) {
        this.name = name;
        this.iconsArray = iconsArray;
    }

    public static final Creator<IconsCategory> CREATOR = new Creator<IconsCategory>() {
        @Override
        public IconsCategory createFromParcel(Parcel in) {
            String name = in.readString();
            ArrayList<IconItem> icons = new ArrayList<>();
            in.readTypedList(icons, IconItem.CREATOR);
            return new IconsCategory(name, icons);
        }

        @Override
        public IconsCategory[] newArray(int size) {
            return new IconsCategory[size];
        }
    };

    public String getCategoryName() {
        return this.name;
    }

    public ArrayList<IconItem> getIconsArray() {
        return iconsArray.size() > 0 ? this.iconsArray : null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeTypedList(iconsArray);
    }
}