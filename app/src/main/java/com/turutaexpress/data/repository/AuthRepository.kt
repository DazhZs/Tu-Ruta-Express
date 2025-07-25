package com.turutaexpress.data.repository

import com.turutaexpress.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(user: User, password: String): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(user.email, password).await()
            val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user es nulo")
            val userWithUid = user.copy(uid = firebaseUser.uid)
            db.collection("users").document(firebaseUser.uid).set(userWithUid).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserData(uid: String): Result<User> {
        return try {
            val document = db.collection("users").document(uid).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Usuario no encontrado en Firestore."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }
            val token = task.result
            val userId = getCurrentUser()?.uid
            if (userId != null && token != null) {
                db.collection("users").document(userId).update("fcmToken", token)
            }
        }
    }

    suspend fun requestVerificationCodeLocally(): Result<String> {
        val userId = getCurrentUser()?.uid ?: return Result.failure(Exception("Usuario no logueado"))
        return try {
            val code = (100000..999999).random().toString()
            db.collection("users").document(userId).update("verificationCode", code).await()
            Result.success(code)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitVerificationCode(code: String): Result<Unit> {
        val userId = getCurrentUser()?.uid ?: return Result.failure(Exception("Usuario no logueado"))
        val userRef = db.collection("users").document(userId)

        return try {
            val document = userRef.get().await()
            val correctCode = document.getString("verificationCode")

            if (correctCode != null && correctCode == code) {
                userRef.update(mapOf(
                    "isPhoneVerified" to true,
                    "verificationCode" to FieldValue.delete()
                )).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Código incorrecto."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}