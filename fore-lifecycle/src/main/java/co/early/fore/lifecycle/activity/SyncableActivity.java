package co.early.fore.lifecycle.activity;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import co.early.fore.core.Affirm;
import co.early.fore.core.ui.SyncableView;
import co.early.fore.lifecycle.LifecycleSyncer;
import co.early.fore.lifecycle.view.SyncViewActivity;

/**
 * @deprecated use {@link SyncViewActivity} instead.
 */
@Deprecated
public abstract class SyncableActivity extends Activity {


    private LifecycleSyncer lifecycleSyncer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SyncableView syncableView = Affirm.notNull((SyncableView)(getLayoutInflater().inflate(getResourceIdForSyncableView(), null)));

        setContentView((View)syncableView);

        setLifecycleSyncer(new LifecycleSyncer(syncableView,
                getThingsToObserve()));
    }

    private View setLifecycleSyncer(LifecycleSyncer lifecycleSyncer){
        this.lifecycleSyncer = Affirm.notNull(lifecycleSyncer);
        return (View)lifecycleSyncer.syncableView;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (lifecycleSyncer == null){
            throw new RuntimeException("You must call super.onCreate() from within your onCreate() method");
        }
        // add our observer to any models we want to observe
        lifecycleSyncer.addObserversAndSync();
    }

    @Override
    public void onPause() {
        super.onPause();
        // remove our observer from any models we are observing
        lifecycleSyncer.removeObservers();
    }

    public SyncableView getSyncableView(){
        return lifecycleSyncer.syncableView;
    }

    public abstract int getResourceIdForSyncableView();

    public abstract LifecycleSyncer.Observables getThingsToObserve();

}
