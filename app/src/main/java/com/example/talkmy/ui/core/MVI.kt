package com.example.talkmy.ui.core

import com.orhanobut.logger.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.milliseconds

/**
 * Interface for a component that holds and manages UI state.
 */
interface StateContainer<State> {
    val state: StateFlow<State>
    fun updateState(reducer: State.() -> State)
}

/**
 * Interface for a component that manages side effects (one-off events).
 */
interface EffectContainer<Effect> {
    val effect: Flow<Effect>
    suspend fun postEffect(builder: () -> Effect)
}

/**
 * Interface for a component that handles user actions.
 */
interface ActionHandler<Action> {
    fun onAction(action: Action)
}

/**
 * Default implementation of StateContainer using StateFlow.
 */
class DefaultStateContainer<State>(initialState: State) : StateContainer<State> {
    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<State> = _state.asStateFlow()

    @Synchronized
    override fun updateState(reducer: State.() -> State) {
        _state.update(reducer)
    }
}

/**
 * Default implementation of EffectContainer using Channels to ensure events are delivered.
 */
class DefaultEffectContainer<Effect> : EffectContainer<Effect> {
    private val _effect = Channel<Effect>(Channel.BUFFERED)
    override val effect: Flow<Effect> = _effect.receiveAsFlow()

    override suspend fun postEffect(builder: () -> Effect) {
        _effect.send(builder())
    }
}

/**
 * Utility to ensure only one coroutine is active for a specific query/task.
 * Useful for preventing rapid-fire clicks from triggering multiple network requests.
 */
data class SingleActiveQuery(
    val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private var job: Job? = null,
    private val mutex: Mutex = Mutex(),
) {
    suspend fun launch(block: suspend CoroutineScope.() -> Unit) {
        val currentScope = CoroutineScope(currentCoroutineContext())
        val obj: suspend CoroutineScope.() -> Unit = {
            try {
                withContext(dispatcher) {
                    block()
                }
            } catch (t: Throwable) {
                Logger.e(t, "Error in SingleActiveQuery")
            }
        }
        mutex.withLock {
            job?.cancel()
            job?.join()
            job = currentScope.launch(block = obj)
        }
    }
}

/**
 * Utility to debounce queries (e.g., search text input).
 */
data class DebounceQuery(
    private val pipe: MutableSharedFlow<String> = MutableSharedFlow(extraBufferCapacity = 64, replay = 1)
) {
    @OptIn(FlowPreview::class)
    suspend fun launch(collector: FlowCollector<String>) {
        pipe.debounce(300L.milliseconds)
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .collect(collector)
    }
    
    suspend fun emit(query: String) {
        pipe.emit(query)
    }
}
