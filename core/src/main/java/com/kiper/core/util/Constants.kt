package com.kiper.core.util

object Constants {

    const val TAG = "SyncService"
    const val CHANNEL_ID = "sync_service_channel"
    const val NOTIFICATION_ID = 1
    const val TYPE_SEND_REGISTER = "register"
    const val TYPE_SEND_CONNECTION = "connection"
    const val EVENT_TYPE_AUDIO = "audio"
    const val EVENT_TYPE_SCHEDULE = "schedule"
    const val EVENT_TYPE_PROCESS_AUDIO = "ProcessAudio"
    const val EVENT_CLOSE_CONNECTION = "close_connection"
    const val PERIODIC_CHECK_NETWORK_DELAY = 300_000L
    const val PERIODIC_CHECK = 310_000L
    const val TAG_WORKER = "AudioRecordWorker"
    const val TAG_FUTURE_WORKER = "AudioFutureWorker"
    const val ERROR_UPLOAD = "error"
}