package com.kiper.core.util

object Constants {

    const val TAG = "SyncService"
    const val CHANNEL_ID = "sync_service_channel"
    const val NOTIFICATION_ID = 1
    const val TYPE_SEND_REGISTER = "register"
    const val EVENT_TYPE_AUDIO = "audio"
    const val EVENT_TYPE_SCHEDULE = "schedule"
    const val EVENT_TYPE_PROCESS_AUDIO = "ProcessAudio"
    const val EVENT_CLOSE_CONNECTION = "close_connection"
    const val PERIODIC_CHECK_NETWORK_DELAY = 10_000L
    const val PERIODIC_CHECK = 20_000L
    const val DAY_IN_MILLIS = 86_400_000L // 1 day in milliseconds
    const val INVALID_FILE_SIZE = 3.1513671875
    const val TAG_WORKER = "AudioRecordWorker"
    const val TAG_UPLOAD_WORKER = "AudioUploadWorker"
    const val TAG_FUTURE_WORKER = "AudioFutureWorker"
    const val ERROR_UPLOAD = "error"
}