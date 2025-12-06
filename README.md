# 💬 ChatApp

ChatApp is a real-time messaging application built with **Android (Kotlin)** 
and **Jetpack Compose**, using **Firebase** as the main backend.

---

## 🔥 Backend: Firebase

ChatApp uses **Firebase** for all backend operations, including:

- **Firebase Authentication** → User registration and login
- **Cloud Firestore** → Storage for users, chats, and messages
- **Firebase Cloud Messaging (FCM)** → Real-time push notifications
- **Firebase Storage** → Upload and management of images and files

---

## 🗂️ Firestore Database Structure

```plaintext
users/{uid}                      // Each user's profile
    ├── uid: "uid123"
    ├── name: "Raúl Avilés"
    ├── nameLowercase: "raul aviles"
    ├── number: "+593 91234 56780"
    ├── photoUrl: "https://example.com/profile_image.jpg"
    ├── activeInChatId: [chatId]
    └── conversations/{chatId}     // Subcollection of conversations
    ├   ├── chatId: "chatId123"
    ├   ├── clearedTimestamp: Timestamp
    ├   ├── blocked: true
    ├   └── updatedAt: Timestamp
    └── contacts/{uid}             // Subcollection of contacts
        ├── uid: "uid456"
        └── nickName: "raul a"


chats/{chatId}                   // A chat between 2 or more users
   ├── chatId: "chatId123"
   ├── participants: [uid1, uid2]    // Array of participant user IDs
   ├── lastMessage: "Hello"          // Last message (for chat list preview)
   ├── updatedAt: Timestamp
   ├── lastMessageSenderId: "uid1"
   ├── lastMessageType: "text"
   ├── unreadCount: {uid1: 5, uid2: 3} // Count of unread messages for each participant
   ├── createdAt: Timestamp
   ├── isGroup: true
   ├── groupName: "Group Raul Avilés"
   ├── groupPhotoUrl: "https://example.com/group_image.jpg"
   └── messages/{messageId}      // Subcollection of messages
          ├── senderId: uid1
          ├── text: "Hello"
          ├── timestamp: Timestamp
          └── type: "text" // or "image", "file", etc.
```

## 🚀 Technologies Used
- Kotlin
- Jetpack Compose
- Hilt (Dependency Injection)
- Firebase SDK
- Coroutines / Flow

# My CV here:
[Raúl Avilés Web Page](https://raulaviles.netlify.app/)