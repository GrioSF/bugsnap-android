package com.grio.lib.core.interactor

import com.grio.lib.core.exception.Failure
import com.grio.lib.core.functional.Either
import kotlinx.coroutines.*


/**
 * Abstract class for a Use Case (Interactor in terms of Clean Architecture).
 * This abstraction represents an execution unit for different use cases (this means than any use
 * case in the application should implement this contract).
 *
 * By convention each [UseCase] implementation will execute its job in a background thread
 * (kotlin coroutine) and will post the result in the UI thread.
 */
abstract class UseCase<out Type, in Params> where Type : Any {

    abstract suspend fun run(params: Params? = null): Either<Failure, Type>


    operator fun invoke(params: Params?, onResult: (Either<Failure, Type>) -> Unit = {}) {
        val job = GlobalScope.async {
            if (params != null) {
                run(params)
            } else {
                run()
            }
        }
        MainScope().launch(Dispatchers.Main) { onResult(job.await()) }
    }

    class None
}

abstract class UseCaseNoParams<out Type> where Type : Any {

    abstract suspend fun run(): Either<Failure, Type>


    operator fun invoke(onResult: (Either<Failure, Type>) -> Unit = {}) {
        val job = GlobalScope.async {
            run()
        }
        MainScope().launch(Dispatchers.Main) { onResult(job.await()) }
    }

    class None
}

abstract class UseCaseSync<out Type, in Params> where Type : Any {

    abstract fun run(params: Params): Either<Failure, Type>

    operator fun invoke(params: Params, onResult: (Either<Failure, Type>) -> Unit = {}) {
        val job = run(params)
        onResult(job)
    }

    class None

}