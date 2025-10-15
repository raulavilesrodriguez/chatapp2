package com.packt.chat.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.packt.ui.navigation.DeepLinks
import androidx.core.net.toUri
import com.packt.settings.domain.usecases.GetCurrentUserId
import com.packt.settings.domain.usecases.GetAndStoreFCMToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MessagingService: FirebaseMessagingService() {

    @Inject
    lateinit var getAndStoreFCMToken: GetAndStoreFCMToken
    @Inject
    lateinit var getCurrentUserId: GetCurrentUserId

    companion object {
        const val CHANNEL_ID = "Chat_message"
        const val CHANNEL_DESCRIPTION = "Recibe una notificación cuando se recibe un mensaje de chat"
        const val CHANNEL_TITLE = "Nuevo mensaje de chat"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Verificamos si hay un usuario logueado actualmente
        val userId = getCurrentUserId()
        if (userId.isNotEmpty()) {
            // Usamos una corrutina en el scope de IO para guardar el nuevo token en segundo plano.
            // No usamos viewModelScope porque esto no es un ViewModel.
            CoroutineScope(Dispatchers.IO).launch {
                getAndStoreFCMToken(userId)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if(remoteMessage.data.isNotEmpty()){
            val senderName = remoteMessage.data["senderName"]
            val messageContent = remoteMessage.data["content"]
            val chatId = remoteMessage.data["chatId"]

            val notificationId = chatId?.hashCode() ?: System.currentTimeMillis().toInt()

            if(chatId != null){
                showNotification(senderName, messageContent, chatId, notificationId)
            }
        }
    }

    private fun showNotification(senderName:String?, messageContent:String?, chatId:String, notificationId:Int){
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_TITLE,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
        }
        notificationManager.createNotificationChannel(channel)

        val deepLinkUrl = DeepLinks.chatRoute.replace("{chatId}", chatId)

        val intent = Intent(Intent.ACTION_VIEW, deepLinkUrl.toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Create a PendingIntent for the Intent
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        @SuppressLint("DiscouragedApi")
        val notificationIconResId = resources.getIdentifier(
            "notification", // 1. El nombre del recurso
            "drawable",     // 2. El tipo de recurso
            packageName     // 3. El paquete de la aplicación (esto resuelve la ambigüedad)
        )

        // build notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(notificationIconResId)
            .setContentTitle(senderName ?: "Nuevo Mensaje") // El título es el nombre del remitente
            .setContentText(messageContent) // El cuerpo es el contenido del mensaje
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // La acción al tocar la notificación
            .setAutoCancel(true) // La notificación desaparece al tocarla
            .build()

        notificationManager.notify(notificationId, notification)
    }
}