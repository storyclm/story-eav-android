package expert.rightperception.attributesapp.data.repository.signalr

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.provider.Settings
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.microsoft.signalr.Subscription
import expert.rightperception.attributesapp.BuildConfig
import expert.rightperception.attributesapp.R
import expert.rightperception.attributesapp.data.repository.signalr.model.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import java.util.*

class SignalRConnector(
    context: Context,
    endpoint: String,
    entityIds: List<String>,
    eventSubscribers: List<EventSubscriber>,
    reconnectPolicy: ReconnectPolicy = ReconnectPolicy(),
    accessTokenProvider: () -> String?
) {

    private val compositeDisposable = CompositeDisposable()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val subscribers = eventSubscribers.toMutableList()
    private val virtualDevice = createVirtualDevice(context, entityIds)
    private val hubConnection: HubConnection = HubConnectionBuilder.create(endpoint)
        .withAccessTokenProvider(Single.fromCallable(accessTokenProvider))
        .build()
    private val onSubs = mutableListOf<Subscription>()

    init {
        val request = NetworkRequest.Builder()
            .build()
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                reconnect()
            }
        }
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).registerNetworkCallback(request, callback)

        scope.launch {
            delay(1 * 60 * 1000)
            reconnect()
        }

        hubConnection.onClosed {
            it.printStackTrace()
            reconnect()
        }
        connectSignalR()
    }

    private fun connectSignalR() {
        execute(hubConnection.start()) {
            execute(hubConnection.invoke("ConnectToVirtualDevice", virtualDevice)) {
                createSubscriptions()
            }
        }
    }
    private fun reconnect() {
        if (hubConnection.connectionState == HubConnectionState.DISCONNECTED) {
            connectSignalR()
        }
    }

    fun dispose() {
        subscribers.clear()
        onSubs.unsubscribe()
        val stopSubscription = hubConnection.stop()
            .doOnTerminate { compositeDisposable.dispose() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                },
                {
                    it.printStackTrace()
                }
            )
        compositeDisposable.add(stopSubscription)
    }

    private fun createVirtualDevice(context: Context, subscriptions: List<String>): VirtualDevice {
        val nightMode = context.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)
        val theme  = if (nightMode == Configuration.UI_MODE_NIGHT_YES) "Dark" else "Light"
        return VirtualDevice(
            deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID),
            os = 0,
            osVersion = Build.VERSION.SDK_INT.toString(),
            app = context.getString(R.string.app_name),
            appVersion = BuildConfig.VERSION_NAME,
            language = "ru-ru",
            model = Build.MODEL,
            timeZone = TimeZone.getDefault().rawOffset / 1000 / 60 / 60,
            theme = theme,
            subscriptions = subscriptions
        )
    }

    private fun execute(completable: Completable, retryCount: Int = 2, onSuccess: () -> Unit) {
        val disposable = completable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    onSuccess()
                },
                {
                    it.printStackTrace()
                    if (retryCount > 0) {
                        execute(completable, retryCount - 1, onSuccess)
                    }
                }
            )
        compositeDisposable.add(disposable)
    }

    private fun createSubscriptions() {
        onSubs.unsubscribe()
        subscribers.forEach { eventSubscriber ->
            val onSub = when (eventSubscriber) {
                is EventsSubscriber0 -> {
                    hubConnection.on(eventSubscriber.target, eventSubscriber.callback)
                }
                is EventsSubscriber1<*> -> {
                    hubConnection.on(eventSubscriber.target, eventSubscriber.callback, eventSubscriber.param)
                }
                is EventsSubscriber2<*, *> -> {
                    hubConnection.on(eventSubscriber.target, eventSubscriber.callback, eventSubscriber.param1, eventSubscriber.param2)
                }
                is EventsSubscriber3<*, *, *> -> {
                    hubConnection.on(eventSubscriber.target, eventSubscriber.callback, eventSubscriber.param1, eventSubscriber.param2, eventSubscriber.param3)
                }
            }
            onSubs.add(onSub)
        }
    }

    private fun List<Subscription>.unsubscribe() {
        forEach { it.unsubscribe() }
    }
}