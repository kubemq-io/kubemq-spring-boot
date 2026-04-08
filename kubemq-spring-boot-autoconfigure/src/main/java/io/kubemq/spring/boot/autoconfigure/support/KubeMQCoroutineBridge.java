package io.kubemq.spring.boot.autoconfigure.support;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * Bridge interface for Kotlin coroutine listener support.
 *
 * <p>The autoconfigure module cannot directly reference the concrete
 * {@code KubeMQCoroutineListenerSupport} class because it lives in the
 * kotlin module (which depends on autoconfigure, not the other way around).
 * This interface defines the contract the container needs, and the kotlin
 * module's support class implements it.
 */
public interface KubeMQCoroutineBridge {

    /**
     * Returns {@code true} if the given method is a Kotlin {@code suspend} function.
     */
    boolean isSuspendFunction(Method method);

    /**
     * Invokes a suspend listener method within a per-container coroutine scope.
     *
     * @param containerId the container identifier (used for scope isolation)
     * @param target      the bean instance containing the listener method
     * @param method      the suspend method to invoke
     * @param args        the arguments to pass (excluding the continuation)
     * @return a {@link CompletableFuture} that completes when the suspend function finishes
     */
    CompletableFuture<Object> invokeSuspend(String containerId, Object target, Method method, Object... args);

    /**
     * Cancels the coroutine scope associated with the given container.
     */
    void cancelScope(String containerId);
}
