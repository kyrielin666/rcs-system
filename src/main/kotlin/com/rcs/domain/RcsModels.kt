package com.rcs.domain

import java.time.Instant
import kotlin.math.pow
import kotlin.math.sqrt

enum class VehicleStatus {
    IDLE,
    BUSY,
    CHARGING,
    OFFLINE
}

enum class TransportOrderStatus {
    CREATED,
    DISPATCHED,
    RUNNING,
    FINISHED,
    CANCELLED
}

enum class CandidateStatus {
    QUALIFIED,
    REJECTED
}

enum class PermitStatus {
    GRANTED,
    DENIED
}

data class Pose(
    val id: String,
    val x: Double,
    val y: Double,
    val area: String
) {
    fun distanceTo(other: Pose): Double =
        sqrt((x - other.x).pow(2) + (y - other.y).pow(2))
}

data class Path(
    val id: String,
    val fromPoseId: String,
    val toPoseId: String,
    val distance: Double,
    val bidirectional: Boolean = true
)

data class Route(
    val poseIds: List<String>,
    val pathIds: List<String>,
    val distance: Double
)

data class Vehicle(
    val id: String,
    val name: String,
    val poseId: String,
    val batteryPercent: Int,
    val status: VehicleStatus,
    val area: String
)

data class TransportOrder(
    val id: String,
    val sourcePoseId: String,
    val targetPoseId: String,
    val priority: Int,
    val requiredArea: String? = null,
    val minBatteryPercent: Int = 20,
    val status: TransportOrderStatus = TransportOrderStatus.CREATED,
    val assignedVehicleId: String? = null,
    val createdAt: Instant = Instant.now()
)

data class TaskController(
    val vehicleId: String,
    val queue: List<String> = emptyList()
) {
    val queueDepth: Int
        get() = queue.size
}

data class Candidate(
    val orderId: String,
    val vehicleId: String,
    val status: CandidateStatus,
    val score: Double?,
    val route: Route?,
    val reasons: List<String>
)

data class TrafficReservation(
    val resourceId: String,
    val vehicleId: String,
    val orderId: String,
    val reservedAt: Instant = Instant.now()
)

data class PermitResult(
    val status: PermitStatus,
    val reservation: TrafficReservation?,
    val reason: String?
)

data class DispatchLog(
    val id: String,
    val orderId: String,
    val selectedVehicleId: String?,
    val candidates: List<Candidate>,
    val message: String,
    val createdAt: Instant = Instant.now()
)

