package expert.rightperception.attributesapp.data.repository.signalr.model

import com.microsoft.signalr.Action
import com.microsoft.signalr.Action1
import com.microsoft.signalr.Action2
import com.microsoft.signalr.Action3

sealed class EventSubscriber

class EventsSubscriber0(
    val target: String,
    val callback: Action
) : EventSubscriber()

class EventsSubscriber1<T>(
    val target: String,
    val param: Class<T>,
    val callback: Action1<T>
) : EventSubscriber()

class EventsSubscriber2<T1, T2>(
    val target: String,
    val param1: Class<T1>,
    val param2: Class<T2>,
    val callback: Action2<T1, T2>
): EventSubscriber()

class EventsSubscriber3<T1, T2, T3>(
    val target: String,
    val param1: Class<T1>,
    val param2: Class<T2>,
    val param3: Class<T3>,
    val callback: Action3<T1, T2, T3>
): EventSubscriber()