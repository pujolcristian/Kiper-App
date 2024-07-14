package com.kiper.app.service

import android.util.Log
import com.kiper.app.presentation.BaseViewModel
import com.kiper.core.domain.model.AudioRecording
import com.kiper.core.domain.model.ScheduleResponse
import com.kiper.core.domain.usecase.DeleteRecordingUseCase
import com.kiper.core.domain.usecase.GetDeviceSchedulesUseCase
import com.kiper.core.domain.usecase.GetRecordingsForDayUseCase
import com.kiper.core.domain.usecase.SaveRecordingUseCase
import com.kiper.core.domain.usecase.UploadAudioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val getDeviceSchedulesUseCase: GetDeviceSchedulesUseCase,
    private val getRecordingsForDayUseCase: GetRecordingsForDayUseCase,
    private val saveRecordingUseCase: SaveRecordingUseCase,
    private val deleteRecordingUseCase: DeleteRecordingUseCase,
    private val uploadAudioUseCase: UploadAudioUseCase,
) : BaseViewModel() {

    private val _schedules = MutableStateFlow<List<ScheduleResponse>?>(null)
    val schedules: StateFlow<List<ScheduleResponse>?> get() = _schedules

    private val _recordings = MutableStateFlow<List<AudioRecording>?>(null)
    val recordings: StateFlow<List<AudioRecording>?> get() = _recordings

    private val _uploadResult = MutableStateFlow<Boolean?>(null)
    val uploadResult: StateFlow<Boolean?> get() = _uploadResult

    private val _uploadProgress = MutableStateFlow<Int?>(null)
    val uploadProgress: StateFlow<Int?> get() = _uploadProgress

    private val _fileDeleted = MutableStateFlow<String?>(null)
    val fileDeleted: StateFlow<String?> get() = _fileDeleted

    private val _isOutOfSchedule = MutableStateFlow<Boolean?>(null)
    val isOutOfSchedule: StateFlow<Boolean?> get() = _isOutOfSchedule

    private var coroutineGlobalIsRunning = false

    fun fetchDeviceSchedules(deviceId: String) = launch {
        execute {
            Log.d("SyncViewModel", "Fetching schedules for device: $deviceId")
            getDeviceSchedulesUseCase(deviceId).collect {
                Log.d("SyncViewModel", "Schedules fetched: $it")
                _schedules.value = it
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
            Log.d("SyncViewModel", "Uploading audio files: $filePaths")
            val response = uploadAudioUseCase.execute(filePaths, deviceId, eventType)
            response.forEachIndexed { index, b ->
                if (b) {
                    fileNames?.get(index)?.let { deleteRecording(fileName = it) }
                    _uploadResult.value = true
                } else {
                    Log.d("SyncViewModel", "Upload failed for file ${fileNames?.get(index)}")
                }
            }
        }
    }

    fun getRecordingsForDay(startOfDay: Long, endOfDay: Long) = launch{
        execute {
            val recordings = getRecordingsForDayUseCase(startOfDay, endOfDay)
            _recordings.value = recordings
        }
    }

    fun saveRecording(recording: AudioRecording) = launch {
        execute {
            println("Saving recording VM: $recording")
            saveRecordingUseCase(recording)
        }
    }

    private fun deleteRecording(fileName: String) = launch {
        execute {
            val name = fileName.split(".").getOrNull(0) ?: ""
            deleteRecordingUseCase(name)
            _fileDeleted.value = fileName

        }
    }

    fun setOutOfSchedule(value: Boolean) {
        _isOutOfSchedule.value = value
    }
}