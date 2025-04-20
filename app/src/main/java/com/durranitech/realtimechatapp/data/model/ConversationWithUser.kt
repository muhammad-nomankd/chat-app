package com.durranitech.realtimechatapp.data.model

import Conversation

data class ConversationWithUser(
    val conversation: Conversation,
    val user: User
)
