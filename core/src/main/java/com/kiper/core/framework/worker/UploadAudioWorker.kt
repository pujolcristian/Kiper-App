package com.kiper.core.framework.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kiper.core.domain.usecase.DeleteRecordingUseCase
import com.kiper.core.domain.usecase.GetRecordingsForDayUseCase
import com.kiper.core.domain.usecase.UploadAudioUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class UploadAudioWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val uploadAudioUseCase: UploadAudioUseCase,
    private val getRecordingsForDayUseCase: GetRecordingsForDayUseCase,
    private val deleteRecordingUseCase: DeleteRecordingUseCase,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val startOfDay = getStartOfDay().timeInMillis
        val endOfDay = getEndOfDay().timeInMillis
        val recordings = getRecordingsForDayUseCase(startOfDay, endOfDay)
        val deviceId = inputData.getString("deviceId") ?: return@withContext Result.failure()

        recordings.forEach { recording ->
            val success = uploadAudioUseCase.execute(emptyList(), deviceId, recording.fileName)
            println("UploadAudioWorker")
            success.forEach {
                if (it) {
                    deleteRecordingUseCase(recording.fileName)
                } else {
                    return@withContext Result.retry()
                }
            }
        }
        Result.success()
    }

    private fun getStartOfDay(): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun getEndOfDay(): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
    }
}
