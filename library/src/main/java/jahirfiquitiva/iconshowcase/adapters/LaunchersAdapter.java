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

package jahirfiquitiva.iconshowcase.adapters;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.fragments.ApplyFragment;
import jahirfiquitiva.iconshowcase.utilities.ThemeUtils;
import jahirfiquitiva.iconshowcase.utilities.Utils;

public class LaunchersAdapter extends RecyclerView.Adapter<LaunchersAdapter.LauncherHolder> implements View.OnClickListener {

    public interface ClickListener {

        void onClick(int index);
    }

    private final Context context;
    private final List<ApplyFragment.Launcher> launchers;
    private final ClickListener mCallback;

    public LaunchersAdapter(Context context, List<ApplyFragment.Launcher> launchers, ClickListener callback) {
        this.context = context;
        this.launchers = launchers;
        this.mCallback = callback;
    }

    @Override
    public LauncherHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new LauncherHolder(inflater.inflate(R.layout.item_launcher, parent, false));
    }

    @Override
    public void onBindViewHolder(LauncherHolder holder, int position) {
        // Turns Launcher name "Something Pro" to "ic_something_pro"
        String iconName = "ic_" + launchers.get(position).name.toLowerCase().replace(" ", "_");
        int iconResource = Utils.getIconResId(context, context.getResources(),
                context.getPackageName(), iconName, "Launchers");

        final int dark = ContextCompat.getColor(context, R.color.launcher_tint_dark);
        final int light = ContextCompat.getColor(context, R.color.launcher_tint_light);
        final int textDark = ContextCompat.getColor(context, R.color.launcher_text_light);
        final int textLight = ContextCompat.getColor(context, R.color.launcher_text_dark);

        if (iconResource != 0) {
            Glide.with(context)
                    .load(iconResource)
                    .dontAnimate()
                    .into(holder.icon);
        }

        holder.launcherName.setText(launchers.get(position).name.toUpperCase(Locale.getDefault()));

        if (launchers.get(position).isInstalled(context)) {
            holder.icon.setColorFilter(null);
            holder.itemBG.setBackgroundColor(launchers.get(position).launcherColor);
            holder.launcherName.setTextColor(textLight);
        } else {
            holder.icon.setColorFilter(bnwFilter());
            holder.itemBG.setBackgroundColor(ThemeUtils.darkTheme ? dark : light);
            holder.launcherName.setTextColor(ThemeUtils.darkTheme ? textDark : textLight);
        }

        holder.view.setTag(position);
        holder.view.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return launchers == null ? 0 : launchers.size();
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() != null) {
            int index = (int) v.getTag();
            if (mCallback != null)
                mCallback.onClick(index);
        }
    }

    class LauncherHolder extends RecyclerView.ViewHolder {

        final View view;
        final ImageView icon;
        final TextView launcherName;
        final LinearLayout itemBG;

        LauncherHolder(View v) {
            super(v);
            view = v;
            itemBG = (LinearLayout) view.findViewById(R.id.itemBG);
            icon = (ImageView) view.findViewById(R.id.launcherIcon);
            launcherName = (TextView) view.findViewById(R.id.launcherName);
        }
    }

    private ColorFilter bnwFilter() {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        return new ColorMatrixColorFilter(matrix);
    }

}