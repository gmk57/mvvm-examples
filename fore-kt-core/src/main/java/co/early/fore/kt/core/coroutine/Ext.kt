package co.early.fore.kt.core.coroutine

import co.early.fore.core.WorkMode
import co.early.fore.kt.core.delegate.Fore
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext


/**
 * Testing unwrapped co-routines is not as straight forward as it could be, depending on
 * kotlinx-coroutines-test, was not giving us determinate test results at
 * the moment - and for unit tests that's a total deal breaker. It's ok to test single suspend
 * functions, but not if you want to test units of code that contain more than one suspend function
 * and use a mixture of IO and Main dispatchers.
 *
 * There is a complicated discussion about that here: https://github.com/Kotlin/kotlinx.coroutines/pull/1206
 *
 * In any case https://github.com/Kotlin/kotlinx.coroutines/blob/coroutines-test/kotlinx-coroutines-test/README.md
 * is focussed on swapping out the main dispatcher for unit tests. Even with runBlockingTest, other dispatchers can
 * still run concurrently with your tests, making tests much more complicated. The whole thing is extremely
 * complicated in fact (which is probably why it doesn't work yet).
 *
 * For the moment we continue to use the very simple and clear WorkMode switch as we have done in the past.
 * SYNCHRONOUS means everything is run sequentially in a blocking manner and on whatever thread the caller
 * is on. ASYNCHRONOUS gives you the co-routine behaviour you would expect.
 *
 * NB. This means there is no virtual time unless you implement it yourself though, for instance if you have code
 * like this in your app: delay(10 000), it will sit there and wait during a unit test, same as it would in app code.
 * You can write something like this instead: delay(if (workMode == WorkMode.ASYNCHRONOUS) 10000 else 1)
 *
 * Copyright © 2019 early.co. All rights reserved.
 */

fun launchIO(workMode: WorkMode? = null, exceptionHandler: CoroutineExceptionHandler? = null, block: suspend CoroutineScope.() -> Unit): Job {
    return if (Fore.getWorkMode(workMode) == WorkMode.SYNCHRONOUS) {
        runBlocking { CompletableDeferred(block()) }
    } else {
        CoroutineScope(Dispatchers.IO.add(exceptionHandler)).launch { block() }
    }
}

fun launchDefault(workMode: WorkMode? = null, exceptionHandler: CoroutineExceptionHandler? = null, block: suspend CoroutineScope.() -> Unit): Job {
    return if (Fore.getWorkMode(workMode) == WorkMode.SYNCHRONOUS) {
        runBlocking { CompletableDeferred(block()) }
    } else {
        CoroutineScope(Dispatchers.Default.add(exceptionHandler)).launch { block() }
    }
}

fun launchCustom(dispatcher: CoroutineContext, workMode: WorkMode? = null, block: suspend CoroutineScope.() -> Unit): Job {
    return if (Fore.getWorkMode(workMode) == WorkMode.SYNCHRONOUS) {
        runBlocking { CompletableDeferred(block()) }
    } else {
        CoroutineScope(dispatcher).launch { block() }
    }
}

/**
 * Platform may or may not provide instance of `MainDispatcher`, see kotlin documentation to [Dispatchers.Main]
 * if using this code from a pure kotlin module
 */
fun launchMain(workMode: WorkMode? = null, exceptionHandler: CoroutineExceptionHandler? = null, block: suspend CoroutineScope.() -> Unit): Job {
    return if (Fore.getWorkMode(workMode) == WorkMode.SYNCHRONOUS) {
        runBlocking { CompletableDeferred(block()) }
    } else {
        CoroutineScope(Dispatchers.Main.add(exceptionHandler)).launch { block() }
    }
}

/**
 * Platform may or may not provide instance of `MainDispatcher`, see kotlin documentation to [Dispatchers.Main]
 * if using this code from a pure kotlin module
 *
 * Implementation note: [MainCoroutineDispatcher.immediate] is not supported on Native and JS platforms.
 */
fun launchMainImm(workMode: WorkMode? = null, exceptionHandler: CoroutineExceptionHandler? = null, block: suspend CoroutineScope.() -> Unit): Job {
    return if (Fore.getWorkMode(workMode) == WorkMode.SYNCHRONOUS) {
        runBlocking { CompletableDeferred(block()) }
    } else {
        CoroutineScope(Dispatchers.Main.immediate.add(exceptionHandler)).launch { block() }
    }
}

fun <T> asyncIO(workMode: WorkMode? = null, exceptionHandler: CoroutineExceptionHandler? = null, block: suspend CoroutineScope.() -> T): Deferred<T> {
    return if (Fore.getWorkMode(workMode) == WorkMode.SYNCHRONOUS) {
        runBlocking { CompletableDeferred(block()) }
    } else {
        CoroutineScope(Dispatchers.IO.add(exceptionHandler)).async { block() }
    }
}

