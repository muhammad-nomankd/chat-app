
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.durranitech.realtimechatapp.data.model.User
import com.durranitech.realtimechatapp.data.repository.UserRepository
import com.durranitech.realtimechatapp.data.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserSearchViewModel(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val _searchState = MutableStateFlow<Resource<List<User>>>(Resource.Idle())
    val searchState = _searchState.asStateFlow()

    private val _conversationState = MutableStateFlow<Resource<String>?>(null)
    val conversationState = _conversationState.asStateFlow()


    private val _selectedUserId = MutableStateFlow<String>("")
    val selectedUserId = _selectedUserId.asStateFlow()

    fun searchUsers(query: String) {
        if (query.length < 2) {
            _searchState.value = Resource.Success(emptyList())
            return
        }

        viewModelScope.launch {
            userRepository.searchUser(query).collect { result ->
                _searchState.value = result
            }
        }
    }

    fun createConversation(otherUserId: String) {
        // Set the selected user ID when creating a conversation
        _selectedUserId.value = otherUserId

        viewModelScope.launch {
            chatRepository.createOrGetConversation(otherUserId).collect { result ->
                _conversationState.value = result
            }
        }
    }
}