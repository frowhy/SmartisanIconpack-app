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

package jahirfiquitiva.iconshowcase.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.util.ArrayList;
import java.util.List;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.adapters.FAQsAdapter;
import jahirfiquitiva.iconshowcase.models.FAQsItem;
import jahirfiquitiva.iconshowcase.utilities.Preferences;
import jahirfiquitiva.iconshowcase.utilities.Utils;
import jahirfiquitiva.iconshowcase.views.DividerItemDecoration;
import jahirfiquitiva.iconshowcase.views.GridSpacingItemDecoration;

public class FAQsFragment extends Fragment {

    private ViewGroup layout;
    private Context context;
    private Preferences mPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        context = getActivity();
        mPrefs = new Preferences(context);

        if (layout != null) {
            ViewGroup parent = (ViewGroup) layout.getParent();
            if (parent != null) {
                parent.removeView(layout);
            }
        }
        try {
            layout = (ViewGroup) inflater.inflate(R.layout.faqs_section, container, false);
        } catch (InflateException e) {
            //Do nothing
        }

        String[] questions = getResources().getStringArray(R.array.questions);
        String[] answers = getResources().getStringArray(R.array.answers);

        List<FAQsItem> faqs = new ArrayList<>();
        for (int i = 0; i < questions.length; i++) {
            FAQsItem item = new FAQsItem(questions[i], answers[i]);
            faqs.add(item);
        }

        int cardsSpacing = getResources().getDimensionPixelSize(R.dimen.dividers_height);

        RecyclerView faqsList = (RecyclerView) layout.findViewById(R.id.faqs_list);

        FAQsAdapter faqsAdapter = new FAQsAdapter(faqs, getActivity());

        boolean listsCards;

        if (context.getResources().getBoolean(R.bool.dev_options)) {
            listsCards = mPrefs.getDevListsCards();
        } else {
            listsCards = context.getResources().getBoolean(R.bool.faqs_cards);
        }

        if (listsCards) {
            faqsList.setLayoutManager(new GridLayoutManager(getActivity(), 1));
            faqsList.addItemDecoration(new GridSpacingItemDecoration(1,
                    getActivity().getResources().getDimensionPixelSize(R.dimen.cards_margin),
                    true));
        } else {
            faqsList.setLayoutManager(new LinearLayoutManager(getActivity()));
            faqsList.addItemDecoration(new DividerItemDecoration(getActivity(), null, cardsSpacing, false, false));
        }

        faqsList.setItemAnimator(new DefaultItemAnimator());
        faqsList.setHasFixedSize(true);
        faqsList.setAdapter(faqsAdapter);

        RecyclerFastScroller fastScroller = (RecyclerFastScroller) layout.findViewById(R.id.rvFastScroller);
        fastScroller.attachRecyclerView(faqsList);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.collapseToolbar(getActivity());
    }

}