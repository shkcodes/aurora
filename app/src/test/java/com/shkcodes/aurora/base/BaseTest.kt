package com.shkcodes.aurora.base

import app.cash.turbine.FlowTurbine
import app.cash.turbine.test
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import kotlin.time.ExperimentalTime

open class BaseTest {

    protected val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    protected val testDispatcherProvider = object : DispatcherProvider() {
        override val main: CoroutineDispatcher = testDispatcher
        override val io: CoroutineDispatcher = testDispatcher
        override val default: CoroutineDispatcher = testDispatcher
        override val unconfined: CoroutineDispatcher = testDispatcher
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        testDispatcher.pauseDispatcher()
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        Dispatchers.resetMain()
        testScope.cleanupTestCoroutines()
    }

    fun test(block: suspend () -> Unit) = testDispatcher.runBlockingTest {
        block()
    }

    @ExperimentalTime
    suspend fun <S, I> BaseViewModel<S, I>.testStates(
        states: suspend FlowTurbine<S>.() -> Unit
    ) {
        getState().test {
            states()
        }
    }

    @ExperimentalTime
    suspend fun <S, I> BaseViewModel<S, I>.testSideEffects(
        sideEffects: suspend FlowTurbine<SideEffect>.() -> Unit
    ) {
        getSideEffects().test {
            sideEffects()
        }
    }

}