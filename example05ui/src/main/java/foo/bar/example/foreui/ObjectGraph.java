package foo.bar.example.foreui;

import android.app.Application;

import java.util.HashMap;
import java.util.Map;

import co.early.fore.core.WorkMode;
import co.early.fore.core.logging.AndroidLogger;
import co.early.fore.core.logging.Logger;
import co.early.fore.core.time.SystemTimeWrapper;
import foo.bar.example.foreui.feature.tictactoe.Board;

import static co.early.fore.core.Affirm.notNull;

/**
 * This is the price you pay for not using Dagger, the payback is not having to write modules
 */
class ObjectGraph {

    private volatile boolean initialized = false;
    private final Map<Class<?>, Object> dependencies = new HashMap<>();


    void setApplication(Application application) {
        setApplication(application, WorkMode.ASYNCHRONOUS);
    }

    void setApplication(Application application, final WorkMode workMode) {

        notNull(application);
        notNull(workMode);

        // create dependency graph
        AndroidLogger logger = new AndroidLogger();
        Board board = new Board(workMode, new SystemTimeWrapper());

        // add models to the dependencies map if you will need them later
        dependencies.put(Board.class, board);
        dependencies.put(Logger.class, logger);

    }

    void init() {
        if (!initialized) {
            initialized = true;

            // run any necessary initialization code once object graph has been created here

        }
    }

    <T> T get(Class<T> model) {

        notNull(model);
        T t = model.cast(dependencies.get(model));
        notNull(t);

        return t;
    }

    <T> void putMock(Class<T> clazz, T object) {

        notNull(clazz);
        notNull(object);

        dependencies.put(clazz, object);
    }

}