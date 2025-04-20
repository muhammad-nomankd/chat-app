package com.durranitech.realtimechatapp.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.durranitech.realtimechatapp.data.model.User
import com.durranitech.realtimechatapp.data.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID


class UserRepository() {

    val auth = FirebaseAuth.getInstance()
    val fireStore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()


    fun searchUser(query: String): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())

        try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val snapshot = fireStore.collection("users").get().await()

            val users = snapshot.toObjects(User::class.java).filter {
                it.userId != currentUserId && (it.userName.contains(
                    query, ignoreCase = true
                ) || (it.userEmai.contains(query, ignoreCase = true)))
            }
            emit(Resource.Success(users))

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to Search users"))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getUserProfile(userId: String): Resource<User> {
        return try {
            val documentSnapshot = fireStore.collection("users")
                .document(userId)
                .get()
                .await()

            if (documentSnapshot.exists()) {
                val user = documentSnapshot.toObject(User::class.java)
                Resource.Success(user)
            } else {
                Resource.Error("User not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        } as Resource<User>
    }

    suspend fun updateUserProfile(user: User): Resource<String> {
        return try {
            fireStore.collection("users")
                .document(user.userId)
                .set(user)
                .await()

            Resource.Success("Profile updated successfully")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update profile")
        }
    }

    suspend fun uploadProfileImage(userId: String, imageUri: coil3.Uri): Resource<String> {
        return try {
            val storageRef: StorageReference = storage.reference
            val imageRef: StorageReference = storageRef.child("profile_images/$userId/${UUID.randomUUID()}")

            // Convert coil3.Uri to android.net.Uri
            val androidUri = android.net.Uri.parse(imageUri.toString())

            val uploadTask: UploadTask.TaskSnapshot = imageRef.putFile(androidUri).await()
            val downloadUrl: String = imageRef.downloadUrl.await().toString()

            Resource.Success(downloadUrl)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to upload image")
        }
    }
}

