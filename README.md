Error Handling Improvements
Show clear error messages (e.g., invalid login, failed message send)

Add retry functionality for failed actions (sending messages, loading data)

UI 
Add smooth transitions (e.g., screen navigation, message send animation)

Improve dark mode visuals

Add loading indicators (login, chat load)


Performance Optimizations
Use pagination for chat history

Apply memoization where needed (Compose recompositions)

Profile UI performance & clean up heavy Composables


Day 1: Core Setup

Project setup & Firebase integration (Auth + Firestore)

Data models (User, Message, Conversation)

Email & Google authentication

Repository pattern + base architecture (MVVM, Hilt, Firebase)

Day 2: Main Features

Chat List screen (real-time updates from Firestore)

One-to-one chat screen with real-time messaging

Send/receive messages

https://github.com/user-attachments/assets/bf516c08-5c71-46dd-a9b5-df61ccb88b67



Firestore structure:

/users/{uid}
/conversations/{conversationId}
/conversations/{conversationId}/messages/{messageId}

Day 3: Polish & Optimizations

User search implementation

Error handling & retry buttons

UI polishing (dark mode, transitions, loading states)

Performance tuning (pagination, recomposition optimization)

Final testing on physical device

Write clean README.md with screenshots & setup instructions
