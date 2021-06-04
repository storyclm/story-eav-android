package expert.rightperception.attributesapp.data.repository.signalr

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.provider.Settings
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
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
import java.util.concurrent.atomic.AtomicReference

class SignalRConnector(
    context: Context,
    endpoint: String,
    entityIds: List<String>,
    eventSubscribers: List<EventSubscriber>,
    private val reconnectPolicy: ReconnectPolicy = ReconnectPolicy(),
    accessTokenProvider: () -> String?
) {

    private var lastConnect: Date? = null
    private val connectionStatus = AtomicReference(ConnectionStatus.DISCONNECTED)
    private val compositeDisposable = CompositeDisposable()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val reconnectJob: Job

    private val subscribers = eventSubscribers.toMutableList()
    private val virtualDevice = createVirtualDevice(context, entityIds)
    private val hubConnection: HubConnection = HubConnectionBuilder.create(endpoint)
        .withAccessTokenProvider(Single.fromCallable(accessTokenProvider))
        .build()
    private val onSubs = mutableListOf<Subscription>()

    init {
        hubConnection.onClosed {
            it.printStackTrace()
            connectionStatus.set(ConnectionStatus.DISCONNECTED)
            connectSignalR()
        }
        connectSignalR()

        val request = NetworkRequest.Builder()
            .build()
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                connectSignalR()
            }
        }
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).registerNetworkCallback(request, callback)

        reconnectJob = scope.launch {
            delay(reconnectPolicy.reconnectIntervalMillis)
            connectSignalR()
        }
    }

    private fun connectSignalR(retryCount: Long = reconnectPolicy.numberOfRetries) {
        if (lastConnect?.let { (Date().time - it.time) < reconnectPolicy.millisBetweenRetries } == true) return
        if (retryCount > 0) {
            if (connectionStatus.compareAndSet(ConnectionStatus.DISCONNECTED, ConnectionStatus.CONNECTING)) {
                lastConnect = Date()
                ExecutionChain(
                    compositeDisposable,
                    reconnectPolicy.numberOfRetries,
                    {
                        createSubscriptions()
                        connectionStatus.set(ConnectionStatus.CONNECTED)
                    },
                    {
                        it.printStackTrace()
                        connectionStatus.set(ConnectionStatus.DISCONNECTED)
                        scope.launch {
                            delay(reconnectPolicy.millisBetweenRetries)
                            connectSignalR(retryCount - 1)
                        }
                    }
                )
                    .add { hubConnection.start() }
                    .add { hubConnection.invoke("ConnectToVirtualDevice", virtualDevice) }
                    .execute()
            }
        } else {
            connectionStatus.set(ConnectionStatus.DISCONNECTED)
        }
    }

    fun dispose() {
        reconnectJob.cancel()
        subscribers.clear()
        compositeDisposable.dispose()
        onSubs.unsubscribe()
        val stopSubscription = hubConnection.stop()
            .doOnTerminate { compositeDisposable.dispose() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {},
                {
                    it.printStackTrace()
                }
            )
        compositeDisposable.add(stopSubscription)
    }

    @SuppressLint("HardwareIds")
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

    private enum class ConnectionStatus {
        CONNECTED, CONNECTING, DISCONNECTED
    }

    private class ExecutionChain(
        private val compositeDisposable: CompositeDisposable,
        private val retryCount: Long,
        private val onSuccess: () -> Unit,
        private val onError: (Throwable) -> Unit
    ) {
        private val executables = mutableListOf<() -> Completable>()

        fun add(executable: () -> Completable): ExecutionChain {
            executables.add(executable)
            return this
        }

        fun execute() {
            if (executables.isNotEmpty()) {
                exec(0, retryCount)
            }
        }

        private fun exec(index: Int, retriesLeft: Long) {
            val completable = executables[index]()
            val disposable = completable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        val nextIndex = index + 1
                        if (nextIndex < executables.size) {
                            exec(nextIndex, retryCount)
                        } else {
                            onSuccess()
                        }
                    },
                    {
                        it.printStackTrace()
                        if (retriesLeft > 0) {
                            exec(index, retriesLeft - 1)
                        } else {
                            onError(it)
                        }
                    }
                )
            compositeDisposable.add(disposable)
        }
    }
}