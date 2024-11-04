package com.kiper.app.service

import android.util.Log
import com.kiper.app.presentation.BaseViewModel
import com.kiper.core.domain.model.AudioRecording
import com.kiper.core.domain.model.ScheduleResponse
import com.kiper.core.domain.usecase.DeleteAllRecordingsUseCase
import com.kiper.core.domain.usecase.GetDeviceSchedulesUseCase
import com.kiper.core.domain.usecase.GetRecordingsForDayUseCase
import com.kiper.core.domain.usecase.SaveRecordingUseCase
import com.kiper.core.domain.usecase.UploadAudioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val getDeviceSchedulesUseCase: GetDeviceSchedulesUseCase,
    private val getRecordingsForDayUseCase: GetRecordingsForDayUseCase,
    private val saveRecordingUseCase: SaveRecordingUseCase,
    private val uploadAudioUseCase: UploadAudioUseCase,
    private val deleteAllRecordingsUseCase: DeleteAllRecordingsUseCase,
) : BaseViewModel() {

    private val _schedules = MutableSharedFlow<List<ScheduleResponse>?>()
    val schedules: SharedFlow<List<ScheduleResponse>?> get() = _schedules

    private val _recordings = MutableSharedFlow<List<AudioRecording>?>()
    val recordings: SharedFlow<List<AudioRecording>?> get() = _recordings

    private val _uploadResult = MutableSharedFlow<Boolean?>()
    val uploadResult: SharedFlow<Boolean?> get() = _uploadResult

    private val _fileDeleted = MutableStateFlow<List<String?>>(emptyList())
    val fileDeleted: StateFlow<List<String?>> get() = _fileDeleted


    fun fetchDeviceSchedules(deviceId: String) = launch {
        execute {
            getDeviceSchedulesUseCase(deviceId).collectLatest {
                Log.d("Schedules", "Schedules: $it")
                _schedules.emit(it)
            }
        }
    }

    fun uploadAudio(
        filePaths: List<String>,
        deviceId: String,
        eventType: String,
        fileNames: List<String>? = emptyList(),
    ) = launch {
        execute {
            val response = uploadAudioUseCase.execute(filePaths, deviceId, eventType)
            val list = response.mapIndexed() { index, b ->
                if (b) {
                    fileNames?.get(index)
                } else {
                    null
                }
            }
            deleteRecording(fileNames = list)
            _uploadResult.emit(true)
        }
    }

    fun getRecordingsForDay(startOfDay: Long, endOfDay: Long) = launch {
        execute {
            val recordings = getRecordingsForDayUseCase(startOfDay, endOfDay)
            Log.d("Recordings0", "Recordings: $recordings")
            _recordings.emit(recordings)
        }
    }

    fun saveRecording(recording: AudioRecording) = launch {
        execute {
            saveRecordingUseCase(recording)
        }
    }

    private fun deleteRecording(fileNames: List<String?>) = launch {
        execute {
            _fileDeleted.value = fileNames
        }
    }

    fun resetDataBase() = launch {
        execute {
            deleteAllRecordingsUseCase.invoke()
        }
    }
}