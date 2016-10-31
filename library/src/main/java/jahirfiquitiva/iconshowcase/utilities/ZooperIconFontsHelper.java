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

package jahirfiquitiva.iconshowcase.utilities;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Created BY: Jonathan Merritt.
 * <p/>
 * IconFont Fix For Zooper Widget Pro.
 **/

public class ZooperIconFontsHelper {

    public static final int CHECKING = 0;
    public static final int FIXING = 1;
    public static final int CANCELING = 2;

    public interface Callback {

        /**
         * OnSaved(File file);
         * Called for each IconFont saved.
         *
         * @param file <description>
         *             File for the saved IconFont. />
         **/

        void OnSaved(File file);

        /**
         * OnFinished(int state, int size);
         * Called for each finish.
         *
         * @param state <description>
         *              CHECKING - Finished checking for missing IconFonts.
         *              FIXING - Finished fixing any missing IconFonts.
         *              CANCELING - Finished canceling and deleting fixed IconFonts. />
         * @param size  <description>
         *              Total amount of missing/fixed or deleted IconFonts. />
         **/

        void OnFinished(int state, int size);

        /**
         * OnError(Throwable error);
         * Called for any errors thrown during checking/fixing or canceling.
         *
         * @param throwable <description>
         *                  Throwable for the error or exception. />
         **/

        void OnError(Throwable throwable);

        /**
         * OnDeleted(String name);
         * Called for each deleted IconFont.
         *
         * @param name <description>
         *             Name of the last deleted IconFont. />
         **/

        void OnDeleted(String name);
    }

    private Callback mCallback;

    /**
     * setCallback(Callback callback);
     * Sets the (Callback)Interface for the Helper.
     *
     * @param callback <description>
     *                 (Callback)Interface for all check/fix or cancel updates. />
     **/

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    /**
     * init(Context context);
     * Initiates a new ZooperIconFontsHelper.
     *
     * @param context <description>
     *                Any Context should do, if it is implementing #Callback
     *                it will automatically setCallback(context).
     *                Otherwise you need to setCallback(callback) yourself to get updates./>
     **/

    public static ZooperIconFontsHelper with(Context context) {
        return new ZooperIconFontsHelper(context);
    }

    private final AssetManager mAssetManager;

    public ZooperIconFontsHelper(Context context) {
        this.mAssetManager = context.getAssets();
        if (context instanceof Callback) this.setCallback((Callback) context);
    }

    private boolean mCheckAndFix = false;

    /**
     * check(boolean andFix);
     * Starts Sub To Check For Any Missing IconFonts.
     *
     * @param andFix <description>
     *               false - only checks.
     *               true - starts fix after checks, if needed. />
     **/

    public void check(boolean andFix) {
        this.mCheckAndFix = andFix;
        this.subscription(CHECKING);
    }

    /**
     * fix();
     * Starts Sub To Fix Any Missing IconFonts.
     **/

    public void fix() {
        this.subscription(FIXING);
    }

    /**
     * cancel();
     * Starts Sub To Cancel & Delete Any Fixed IconFonts.
     **/

    public void cancel() {
        this.subscription(CANCELING);
    }

    /************************************************************************/

    private List<String> mMissing = null;
    private List<String> mIconsets = null;
    private List<File> mFixed = null;
    private int mFixIndex = 0;
    private int mCancelIndex = 0;

    private Subscription mSubscription = null;
    private Subscription subscription(final int state) {
        if (state == FIXING)
            try {
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                    throw new IOException("External Media is not mounted!");
            } catch (IOException exception) {
                if (this.mCallback != null) this.mCallback.OnError(exception);
            }

        Observable<?> o = Observable.create(this.onSubscribe(state))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());

        if (state != CHECKING)
            o.take(state == FIXING ? this.mMissing.size() : this.mFixed.size() + 1);

        if (this.mSubscription != null) {
            this.mSubscription.unsubscribe();
            this.mSubscription = null;
        }

