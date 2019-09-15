package co.early.fore.lifecycle;

import android.view.LayoutInflater;

import java.util.Arrays;
import java.util.List;

import co.early.fore.core.Affirm;
import co.early.fore.core.observer.Observable;
import co.early.fore.core.observer.Observer;
import co.early.fore.core.ui.SyncableView;
import co.early.fore.lifecycle.activity.SyncActivity;
import co.early.fore.lifecycle.activity.SyncXActivity;
import co.early.fore.lifecycle.fragment.SyncFragment;
import co.early.fore.lifecycle.fragment.SyncXFragment;
import co.early.fore.lifecycle.view.SyncViewActivity;
import co.early.fore.lifecycle.view.SyncViewFragment;
import co.early.fore.lifecycle.view.SyncViewXActivity;
import co.early.fore.lifecycle.view.SyncViewXFragment;

/**
 * <p>
 *      Class used by
 *      {@link SyncActivity},
 *      {@link SyncXActivity},
 *      {@link SyncFragment},
 *      {@link SyncXFragment}
 *      {@link SyncViewActivity},
 *      {@link SyncViewXActivity},
 *      {@link SyncViewFragment},
 *      {@link SyncViewXFragment}
 *      to hold a reference to a list of {@link Observable} instances.
 *      A single {@link Observer} which calls {@link SyncableView#syncView()} when notified
 *      is added and removed in line with android lifecycle methods to prevent memory leaks
 *      and ensure UI consistency.
 * </p>
 */
public class LifecycleSyncer {

    private final List<Observable> observablesList;
    public final SyncableView syncableView;

    private Observer viewUpdater = new Observer() {
        @Override
        public void somethingChanged() {
            syncableView.syncView();
        }
    };

    public LifecycleSyncer(LayoutInflater layoutInflater, int layoutResourceId, Observables observables) {
        Affirm.notNull(layoutInflater);
        this.observablesList = Affirm.notNull(observables).observablesList;
        this.syncableView = Affirm.notNull((SyncableView) layoutInflater.inflate(layoutResourceId, null));
    }

    public LifecycleSyncer(SyncableView syncableView, Observables observables) {
        this.observablesList = Affirm.notNull(observables).observablesList;
        this.syncableView = Affirm.notNull(syncableView);
    }

    public void addObserversAndSync() {
        for (Observable observable : observablesList){
            observable.addObserver(viewUpdater);
        }
        syncableView.syncView();
    }

    public void removeObservers() {
        for (Observable observable : observablesList){
            observable.removeObserver(viewUpdater);
        }
    }


    public static class Observables{

        private final List<Observable> observablesList;

        public Observables(Observable... observablesList) {
            this.observablesList = Arrays.asList(Affirm.notNull(observablesList));
        }
    }

}
