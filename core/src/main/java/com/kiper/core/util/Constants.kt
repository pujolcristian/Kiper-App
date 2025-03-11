package com.kiper.core.util

object Constants {
    // Permisos y códigos de solicitud
    const val REQUEST_CODE_ENABLE_ADMIN = 11
    const val REQUEST_CODE_PERMISSIONS = 10
    const val REQUEST_UNKNOWN_SOURCES_PERMISSION_CODE = 12

    // Intents y acciones
    const val ACTION_SHOW_CUSTOM_DIALOG = "com.kiper.app.SHOW_CUSTOM_DIALOG"
    const val EXTRA_MESSAGE = "message"
    const val EXTRA_SHOW_DIALOG = "showDialog"

    // Nombres de paquetes y componentes
    const val LAUNCHER_PACKAGE = "com.sgtc.launcher"
    const val LAUNCHER_ACTIVITY = "com.sgtc.launcher.Launcher"

    // Intents y acciones
    const val ACTION_CLOSE_APP = "com.kiper.app.CLOSE_APP_ACTION"


    // Tags para logs
    const val TAG_MAIN_ACTIVITY = "MainActivity"
    const val TAG_SYNC_SERVICE = "SyncService"
    const val TAG_DELETE_FILES = "DeleteFiles"
    const val TAG_NOTIFICATION_SOUND = "NotificationSound"
    const val TAG_RECORDING_SERVICE = "RecordingService"

    // WorkManager
    const val WORK_SERVICE_MONITOR = "ServiceMonitorWork"

    // Notificaciones
    const val CHANNEL_ID = "sync_service_channel"
    const val CHANNEL_NAME = "Sync Service"
    const val NOTIFICATION_ID = 1

    // Eventos WebSocket
    const val TYPE_SEND_REGISTER = "register"
    const val TYPE_SEND_CONNECTION = "connection"
    const val EVENT_TYPE_AUDIO = "audio"
    const val EVENT_TYPE_SCHEDULE = "schedule"
    const val EVENT_TYPE_PROCESS_AUDIO = "ProcessAudio"
    const val EVENT_TYPE_CAPSULE = "educationalCapsule"
    const val EVENT_CLOSE_CONNECTION = "close_connection"

    // Worker Tags
    const val TAG_WORKER = "AudioRecordWorker"
    const val TAG_FUTURE_WORKER = "AudioFutureWorker"

    // Configuración de red
    const val PERIODIC_CHECK_NETWORK_DELAY = 300_000L
    const val PERIODIC_CHECK = 310_000L

    const val ERROR_UPLOAD = "error"
}
