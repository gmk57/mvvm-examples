package foo.bar.example.foreretrofit;

import android.app.Application;

import java.util.HashMap;
import java.util.Map;

import co.early.fore.core.WorkMode;
import co.early.fore.core.logging.AndroidLogger;
import co.early.fore.retrofit.CallProcessor;
import co.early.fore.retrofit.InterceptorLogging;
import foo.bar.example.foreretrofit.api.CustomGlobalErrorHandler;
import foo.bar.example.foreretrofit.api.CustomGlobalRequestInterceptor;
import foo.bar.example.foreretrofit.api.CustomRetrofitBuilder;
import foo.bar.example.foreretrofit.api.fruits.FruitService;
import foo.bar.example.foreretrofit.feature.fruit.FruitFetcher;
import foo.bar.example.foreretrofit.message.UserMessage;
import retrofit2.Retrofit;

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

        // networking classes common to all models
        Retrofit retrofit = CustomRetrofitBuilder.create(
                new CustomGlobalRequestInterceptor(logger),
                new InterceptorLogging(logger));//logging interceptor should be the last one

        CallProcessor<UserMessage> callProcessor = new CallProcessor<UserMessage>(
                new CustomGlobalErrorHandler(logger),
                logger);


        // models
        FruitFetcher fruitFetcher = new FruitFetcher(
                retrofit.create(FruitService.class),
                callProcessor,
                logger,
                workMode);


        // add models to the dependencies map if you will need them later
        dependencies.put(FruitFetcher.class, fruitFetcher);

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