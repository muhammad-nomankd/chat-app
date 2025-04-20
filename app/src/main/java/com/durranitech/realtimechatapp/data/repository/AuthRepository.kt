

import androidx.lifecycle.MutableLiveData
import com.durranitech.realtimechatapp.data.model.User
import com.durranitech.realtimechatapp.viewModel.AuthUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository(
    private val firebaseAuth: FirebaseAuth, private val firestore: FirebaseFirestore
) {
    fun checkEmailAndAuthenticate(
        email: String,
        password: String,
        state: MutableLiveData<AuthUiState>
    )
    { state.postValue(AuthUiState.Loading)
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = firebaseAuth.currentUser?.uid ?: "User ID is null"
                val userEmail = firebaseAuth.currentUser?.email ?: ""
                val name = userEmail.substringBefore("@")
                val user = User(
                    userId = userId, userEmai = userEmail, userName = name, imageUrl = "", createdAt = System.currentTimeMillis()
                )
                firestore.collection("users").document(userId).set(user)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            state.postValue(AuthUiState.Success("You are Successfully Registered"))
                        } else {
                            state.postValue(AuthUiState.Error("Error saving user data"))
                        }
                    }.addOnFailureListener {
                        state.postValue(AuthUiState.Error("Error creating use Account:${it.message}"))
                    }
            }

        }.addOnFailureListener {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        state.postValue(AuthUiState.Success("You have Successfully Signed In"))
                    } else {
                        state.postValue(AuthUiState.Error("Error Signing In Please try again"))
                    }
                }.addOnFailureListener {
                    state.postValue(AuthUiState.Error("Enter valid email and password"))
                }
        }

    }

    fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }

    fun signOut(){
        firebaseAuth.signOut()
    }

    fun getCurrentUserId(): String{
        return firebaseAuth.currentUser?.uid ?: ""
    }
}
