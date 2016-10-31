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

package jahirfiquitiva.iconshowcase.models;

public class AppFilterError {

    private final boolean emptyComponent, halfEmptyPackage, halfEmptyComponent;
    private final String iconName, completeComponent;
    private final int iconID;

    public AppFilterError(boolean emptyComponent, boolean halfEmptyPackage,
                          boolean halfEmptyComponent, String iconName, String completeComponent,
                          int iconID) {
        this.emptyComponent = emptyComponent;
        this.halfEmptyPackage = halfEmptyPackage;
        this.halfEmptyComponent = halfEmptyComponent;
        this.iconName = iconName;
        this.completeComponent = completeComponent;
        this.iconID = iconID;
    }

    public boolean hasEmptyComponent() {
        return emptyComponent;
    }

    public boolean hasHalfEmptyPackage() {
        return halfEmptyPackage;
    }

    public boolean hasHalfEmptyComponent() {
        return halfEmptyComponent;
    }

    public String getIconName() {
        return iconName;
    }

    public String getCompleteComponent() {
        return completeComponent;
    }

    public int getIconID() {
        return iconID;
    }

}