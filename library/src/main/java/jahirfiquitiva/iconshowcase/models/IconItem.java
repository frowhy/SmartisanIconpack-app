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

public class IconItem implements Parcelable {

    private final String name;
    private final int resId;

    public IconItem(String name, int resId) {
        this.name = name;
        this.resId = resId;
    }

    public String getName() {
        return this.name;
    }

    public int getResId() {
        return this.resId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(resId);
    }

    public static final Creator<IconItem> CREATOR = new Creator<IconItem>() {
        @Override
        public IconItem createFromParcel(Parcel in) {
            String name = in.readString();
            int redId = in.readInt();
            return new IconItem(name, redId);
        }

        @Override
        public IconItem[] newArray(int size) {
            return new IconItem[size];
        }
    };
}