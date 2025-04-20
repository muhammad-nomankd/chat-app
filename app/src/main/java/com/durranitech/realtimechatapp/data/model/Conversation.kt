data class Conversation(
    val conversationId: String = "",
    val participants: List<String> = listOf(),
    val lastMessage: String = "",
    val lastMessageSenderId: String = "",
    val lastMessageTimestamp: Long = 0
)