fun <T> asyncDefault(workMode: WorkMode? = null, exceptionHandler: CoroutineExceptionHandler? = null, block: suspend CoroutineScope.() -> T): Deferred<T> {
    return if (Fore.getWorkMode(workMode) == WorkMode.SYNCHRONOUS) {
        runBlocking { CompletableDeferred(block()) }
    } else {
        CoroutineScope(Dispatchers.Default.add(exceptionHandler)).async { block() }
    }
}

fun <T> asyncCustom(dispatcher: CoroutineContext, workMode: WorkMode? = null, block: suspend CoroutineScope.() -> T): Deferred<T> {
    return if (Fore.getWorkMode(workMode) == WorkMode.SYNCHRONOUS) {
        runBlocking { CompletableDeferred(block()) }
    } else {
        CoroutineScope(dispatcher).async { block() }
    }
}

/**
 * Platform may or may not provide instance of `MainDispatcher`, see kotlin documentation to [Dispatchers.Main]
 * if using this code from a pure kotlin module
 */
fun <T> asyncMain(workMode: WorkMode? = null, exceptionHandler: CoroutineExceptionHandler? = null, block: suspend CoroutineScope.() -> T): Deferred<T> {
    return if (Fore.getWorkMode(workMode) == WorkMode.SYNCHRONOUS) {
        runBlocking { CompletableDeferred(block()) }
    } else {
        CoroutineScope(Dispatchers.Main.add(exceptionHandler)).async { block() }
    }
}

/**
 * Platform may or may not provide instance of `MainDispatcher`, see kotlin documentation to [Dispatchers.Main]
 * if using this code from a pure kotlin module
 *
 * Implementation note: [MainCoroutineDispatcher.immediate] is not supported on Native and JS platforms.
 */
fun <T> asyncMainImm(workMode: WorkMode? = null, exceptionHandler: CoroutineExceptionHandler? = null, block: suspend CoroutineScope.() -> T): Deferred<T> {
    return if (Fore.getWorkMode(workMode) == WorkMode.SYNCHRONOUS) {
        runBlocking { CompletableDeferred(block()) }
    } else {
        CoroutineScope(Dispatchers.Main.immediate.add(exceptionHandler)).async { block() }
    }
}

suspend fun <T> awaitIO(workMode: WorkMode? = null, exceptionHandler: CoroutineExceptionHandler? = null, block: suspend CoroutineScope.() -> T): T {
    return if (Fore.getWorkMode(workMode) == WorkMode.SYNCHRONOUS) {
        block(CoroutineScope(coroutineContext))
    } else {
        withContext(Dispatchers.IO.add(exceptionHandler)) { block() }
    }
}

suspend fun <T> awaitDefault(workMode: WorkMode? = null, exceptionHandler: CoroutineExceptionHandler? = null, block: suspend CoroutineScope.() -> T): T {
    return if (Fore.getWorkMode(workMode) == WorkMode.SYNCHRONOUS) {
        block(CoroutineScope(coroutineContext))
    } else {
        withContext(Dispatchers.Default.add(exceptionHandler)) { block() }
    }
}

suspend fun <T> awaitCustom(dispatcher: CoroutineContext, workMode: WorkMode? = null, block: suspend CoroutineScope.() -> T): T {
    return if (Fore.getWorkMode(workMode) == WorkMode.SYNCHRONOUS) {
        block(CoroutineScope(coroutineContext))
    } else {
        withContext(dispatcher) { block() }
    }
}

/**
 * Platform may or may not provide instance of `MainDispatcher`, see kotlin documentation to [Dispatchers.Main]
 * if using this code from a pure kotlin module
 */
suspend fun <T> awaitMain(workMode: WorkMode? = null, exceptionHandler: CoroutineExceptionHandler? = null, block: suspend CoroutineScope.() -> T): T {
    return if (Fore.getWorkMode(workMode) == WorkMode.SYNCHRONOUS) {
        block(CoroutineScope(coroutineContext))
    } else {
        withContext(Dispatchers.Main.add(exceptionHandler)) { block() }
    }
}

/**
 * Platform may or may not provide instance of `MainDispatcher`, see kotlin documentation to [Dispatchers.Main]
 * if using this code from a pure kotlin module
 *
 * Implementation note: [MainCoroutineDispatcher.immediate] is not supported on Native and JS platforms.
 */
suspend fun <T> awaitMainImm(workMode: WorkMode? = null, exceptionHandler: CoroutineExceptionHandler? = null, block: suspend CoroutineScope.() -> T): T {
    return if (Fore.getWorkMode(workMode) == WorkMode.SYNCHRONOUS) {
        block(CoroutineScope(coroutineContext))
    } else {
        withContext(Dispatchers.Main.immediate.add(exceptionHandler)) { block() }
    }
}

private fun CoroutineContext.add(exceptionHandler: CoroutineExceptionHandler?): CoroutineContext {
    return exceptionHandler?.let {
        this + it
    } ?: this
}
