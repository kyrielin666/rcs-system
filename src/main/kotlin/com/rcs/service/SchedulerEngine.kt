package com.rcs.service

import com.rcs.domain.Candidate
import com.rcs.domain.CandidateStatus
import com.rcs.domain.DispatchLog
import com.rcs.domain.PermitStatus
import com.rcs.domain.TransportOrder
import com.rcs.domain.TransportOrderStatus
import com.rcs.domain.Vehicle
import com.rcs.domain.VehicleStatus
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SchedulerEngine(
    private val store: InMemoryRcsStore,
    private val trafficControl: TrafficControl
) {
    fun tick(): DispatchLog {
        val order = selectNextOrder()
            ?: return DispatchLog(
                id = newLogId(),
                orderId = "-",
                selectedVehicleId = null,
                candidates = emptyList(),
                message = "no pending transport order"
            ).also(store::addLog)

        return schedule(order)
    }

    fun schedule(order: TransportOrder): DispatchLog {
        val candidates = generateCandidates(order)
        val winner = chooseWinner(candidates)

        if (winner == null || winner.route == null) {
            return DispatchLog(
                id = newLogId(),
                orderId = order.id,
                selectedVehicleId = null,
                candidates = candidates,
                message = "no qualified candidate"
            ).also(store::addLog)
        }

        val permit = trafficControl.requestRoute(winner.vehicleId, order.id, winner.route)
        if (permit.status == PermitStatus.DENIED) {
            return DispatchLog(
                id = newLogId(),
                orderId = order.id,
                selectedVehicleId = null,
                candidates = candidates,
                message = permit.reason ?: "traffic control denied route"
            ).also(store::addLog)
        }

        store.enqueue(winner.vehicleId, order.id)
        store.updateOrder(
            order.copy(
                status = TransportOrderStatus.DISPATCHED,
                assignedVehicleId = winner.vehicleId
            )
        )

        return DispatchLog(
            id = newLogId(),
            orderId = order.id,
            selectedVehicleId = winner.vehicleId,
            candidates = candidates,
            message = "order dispatched to ${winner.vehicleId}"
        ).also(store::addLog)
    }

    private fun selectNextOrder(): TransportOrder? =
        store.pendingOrders()
            .sortedWith(compareByDescending<TransportOrder> { it.priority }.thenBy { it.createdAt })
            .firstOrNull()

    private fun generateCandidates(order: TransportOrder): List<Candidate> =
        store.vehicles().map { vehicle -> buildCandidate(order, vehicle) }

    private fun chooseWinner(candidates: List<Candidate>): Candidate? =
        candidates
            .filter { it.status == CandidateStatus.QUALIFIED }
            .minByOrNull { it.score ?: Double.MAX_VALUE }

    private fun buildCandidate(order: TransportOrder, vehicle: Vehicle): Candidate {
        val reasons = mutableListOf<String>()
        if (vehicle.status != VehicleStatus.IDLE) reasons += "vehicle is ${vehicle.status}"
        if (vehicle.batteryPercent < order.minBatteryPercent) reasons += "battery is below ${order.minBatteryPercent}%"
        if (order.requiredArea != null && vehicle.area != order.requiredArea) {
            reasons += "vehicle area ${vehicle.area} does not match ${order.requiredArea}"
        }

        val pickupRoute = trafficControl.planRoute(vehicle.id, vehicle.poseId, order.sourcePoseId)
        val deliveryRoute = trafficControl.planRoute(vehicle.id, order.sourcePoseId, order.targetPoseId)
        if (pickupRoute == null || deliveryRoute == null) reasons += "route is unreachable"

        val route = if (pickupRoute != null && deliveryRoute != null) {
            pickupRoute.copy(
                poseIds = pickupRoute.poseIds + deliveryRoute.poseIds.drop(1),
                pathIds = pickupRoute.pathIds + deliveryRoute.pathIds,
                distance = pickupRoute.distance + deliveryRoute.distance
            )
        } else {
            null
        }

        val queuePenalty = (store.controller(vehicle.id)?.queueDepth ?: 0) * 10.0
        val priorityBonus = order.priority * 2.0
        val batteryBonus = vehicle.batteryPercent / 20.0
        val score = route?.let { it.distance + queuePenalty - priorityBonus - batteryBonus }

        return Candidate(
            orderId = order.id,
            vehicleId = vehicle.id,
            status = if (reasons.isEmpty()) CandidateStatus.QUALIFIED else CandidateStatus.REJECTED,
            score = if (reasons.isEmpty()) score else null,
            route = route,
            reasons = reasons.ifEmpty { listOf("qualified") }
        )
    }

    private fun newLogId(): String = "DL-${UUID.randomUUID().toString().take(8)}"
}