        return this.mSubscription = o.subscribe(new Observer<Object>() {
            final ZooperIconFontsHelper c = ZooperIconFontsHelper.this;

            @Override
            public void onCompleted() {
                int s;
                if (state != CHECKING)
                    s = state == FIXING ? c.mFixIndex : c.mCancelIndex - 1;
                else s = c.mMissing.size();
                if (c.mCheckAndFix && s > 0) c.fix();
                if (c.mCallback != null) c.mCallback.OnFinished(state, s);
            }

            @Override
            public void onError(Throwable throwable) {
                if (c.mCallback != null) c.mCallback.OnError(throwable);
            }

            @Override
            public void onNext(Object object) {
                if (object instanceof File) {
                    if (c.mCallback != null) c.mCallback.OnSaved((File) object);
                } else if (object instanceof String && state == CANCELING) {
                    if (c.mCallback != null) c.mCallback.OnDeleted((String) object);
                }
            }
        });
    }

    private Observable.OnSubscribe<Object> onSubscribe(final int state) {
        return new Observable.OnSubscribe<Object>() {
            final ZooperIconFontsHelper c = ZooperIconFontsHelper.this;
            final String ICONSETS = "iconsets";
            @Override
            public void call(final Subscriber<? super Object> subscriber) {
                Scheduler.Worker w = Schedulers.newThread().createWorker();
                Action0 a = new Action0() {
                    @Override
                    public void call() {
                        try {
                            if (state == FIXING) {
                                if (c.mFixIndex < c.mMissing.size())
                                    subscriber.onNext(c.save());
                                else subscriber.onCompleted();
                                c.mFixIndex++;
                            } else if (state == CANCELING) {
                                if (c.mCancelIndex < c.mFixed.size())
                                    subscriber.onNext(c.delete());
                                else subscriber.onCompleted();
                                c.mCancelIndex++;
                            } else {
                                c.preChecks();
                                c.checkAndCompare(ICONSETS);
                                subscriber.onCompleted();
                            }
                        } catch (IOException exception) {
                            subscriber.onError(exception);
                        }
                    }
                };
                long DELAY0 = 400;
                long DELAY1 = 600;
                subscriber.add(state == CHECKING ? w.schedule(a) :
                        w.schedulePeriodically(a, DELAY0, DELAY1, TimeUnit.MILLISECONDS));
            }
        };
    }

    /************************************************************************/

    private static final String USERunderscoreICONSETSunderscorePATH = Environment.getExternalStorageDirectory().toString() + "/ZooperWidget/IconSets/";

    private void preChecks() {
        this.mIconsets = new ArrayList<>();
        this.mMissing = new ArrayList<>();
        this.mFixed = new ArrayList<>();
        this.mFixIndex = 0;
        this.mCancelIndex = 0;
        try {
            File d = new File(USERunderscoreICONSETSunderscorePATH);
            if (!d.exists()) throw new FileNotFoundException();
            Collections.addAll(this.mIconsets, d.list());
        } catch (FileNotFoundException exception) {
            if (this.mCallback != null) this.mCallback.OnError(exception);
        }
    }

    private static final String underscore = File.separator;

    private boolean checkAndCompare(String path) throws IOException {
        String ICONS = "icons";
        String ZIP = ".zip";
        for (String n : this.mAssetManager.list(path)) {
            String p = path + underscore + n;
            if (!n.equals(ICONS)) if (!checkAndCompare(p)) return false;
            else if (!this.mIconsets.contains(n) && n.endsWith(ZIP))
                this.mMissing.add(p);
        }
        return true;
    }

    private File save() throws IOException {
        String p = this.mMissing.get(this.mFixIndex);
        String n = p.substring(p.lastIndexOf(underscore));
        File f = new File(USERunderscoreICONSETSunderscorePATH + underscore + n);
        FileUtils.copyToFile(this.mAssetManager.open(p), f);
        this.mFixed.add(f);
        return f;
    }

    private String delete() throws IOException {
        File f = this.mFixed.get(this.mCancelIndex);
        String n = f.getName();
        FileUtils.forceDelete(f);
        return n;
    }

}