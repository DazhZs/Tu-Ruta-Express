package com.turutaexpress.data.repository

import com.turutaexpress.data.model.ServiceRequest
import com.turutaexpress.data.model.ServiceStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects


class ServiceRepository {

    private val db = FirebaseFirestore.getInstance()
    private val requestsCollection = db.collection("service_requests")

    suspend fun createServiceRequest(request: ServiceRequest): Result<String> {
        return try {
            val newDocument = requestsCollection.document()
            request.id = newDocument.id
            newDocument.set(request).await()
            Result.success(newDocument.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getServiceRequestStream(requestId: String): Flow<ServiceRequest?> {
        return requestsCollection.document(requestId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObject(ServiceRequest::class.java)
            }
    }

    suspend fun cancelServiceRequest(requestId: String): Result<Unit> {
        return try {
            requestsCollection.document(requestId)
                .update("status", ServiceStatus.CANCELADA)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPendingRequestsForDriver(driverId: String): Flow<List<ServiceRequest>> {
        return requestsCollection
            .whereEqualTo("driverId", driverId)
            .whereEqualTo("status", ServiceStatus.PENDIENTE)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ServiceRequest::class.java)
            }
    }

    fun getActiveRequestForDriver(driverId: String): Flow<ServiceRequest?> {
        return requestsCollection
            .whereEqualTo("driverId", driverId)
            .whereIn("status", listOf(ServiceStatus.ACEPTADA, ServiceStatus.EN_CAMINO))
            .limit(1)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.firstOrNull()?.toObject(ServiceRequest::class.java)
            }
    }

    suspend fun acceptRequest(requestId: String): Result<Unit> {
        return try {
            requestsCollection.document(requestId).update("status", ServiceStatus.ACEPTADA).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectRequest(requestId: String): Result<Unit> {
        return try {
            requestsCollection.document(requestId).update("status", ServiceStatus.CANCELADA).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateServiceStatus(requestId: String, newStatus: ServiceStatus): Result<Unit> {
        return try {
            requestsCollection.document(requestId).update("status", newStatus).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getServiceHistory(userId: String, userRole: String): Result<List<ServiceRequest>> {
        val fieldToQuery = if (userRole == "Cliente") "clientId" else "driverId"
        return try {
            val snapshot = requestsCollection
                .whereEqualTo(fieldToQuery, userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val history = snapshot.toObjects(ServiceRequest::class.java)
            Result.success(history)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}