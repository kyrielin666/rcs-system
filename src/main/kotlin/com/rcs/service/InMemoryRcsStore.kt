package com.rcs.service

import com.rcs.domain.DispatchLog
import com.rcs.domain.TaskController
import com.rcs.domain.TransportOrder
import com.rcs.domain.TransportOrderStatus
import com.rcs.domain.Vehicle
import org.springframework.stereotype.Component

@Component
class InMemoryRcsStore {
    private val vehicles = linkedMapOf<String, Vehicle>()
    private val orders = linkedMapOf<String, TransportOrder>()
    private val controllers = linkedMapOf<String, TaskController>()
    private val dispatchLogs = mutableListOf<DispatchLog>()

    fun reset(seedVehicles: List<Vehicle>, seedOrders: List<TransportOrder> = emptyList()) {
        vehicles.clear()
        orders.clear()
        controllers.clear()
        dispatchLogs.clear()
        seedVehicles.forEach {
            vehicles[it.id] = it
            controllers[it.id] = TaskController(it.id)
        }
        seedOrders.forEach { orders[it.id] = it }
    }

    fun vehicles(): List<Vehicle> = vehicles.values.toList()

    fun orders(): List<TransportOrder> = orders.values.toList()

    fun pendingOrders(): List<TransportOrder> =
        orders.values.filter { it.status == TransportOrderStatus.CREATED }

    fun controllers(): List<TaskController> = controllers.values.toList()

    fun logs(): List<DispatchLog> = dispatchLogs.toList()

    fun addOrder(order: TransportOrder): TransportOrder {
        orders[order.id] = order
        return order
    }

    fun updateOrder(order: TransportOrder) {
        orders[order.id] = order
    }

    fun controller(vehicleId: String): TaskController? = controllers[vehicleId]

    fun vehicle(vehicleId: String): Vehicle? = vehicles[vehicleId]

    fun enqueue(vehicleId: String, orderId: String) {
        val controller = controllers.getValue(vehicleId)
        controllers[vehicleId] = controller.copy(queue = controller.queue + orderId)
    }

    fun addLog(log: DispatchLog) {
        dispatchLogs.add(0, log)
    }
}

