package com.rcs.service

import com.rcs.domain.PermitResult
import com.rcs.domain.PermitStatus
import com.rcs.domain.Route
import com.rcs.domain.TrafficReservation
import org.springframework.stereotype.Service

interface TrafficControl {
    fun planRoute(vehicleId: String, fromPoseId: String, toPoseId: String): Route?
    fun requestRoute(vehicleId: String, orderId: String, route: Route): PermitResult
    fun release(vehicleId: String, resourceId: String)
    fun reservations(): List<TrafficReservation>
}

@Service
class InMemoryTrafficControl(
    private val roadNetGraph: RoadNetGraph
) : TrafficControl {
    private val reservations = linkedMapOf<String, TrafficReservation>()

    override fun planRoute(vehicleId: String, fromPoseId: String, toPoseId: String): Route? =
        roadNetGraph.planRoute(fromPoseId, toPoseId)

    override fun requestRoute(vehicleId: String, orderId: String, route: Route): PermitResult {
        val occupied = route.pathIds.firstOrNull { pathId ->
            reservations[pathId]?.let { it.vehicleId != vehicleId } ?: false
        }
        if (occupied != null) {
            return PermitResult(
                status = PermitStatus.DENIED,
                reservation = reservations[occupied],
                reason = "path $occupied has been reserved"
            )
        }

        route.pathIds.forEach { pathId ->
            reservations[pathId] = TrafficReservation(pathId, vehicleId, orderId)
        }
        return PermitResult(
            status = PermitStatus.GRANTED,
            reservation = route.pathIds.firstOrNull()?.let { reservations[it] },
            reason = null
        )
    }

    override fun release(vehicleId: String, resourceId: String) {
        if (reservations[resourceId]?.vehicleId == vehicleId) {
            reservations.remove(resourceId)
        }
    }

    override fun reservations(): List<TrafficReservation> = reservations.values.toList()
}
