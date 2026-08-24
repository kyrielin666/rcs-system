package com.rcs.service

import com.rcs.domain.DispatchLog
import com.rcs.domain.TransportOrder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DispatchService(
    private val store: InMemoryRcsStore,
    private val schedulerEngine: SchedulerEngine
) {
    fun createOrder(request: CreateTransportOrderRequest): TransportOrder =
        store.addOrder(
            TransportOrder(
                id = request.id ?: "TO-${UUID.randomUUID().toString().take(8)}",
                sourcePoseId = request.sourcePoseId,
                targetPoseId = request.targetPoseId,
                priority = request.priority,
                requiredArea = request.requiredArea,
                minBatteryPercent = request.minBatteryPercent
            )
        )

    fun dispatchNext(): DispatchLog = schedulerEngine.tick()

    fun dispatch(order: TransportOrder): DispatchLog = schedulerEngine.schedule(order)
}

data class CreateTransportOrderRequest(
    val id: String? = null,
    val sourcePoseId: String,
    val targetPoseId: String,
    val priority: Int = 1,
    val requiredArea: String? = null,
    val minBatteryPercent: Int = 20
)
