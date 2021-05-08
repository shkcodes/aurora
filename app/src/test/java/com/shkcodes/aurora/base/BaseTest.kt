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
    open fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        Dispatchers.resetMain()
        testScope.cleanupTestCoroutines()
    }

    @ExperimentalTime
    fun <S, I> BaseViewModel<S, I>.test(
        intents: List<I> = emptyList(),
        states: suspend FlowTurbine<S>.() -> Unit = { cancelAndIgnoreRemainingEvents() },
        sideEffects: suspend FlowTurbine<SideEffect>.() -> Unit = { cancelAndIgnoreRemainingEvents() }
    ) {
        testDispatcher.runBlockingTest {
            getState().test {
                intents.forEach(::handleIntent)
                states(this)
            }
        }
        testDispatcher.runBlockingTest {
            getSideEffects().test {
                intents.forEach(::handleIntent)
                sideEffects(this)
            }
        }
    }

}