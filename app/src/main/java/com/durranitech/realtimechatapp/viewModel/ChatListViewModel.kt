
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.durranitech.realtimechatapp.data.model.ConversationWithUser
import com.durranitech.realtimechatapp.data.model.User
import com.durranitech.realtimechatapp.data.utils.Resource
import com.durranitech.realtimechatapp.data.utils.Resource.Error
import com.durranitech.realtimechatapp.data.utils.Resource.Loading
import com.durranitech.realtimechatapp.data.utils.Resource.Success
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatListViewModel(private val chatRepository: ChatRepository) : ViewModel() {
    private val _conversationsState = MutableStateFlow<Resource<List<ConversationWithUser>>>(Resource.Idle())
    val conversationsState = _conversationsState.asStateFlow()

    private val _conversationUserData = MutableStateFlow<Map<String, User>>(emptyMap())
    val conversationUserData = _conversationUserData.asStateFlow()

    init {
        loadConversations()
    }


    fun loadConversations() {
        viewModelScope.launch {
            _conversationsState.value = Loading()
            try {
                chatRepository.getConversations().collect { resource ->
                    when (resource) {
                        is Success -> {
                            val conversations = resource.data
                            val conversationsWithUsersList = mutableListOf<ConversationWithUser>()

                            for (conversation in conversations) {
                                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                                val otherUserId = conversation.participants.find { it != currentUserId }

                                if (otherUserId != null) {
                                    try {
                                        val userDoc = FirebaseFirestore.getInstance()
                                            .collection("users")
                                            .document(otherUserId)
                                            .get()
                                            .await()

                                        val user = userDoc.toObject(User::class.java)
                                        if (user != null) {
                                            conversationsWithUsersList.add(ConversationWithUser(conversation, user))
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }

                            // Update state only once, after all conversations are processed
                            _conversationsState.value = Success(conversationsWithUsersList)
                        }
                        is Loading -> {
                            _conversationsState.value = Loading()
                        }
                        is Error -> {
                            _conversationsState.value = Error(resource.message ?: "Unknown error")
                        }
                        else -> {} // Handle Idle state
                    }
                }
            } catch (e: Exception) {
                _conversationsState.value = Error(e.message ?: "Failed to load conversations")
                e.printStackTrace()
            }
        }
    }

     fun loadUsersForConversations(conversations: List<Conversation>) {
        viewModelScope.launch {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

            conversations.forEach { conversation ->
                val otherUserId = conversation.participants.firstOrNull { it != currentUserId } ?: return@forEach

                try {
                    val userDoc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(otherUserId)
                        .get()
                        .await()

                    val user = userDoc.toObject(User::class.java) ?: return@forEach

                    _conversationUserData.update { userData ->
                        userData + (conversation.conversationId to user)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

