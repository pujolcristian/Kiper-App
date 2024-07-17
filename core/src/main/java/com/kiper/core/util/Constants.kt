package com.kiper.core.util

object Constants {

    const val TAG = "SyncService"
    const val CHANNEL_ID = "sync_service_channel"
    const val NOTIFICATION_ID = 1
    const val EVENT_TYPE_AUDIO = "audio"
    const val EVENT_TYPE_PROCESS_AUDIO = "ProcessAudio"
    const val PERIODIC_CHECK_NETWORK_DELAY = 60_000L // 10 seconds
    const val DAY_IN_MILLIS = 86_400_000L // 1 day in milliseconds
    const val INVALID_FILE_SIZE = 3.1513671875
    const val TAG_WORKER = "AudioRecordWorker"
    const val TAG_UPLOAD_WORKER = "AudioUploadWorker"
    const val TAG_FUTURE_WORKER = "AudioFutureWorker"
    const val ERROR_UPLOAD = "error"
}