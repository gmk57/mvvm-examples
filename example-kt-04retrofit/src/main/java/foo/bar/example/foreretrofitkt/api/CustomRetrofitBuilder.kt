package foo.bar.example.foreretrofitkt.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Most of this will all be specific to your application, when customising for your own case
 * bare in mind that you should be able to use this class in your tests to mock the server
 * by passing different interceptors in:
 *
 * see @[co.early.fore.net.testhelpers.InterceptorStubbedService]
 *
 */
object CustomRetrofitBuilder {

    /**
     * Here we heed to customise Retrofit baseUrl (for testing with MockWebServer) and OkHttp
     * configuration (interceptors, timeouts)
     * @return Retrofit object suitable for instantiating service interfaces
     */
    fun create(
        url: String = "http://www.mocky.io/v2/",
        okHttpConfig: OkHttpClient.Builder.() -> Unit = {}
    ): Retrofit {

        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .client(createOkHttpClient(okHttpConfig))
            .build()
    }

    private fun createOkHttpClient(okHttpConfig: OkHttpClient.Builder.() -> Unit): OkHttpClient {

        val builder = OkHttpClient.Builder()

        builder.okHttpConfig()

        return builder.build()
    }

}
