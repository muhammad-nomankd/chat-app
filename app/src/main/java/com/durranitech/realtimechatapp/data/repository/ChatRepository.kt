import com.durranitech.realtimechatapp.data.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getConversations(): Flow<Resource<List<Conversation>>> = flow {
        emit(Resource.Loading())
        try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

            val snapshotListener = callbackFlow {
                val listener = firestore.collection("conversations")
                    .whereArrayContains("participants", currentUserId)
                    .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }

                        val conversations = snapshot?.toObjects(Conversation::class.java) ?: emptyList()
                        trySend(Resource.Success(conversations))
                    }

                awaitClose { listener.remove() }
            }

            emitAll(snapshotListener)
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load conversations"))
        }
    }

    fun getMessages(conversationId: String): Flow<Resource<List<Message>>> = flow {
        emit(Resource.Loading())
        try {
            val snapshotListener = callbackFlow {
                val listener = firestore.collection("conversations")
                    .document(conversationId)
                    .collection("messages")
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }

                        val messages = snapshot?.toObjects(Message::class.java) ?: emptyList()
                        trySend(Resource.Success(messages))
                    }

                awaitClose { listener.remove() }
            }

            emitAll(snapshotListener)
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load messages"))
        }
    }

    fun sendMessage(conversationId: String, content: String): Flow<Resource<Message>> = flow {
        emit(Resource.Loading())
        try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val messageId = UUID.randomUUID().toString()
            val timestamp = System.currentTimeMillis()

            val message = Message(
                messageId = messageId,
                senderId = currentUserId,
                content = content,
                timestamp = timestamp
            )


            firestore.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .document(messageId)
                .set(message)
                .await()

            firestore.collection("conversations")
                .document(conversationId)
                .update(
                    mapOf(
                        "lastMessage" to content,
                        "lastMessageSenderId" to currentUserId,
                        "lastMessageTimestamp" to timestamp
                    )
                )
                .await()

            emit(Resource.Success(message))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to send message"))
        }
    }

    fun createOrGetConversation(otherUserId: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

            val query = firestore.collection("conversations")
                .whereArrayContains("participants", currentUserId)
                .get()
                .await()

            val existingConversation = query.documents.find { doc ->
                val participants = doc.get("participants") as? List<*>
                participants?.containsAll(listOf(currentUserId, otherUserId)) == true
            }

            if (existingConversation != null) {
                emit(Resource.Success(existingConversation.id))
            } else {
                val conversationId = UUID.randomUUID().toString()
                val conversation = Conversation(
                    conversationId = conversationId,
                    participants = listOf(currentUserId, otherUserId),
                    lastMessageTimestamp = System.currentTimeMillis()
                )

                firestore.collection("conversations")
                    .document(conversationId)
                    .set(conversation)
                    .await()

                emit(Resource.Success(conversationId))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to create conversation"))
        }
    }
}