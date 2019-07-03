package com.grio.lib


import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import androidx.arch.core.executor.testing.InstantTaskExecutorRule



/**
 * Base class for Unit tests. Inherit from it to create test cases which DO NOT contain android
 * framework dependencies or components.
 *
 * @see AndroidTest
 */
@RunWith(MockitoJUnitRunner::class)
abstract class UnitTest {

    @Suppress("LeakingThis")
    @Rule
    @JvmField val injectMocks = InjectMocksRule.create(this@UnitTest)

    @Suppress("LeakingThis")
    @Rule
    @JvmField val instantExecutorRule = InstantTaskExecutorRule()

}
