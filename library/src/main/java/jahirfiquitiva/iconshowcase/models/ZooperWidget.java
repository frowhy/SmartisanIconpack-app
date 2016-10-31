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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import jahirfiquitiva.iconshowcase.utilities.Utils;

public class ZooperWidget {

    private String previewPath;

    public ZooperWidget(String previewPath) {
        this.previewPath = previewPath;
    }

    public String getPreviewPath() {
        return this.previewPath;
    }

    public static Bitmap getTransparentBackgroundPreview(Bitmap original) {
        return Utils.getBitmapWithReplacedColor(original, Color.parseColor("#555555"), Color.TRANSPARENT);
    }

}