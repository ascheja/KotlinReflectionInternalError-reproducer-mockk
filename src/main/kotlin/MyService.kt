package org.ascheja

import kotlinx.coroutines.CompletableDeferred
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MyService(private val someDependency: SomeDependency) {

    suspend fun doItDeferredLambda() {
        val result = CompletableDeferred<Unit>()
        someDependency.methodTakingCallback { result.complete(Unit) }
        return result.await()
    }

    suspend fun doItDeferredAnonymousObject() {
        val result = CompletableDeferred<Unit>()
        someDependency.methodTakingCallback(object : Callback {
            override fun onCallback() {
                result.complete(Unit)
            }
        })
        return result.await()
    }

    suspend fun doItSuspendCoroutine() {
        return suspendCoroutine { cont ->
            someDependency.methodTakingCallback {
                cont.resume(Unit)
            }
        }
    }
}
