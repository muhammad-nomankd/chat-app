
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.durranitech.realtimechatapp.data.model.User
import com.durranitech.realtimechatapp.data.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {
    private val _messagesState = MutableStateFlow<Resource<List<Message>>>(Resource.Loading())
    val messagesState = _messagesState.asStateFlow()

    private val _otherUser = MutableStateFlow<User?>(null)
    val otherUser = _otherUser.asStateFlow()

    private val _sendingState = MutableStateFlow<Resource<Message>?>(null)
    val sendingState = _sendingState.asStateFlow()

    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            _messagesState.value = Resource.Loading()
            try {
            chatRepository.getMessages(conversationId).collect { result ->
                _messagesState.value = result
            }} catch (e: Exception) {
                _messagesState.value = Resource.Error(e.message ?: "Failed to load messages")
            }
        }
    }

    fun loadUserInfo(userId: String) {
        viewModelScope.launch {
            try {
                val userDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()

                _otherUser.value = userDoc.toObject(User::class.java)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun sendMessage(conversationId: String, content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            chatRepository.sendMessage(conversationId, content).collect { result ->
                _sendingState.value = result
            }
        }
    }
}