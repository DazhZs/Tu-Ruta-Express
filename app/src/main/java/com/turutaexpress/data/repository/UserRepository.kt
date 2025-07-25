package com.turutaexpress.data.repository

import android.content.Context
import android.net.Uri
import com.turutaexpress.data.model.MototaxiProfile
import com.turutaexpress.data.model.Site
import com.turutaexpress.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import java.io.File
import java.io.FileOutputStream


data class MototaxistaDisponible(
    val user: User,
    val profile: MototaxiProfile
)

class UserRepository {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val mototaxiProfilesCollection = db.collection("mototaxi_profiles")
    private val sitesCollection = db.collection("sites")

    suspend fun updateMototaxiStatus(uid: String, isActive: Boolean): Result<Unit> {
        return try {
            mototaxiProfilesCollection.document(uid).update("active", isActive).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvailableMototaxistas(colonia: String): Flow<List<MototaxistaDisponible>> {
        return mototaxiProfilesCollection
            .whereEqualTo("active", true)
            .snapshots()
            .map { snapshot ->
                val availableMototaxistas = mutableListOf<MototaxistaDisponible>()
                for (doc in snapshot.documents) {
                    val profile = doc.toObject<MototaxiProfile>()
                    if (profile != null) {
                        val userDoc = usersCollection.document(profile.uid).get().await()
                        val user = userDoc.toObject<User>()
                        if (user != null && user.colonia == colonia) {
                            availableMototaxistas.add(MototaxistaDisponible(user, profile))
                        }
                    }
                }
                availableMototaxistas
            }
    }

    suspend fun getMototaxiProfile(uid: String): Result<MototaxiProfile> {
        return try {
            val document = mototaxiProfilesCollection.document(uid).get().await()
            val profile = document.toObject<MototaxiProfile>()
            if (profile != null) {
                Result.success(profile)
            } else {
                val newProfile = MototaxiProfile(uid = uid)
                mototaxiProfilesCollection.document(uid).set(newProfile).await()
                Result.success(newProfile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(uid: String, name: String, address: String): Result<Unit> {
        return try {
            usersCollection.document(uid).update(mapOf(
                "name" to name,
                "address" to address
            )).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveProfileImageLocally(context: Context, uid: String, imageUri: Uri): Result<String> {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return Result.failure(Exception("No se pudo abrir la imagen."))

            val directory = context.filesDir
            val file = File(directory, "$uid.jpg")
            val outputStream = FileOutputStream(file)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            val localPath = file.absolutePath
            usersCollection.document(uid).update("profileImageUrl", localPath).await()
            Result.success(localPath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSite(site: Site): Result<Unit> {
        return try {
            sitesCollection.document(site.id).set(site).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSiteByAdmin(adminUid: String): Result<Site?> {
        return try {
            val snapshot = sitesCollection.whereEqualTo("adminUid", adminUid).limit(1).get().await()
            val site = snapshot.documents.firstOrNull()?.toObject(Site::class.java)
            Result.success(site)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDriversForSite(siteId: String): Result<List<User>> {
        return try {
            val snapshot = mototaxiProfilesCollection.whereEqualTo("siteId", siteId).get().await()
            val driverUids = snapshot.documents.map { it.id }

            if (driverUids.isEmpty()) {
                return Result.success(emptyList())
            }

            val usersSnapshot = usersCollection.whereIn("uid", driverUids).get().await()
            val users = usersSnapshot.toObjects(User::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun activateMembership(driverUid: String, months: Int): Result<Unit> {
        return try {
            val expiryCalendar = java.util.Calendar.getInstance()
            expiryCalendar.add(java.util.Calendar.MONTH, months)
            val expiryTimestamp = com.google.firebase.Timestamp(expiryCalendar.time)

            mototaxiProfilesCollection.document(driverUid).update(mapOf(
                "membershipStatus" to "ACTIVE",
                "membershipExpiryDate" to expiryTimestamp
            )).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitRating(
        requestId: String,
        ratedUserId: String,
        ratedUserRole: String,
        rating: Float,
        comment: String?,
        isSubmittedByClient: Boolean
    ): Result<Unit> {
        val requestRef = db.collection("service_requests").document(requestId)
        val ratedUserRef = db.collection(if (ratedUserRole == "Mototaxista") "mototaxi_profiles" else "users").document(ratedUserId)

        return try {
            db.runTransaction { transaction ->
                val ratedUserSnapshot = transaction.get(ratedUserRef)

                val oldRating: Float
                val oldTotalTrips: Int

                if (ratedUserRole == "Mototaxista") {
                    val profile = ratedUserSnapshot.toObject(MototaxiProfile::class.java)!!
                    oldRating = profile.rating
                    oldTotalTrips = profile.totalTrips
                } else {
                    val user = ratedUserSnapshot.toObject(User::class.java)!!
                    oldRating = user.ratingAsClient
                    oldTotalTrips = user.totalTripsAsClient
                }

                val newTotalTrips = oldTotalTrips + 1
                val newRating = ((oldRating * oldTotalTrips) + rating) / newTotalTrips

                if (ratedUserRole == "Mototaxista") {
                    transaction.update(ratedUserRef, "rating", newRating, "totalTrips", newTotalTrips)
                } else {
                    transaction.update(ratedUserRef, "ratingAsClient", newRating, "totalTripsAsClient", newTotalTrips)
                }

                if (isSubmittedByClient) {
                    transaction.update(requestRef, "clientRating", rating, "clientComment", comment, "clientHasRated", true)
                } else {
                    transaction.update(requestRef, "driverRating", rating, "driverComment", comment, "driverHasRated", true)
                }
                null
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}