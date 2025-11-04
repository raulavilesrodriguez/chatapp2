// 1. Importaciones actualizadas para la sintaxis V2
import {onDocumentCreated} from "firebase-functions/v2/firestore";
import * as logger from "firebase-functions/logger";
import * as admin from "firebase-admin";

// Inicializa el SDK de administrador
admin.initializeApp();

/**
 * Esta función se dispara cada vez que se crea un nuevo documento de mensaje.
 * Utiliza la sintaxis V2 de Cloud Functions.
 */
// 2. Estructura de la función actualizada a V2
export const sendChatNotification = onDocumentCreated(
  "chats/{chatId}/messages/{messageId}",
  async (event) => {
    // El 'snapshot' y los 'params' ahora están dentro del objeto 'event'
    const snapshot = event.data;
    if (!snapshot) {
      logger.log("No se encontraron datos en el evento, función terminada.");
      return;
    }
    const messageData = snapshot.data();
    const params = event.params;

    const chatId = params.chatId;
    const senderId = messageData.senderId;
    const senderName = messageData.senderName;
    const content = messageData.content;

    logger.log(`Nuevo mensaje en chat ${chatId}. Procesando notificación...`);

    // 1. Obtener la lista de participantes del chat
    const chatDoc = await admin
      .firestore().collection("chats").doc(chatId).get();
    const chatData = chatDoc.data();
    if (!chatData?.participants) {
      logger.log("Chat o participantes no encontrados.");
      return;
    }
    const participants: string[] = chatData.participants;

    // 2. Encontrar a los destinatarios
    const recipients = participants.filter((uid) => uid !== senderId);
    if (recipients.length === 0) {
      logger.log("No hay destinatarios a quienes notificar.");
      return;
    }

    // 3. Para cada destinatario, obtener su token y enviar la notificación
    for (const recipientId of recipients) {
      const userDoc = await admin
        .firestore().collection("users").doc(recipientId).get();
      const userData = userDoc.data();

      if (userData?.fcmToken) {
        // No enviar notificación si el usuario ya está viendo el chat
        if (userData.activeInChatId === chatId) {
          logger.log(
            `Usuario ${recipientId} está activo en el chat. No se notifica.`
          );
          continue;
        }

        const fcmToken = userData.fcmToken;

        // 4. Construir el payload
        const payload = {
          token: fcmToken,
          data: {
            senderName: senderName || "Nuevo Mensaje",
            content: content || "Te ha enviado un mensaje.",
            chatId: chatId,
          },
        };

        logger.log(`Enviando notificación a ${recipientId}`);
        try {
          // 5. Enviar el mensaje
          await admin.messaging().send(payload);
        } catch (error) {
          logger.error(
            `Error al enviar notificación a ${recipientId}`,
            error,
          );
        }
      } else {
        logger.log(`Token FCM no encontrado para el usuario ${recipientId}`);
      }
    }
  },
);
