package io.kubemq.spring.boot.kotlin

import io.kubemq.spring.boot.autoconfigure.support.KubeMQCoroutineBridge
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.future.future
import org.springframework.beans.factory.DisposableBean
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.kotlinFunction

/**
 * Provides infrastructure for invoking `suspend` listener methods from the
 * Java-based listener container.
 *
 * Each container gets its own [CoroutineScope] (keyed by container ID) with a
 * [SupervisorJob] so that one failing listener invocation does not cancel sibling
 * listeners. The dispatcher is configurable via the constructor parameter,
 * defaulting to [Dispatchers.Default].
 *
 * Implements [KubeMQCoroutineBridge] so the autoconfigure module (which cannot
 * depend on the kotlin module) can interact via the bridge interface.
 */
class KubeMQCoroutineListenerSupport(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : KubeMQCoroutineBridge, DisposableBean {

    private val containerScopes = ConcurrentHashMap<String, CoroutineScope>()
    private val kFunctionCache = ConcurrentHashMap<Method, Any>()

    private companion object {
        val NOT_KOTLIN_FUNCTION = Any()
    }

    private fun resolveKFunction(method: Method): KFunction<*>? {
        val cached = kFunctionCache.computeIfAbsent(method) { m ->
            m.kotlinFunction ?: NOT_KOTLIN_FUNCTION
        }
        return cached as? KFunction<*>
    }

    /**
     * Returns `true` if the given [method] is a Kotlin `suspend` function.
     */
    override fun isSuspendFunction(method: Method): Boolean {
        val kFunction = resolveKFunction(method) ?: return false
        return kFunction.isSuspend
    }

    /**
     * Returns (or creates) the [CoroutineScope] for the given container.
     */
    fun scopeForContainer(containerId: String): CoroutineScope =
        containerScopes.computeIfAbsent(containerId) {
            CoroutineScope(SupervisorJob() + dispatcher)
        }

    /**
     * Invokes a suspend listener method within the per-container [CoroutineScope].
     *
     * @param containerId the container identifier (used for scope isolation)
     * @param target the bean instance containing the listener method
     * @param method the suspend method to invoke
     * @param args the arguments to pass (excluding the continuation -- callSuspend injects it automatically)
     * @return a [CompletableFuture] that completes when the suspend function finishes
     */
    override fun invokeSuspend(
        containerId: String,
        target: Any,
        method: Method,
        vararg args: Any?
    ): CompletableFuture<Any?> {
        val kFunction = resolveKFunction(method)
            ?: throw IllegalArgumentException("Method ${method.name} is not a Kotlin function")
        require(kFunction.isSuspend) { "Method ${method.name} is not a suspend function" }
        val scope = scopeForContainer(containerId)
        return scope.future { kFunction.callSuspend(target, *args) }
    }

    /**
     * Cancels the coroutine scope associated with the given container.
     */
    override fun cancelScope(containerId: String) {
        containerScopes.remove(containerId)?.cancel("Container $containerId stopped")
    }

    override fun destroy() {
        containerScopes.values.forEach { it.cancel("Spring context shutting down") }
        containerScopes.clear()
        kFunctionCache.clear()
    }
}
