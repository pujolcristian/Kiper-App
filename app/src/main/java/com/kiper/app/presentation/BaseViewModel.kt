package com.kiper.app.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

open class BaseViewModel : ViewModel() {

    fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(context = NonCancellable, block = block)
    }

    suspend fun execute(
        retries: Int = 2,
        delayDuration: Long = 10000L,
        body: suspend () -> Unit,
    ) {
        var currentAttempt = 0
        var lastException: Exception? = null

        while (currentAttempt <= retries) {
            try {
                withContext(viewModelScope.coroutineContext) {
                    body.invoke()
                }
                return // Exit the function if the body executes successfully
            } catch (ex: Exception) {
                lastException = ex
                Log.e("Exception", "Attempt $currentAttempt failed: $ex")
                if (currentAttempt >= retries) {
                    handleException(ex)
                    return
                }
                currentAttempt++
                delay(delayDuration)
            } catch (ex: HttpException) {
                lastException = ex
                Log.e("HttpException", "Attempt $currentAttempt failed: $ex")
                if (currentAttempt >= retries) {
                    handleException(ex)
                    return
                }
                currentAttempt++
                delay(delayDuration)
            } catch (ex: ArrayIndexOutOfBoundsException) {
                lastException = ex
                Log.e("ArrayIndexOutOfBoundsException", "Attempt $currentAttempt failed: $ex")
                if (currentAttempt >= retries) {
                    handleException(ex)
                    return
                }
                currentAttempt++
                delay(delayDuration)
            } catch (ex: NullPointerException) {
                lastException = ex
                Log.e("NullPointerException", "Attempt $currentAttempt failed: $ex")
                if (currentAttempt >= retries) {
                    handleException(ex)
                    return
                }
                currentAttempt++
                delay(delayDuration)
            } catch (ex: NoSuchFileException) {
                lastException = ex
                Log.e("NoSuchFileException", "Attempt $currentAttempt failed: $ex")
                if (currentAttempt >= retries) {
                    handleException(ex)
                    return
                }
                currentAttempt++
                delay(delayDuration)
            }
        }
        handleException(lastException)
    }

    private fun handleException(ex: Exception?) {
        ex?.let {
            when (it) {
                is HttpException -> Log.e("HttpException", "Final error: $it")
                is ArrayIndexOutOfBoundsException -> Log.e("ArrayIndexOutOfBoundsException", "Final error: $it")
                is NullPointerException -> Log.e("NullPointerException", "Final error: $it")
                is NoSuchFileException -> Log.e("NoSuchFileException", "Final error: $it")
                else -> Log.e("Exception", "Final error: $it")
            }
        }
    }
}
