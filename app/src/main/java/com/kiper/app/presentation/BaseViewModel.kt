package com.kiper.app.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import retrofit2.HttpException

open class BaseViewModel : ViewModel() {

    fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(context = NonCancellable, block = block)
    }

    suspend fun execute(
        body: suspend () -> Unit,
    ) {
        try {
            body.invoke()
        } catch (ex: Exception) {
            Log.e("Exception", "Error: $ex")
        } catch (ex: HttpException) {
            Log.e("HttpException", "Error: $ex")
        } catch (ex: ArrayIndexOutOfBoundsException) {
            Log.e("ArrayIndexOutOfBoundsException", "Error: $ex")
        } catch (ex: NullPointerException) {
            Log.e("NullPointerException", "Error: $ex")
        } catch (ex: NoSuchFileException) {
            Log.e("NoSuchFileException", "Error: $ex")
        } catch (ex: Exception) {
            Log.e("Exception", "Error: $ex")
        }
    }
}