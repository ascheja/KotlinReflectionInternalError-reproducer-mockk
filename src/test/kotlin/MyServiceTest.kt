package org.ascheja

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class MyServiceTest {
    private val dependencyMock: SomeDependency = mockk()

    private val myService = MyService(dependencyMock)

    @Test
    fun `doItDeferredLambda suspends until callback is called`() = runTestWithMethod {
        doItDeferredLambda()
    }

    @Test
    fun `doItDeferredAnonymousObject suspends until callback is called`() = runTestWithMethod {
        doItDeferredAnonymousObject()
    }

    @Test
    fun `doItSuspendCoroutine suspends until callback is called`() = runTestWithMethod {
        doItSuspendCoroutine()
    }

    private fun runTestWithMethod(method: suspend MyService.() -> Unit) = runTest {
        val slot = slot<Callback>()
        every { dependencyMock.methodTakingCallback(capture(slot)) } just Runs
        val doItJob = launch {
            myService.method()
        }
        runCurrent()
        try {
            assertFalse(!doItJob.isCompleted, "method should suspend until callback is called")
        } catch (e: AssertionError) {
            doItJob.cancel(CancellationException("failed", e))
        }
        val callback = slot.captured
        callback.onCallback()
        doItJob.join()
        verify {
            dependencyMock.methodTakingCallback(callback)
        }
    }
}
