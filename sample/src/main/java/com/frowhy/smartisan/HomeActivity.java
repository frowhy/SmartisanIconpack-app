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

package com.frowhy.smartisan;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.frowhy.smartisan.BuildConfig;
import jahirfiquitiva.iconshowcase.utilities.Utils;


public class HomeActivity extends AppCompatActivity {

    private static final boolean
            ENABLE_DONATIONS = true,
            ENABLE_GOOGLE_DONATIONS = false,
            ENABLE_PAYPAL_DONATIONS = true,
            ENABLE_FLATTR_DONATIONS = false,
            ENABLE_BITCOIN_DONATIONS = false,
            ENABLE_LICENSE_CHECK = false,
            ENABLE_AMAZON_INSTALLS = false;

    private static final String GOOGLE_PUBLISHER_KEY = "insert_key_here";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int notifType = getIntent().getIntExtra("notifType", 2);

        Intent intent = new Intent(HomeActivity.this, jahirfiquitiva.iconshowcase.activities.ShowcaseActivity.class);

        intent.putExtra("installer", getAppInstaller());

        intent.putExtra("launchNotifType", notifType);

        intent.putExtra("curVersionCode", getAppCurrentVersionCode());

        intent.putExtra("enableDonations", ENABLE_DONATIONS);
        intent.putExtra("enableGoogleDonations", ENABLE_GOOGLE_DONATIONS);
        intent.putExtra("enablePayPalDonations", ENABLE_PAYPAL_DONATIONS);
        intent.putExtra("enableFlattrDonations", ENABLE_FLATTR_DONATIONS);
        intent.putExtra("enableBitcoinDonations", ENABLE_BITCOIN_DONATIONS);

        intent.putExtra("enableLicenseCheck", (ENABLE_LICENSE_CHECK && !BuildConfig.DEBUG));
        intent.putExtra("enableAmazonInstalls", ENABLE_AMAZON_INSTALLS);

        intent.putExtra("googlePubKey", GOOGLE_PUBLISHER_KEY);

        startActivity(intent);

        finish();

    }

    private String getAppInstaller() {
        return getPackageManager().getInstallerPackageName(getPackageName());
    }

    private int getAppCurrentVersionCode() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Utils.showLog(this, "Unable to get version code. Reason: " + e.getLocalizedMessage());
            return -1;
        }
    }

}