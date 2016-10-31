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

package jahirfiquitiva.iconshowcase.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.activities.ShowcaseActivity;
import jahirfiquitiva.iconshowcase.dialogs.ISDialogs;
import jahirfiquitiva.iconshowcase.models.RequestItem;
import jahirfiquitiva.iconshowcase.utilities.Preferences;
import jahirfiquitiva.iconshowcase.utilities.Utils;


public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestsHolder> {

    public interface ClickListener {

        void onClick(int index);
    }

    public final ArrayList<RequestItem> appsList;
    private final Context context;
    private final Preferences mPrefs;
    private final ClickListener mCallback;
    private AppIconFetchingQueue mAppIconFetchingQueue;

    public RequestsAdapter(final Context context, final ArrayList<RequestItem> appsList,
                           final Preferences mPrefs) {
        this.context = context;
        this.mPrefs = new Preferences(context);
        this.appsList = appsList;
        this.mCallback = new ClickListener() {
            @Override
            public void onClick(int position) {
                int limit = mPrefs.getRequestsLeft();
                if (limit < 0) {
                    changeAppSelectedState(position);
                } else {
                    if (context.getResources().getInteger(R.integer.limit_request_to_x_minutes) <= 0) {
                        changeAppSelectedState(position);
                    } else if (getSelectedApps() < limit) {
                        changeAppSelectedState(position);
                    } else {
                        if (isSelected(position)) {
                            changeAppSelectedState(position);
                        } else {
                            if (context.getResources().getInteger(R.integer.max_apps_to_request) > -1) {
                                if (Utils.canRequestXApps(context,
                                        context.getResources().getInteger(R.integer.limit_request_to_x_minutes),
                                        mPrefs) == -2) {
                                    ISDialogs.showRequestTimeLimitDialog(context,
                                            context.getResources().getInteger(R.integer.limit_request_to_x_minutes));
                                } else {
                                    ISDialogs.showRequestLimitDialog(context, limit);
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    @Override
    public RequestsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        boolean listsCards;
        if (context.getResources().getBoolean(R.bool.dev_options)) {
            listsCards = mPrefs.getDevListsCards();
        } else {
            listsCards = context.getResources().getBoolean(R.bool.request_cards);
        }
        View v = LayoutInflater.from(context).inflate(
                listsCards ?
                        R.layout.card_app_to_request :
                        R.layout.item_app_to_request, parent, false);
        return new RequestsHolder(v);
    }

    @Override
    public void onBindViewHolder(RequestsHolder holder, int position) {
        RequestItem requestsItem = appsList.get(position);
        holder.txtName.setText(requestsItem.getAppName());
        holder.imgIcon.setImageDrawable(requestsItem.getNormalIcon());
        holder.chkSelected.setChecked(requestsItem.isSelected());
        boolean listsCards;
        if (context.getResources().getBoolean(R.bool.dev_options)) {
            listsCards = mPrefs.getDevListsCards();
        } else {
            listsCards = context.getResources().getBoolean(R.bool.request_cards);
        }
        if (listsCards) {
            holder.cardView.setTag(position);
        } else {
            holder.view.setTag(position);
        }
    }

    @Override
    public int getItemCount() {
        return appsList == null ? 0 : appsList.size();
    }

    public class RequestsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        LinearLayout view = null;
        CardView cardView = null;
        final ImageView imgIcon;
        final TextView txtName;
        final CheckBox chkSelected;

        public RequestsHolder(View v) {
            super(v);
            imgIcon = (ImageView) v.findViewById(R.id.imgIcon);
            txtName = (TextView) v.findViewById(R.id.txtName);
            chkSelected = (CheckBox) v.findViewById(R.id.chkSelected);
            boolean listsCards;
            if (context.getResources().getBoolean(R.bool.dev_options)) {
                listsCards = mPrefs.getDevListsCards();
            } else {
                listsCards = context.getResources().getBoolean(R.bool.request_cards);
            }
            if (listsCards) {
                cardView = (CardView) v.findViewById(R.id.requestCard);
                cardView.setOnClickListener(this);
            } else {
                view = (LinearLayout) v.findViewById(R.id.requestCard);
                view.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            if (v.getTag() != null) {
                int index = (int) v.getTag();
                mCallback.onClick(index);
            }
        }

    }

    public void selectOrDeselectAll(boolean select, Preferences mPrefs) {

        boolean showDialog = false, showTimeLimitDialog = false;

        int limit = Utils.canRequestXApps(context,
                context.getResources().getInteger(R.integer.limit_request_to_x_minutes),
                mPrefs);

        if (limit >= -1) {
            for (int i = 0; i < appsList.size(); i++) {
                if (select) {
                    if (limit < 0) {
                        selectApp(i);
                    } else {
                        if (limit > 0) {
                            if (getSelectedApps() < limit) {
                                selectApp(i);
                            } else {
                                showDialog = true;
                                break;
                            }
                        }
                    }
                } else {
                    deselectApp(i);
                }
            }
        } else {
            showTimeLimitDialog = limit == -2;
        }

        if (showDialog) ISDialogs.showRequestLimitDialog(context, limit);

        if (showTimeLimitDialog) {
            ISDialogs.showRequestTimeLimitDialog(context,
                    context.getResources().getInteger(R.integer.limit_request_to_x_minutes));
            deselectAllApps();
            ShowcaseActivity.SELECT_ALL_APPS = false;
        }

    }

    private void selectApp(int position) {
        RequestItem requestsItem = appsList.get(position);
        if (!requestsItem.isSelected()) {
            requestsItem.setSelected(true);
            appsList.set(position, requestsItem);
            notifyItemChanged(position);
        }
    }

    private void deselectApp(int position) {
        RequestItem requestsItem = appsList.get(position);
        if (requestsItem.isSelected()) {
            requestsItem.setSelected(false);
            appsList.set(position, requestsItem);
            notifyItemChanged(position);
        }
    }

    public void deselectAllApps() {
        for (int i = 0; i < appsList.size(); i++) {
            RequestItem requestsItem = appsList.get(i);
            if (requestsItem.isSelected()) {
                requestsItem.setSelected(false);
                appsList.set(i, requestsItem);
                notifyItemChanged(i);
            }
        }
    }

    private void changeAppSelectedState(int position) {
        RequestItem requestsItem = appsList.get(position);
        requestsItem.setSelected(!requestsItem.isSelected());
        appsList.set(position, requestsItem);
        notifyItemChanged(position);
    }

    public int getSelectedApps() {
        int selected = 0;
        for (int i = 0; i < appsList.size(); i++) {
            if (appsList.get(i).isSelected()) {
                selected += 1;
            }
        }
        return selected;
    }

    private boolean isSelected(int i) {
        return appsList.get(i).isSelected();
    }

    public void startIconFetching(RecyclerView view) {
        mAppIconFetchingQueue = new AppIconFetchingQueue(view);
    }

    public void stopAppIconFetching() {
        if (mAppIconFetchingQueue != null) {
            mAppIconFetchingQueue.stop();
        }
    }

    public class AppIconFetchingQueue {

        int mIconsRemaining;
        final RecyclerView mRecyclerView;

        AppIconFetchingQueue(RecyclerView recyclerView) {
            mRecyclerView = recyclerView;
            mIconsRemaining = appsList != null ? appsList.size() : 0;
        }

        public void stop() {
            // Avoids calling stop on thread, which will cause crash.
            mIconsRemaining = 0;
        }

    }

}