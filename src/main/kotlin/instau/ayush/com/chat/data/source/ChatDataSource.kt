package instau.ayush.com.chat.data.source

import instau.ayush.com.chat.resource.MessageEntity
import instau.ayush.com.model.chat.UserEntity
import kotlinx.coroutines.flow.Flow

interface ChatDataSource {
    suspend fun getFriendList(sender: Long): Flow<List<UserEntity>>
    suspend fun checkSessionAvailability(sender: Long, receiver: Long): Long?
    suspend fun createNewSession(sender: Long, receiver: Long): Long
    suspend fun insertMessage(messageEntity: MessageEntity)
    suspend fun getHistoryMessages(sender: Long, receiver: Long): Flow<List<MessageEntity>>
}