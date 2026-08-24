package com.rcs.controller

import com.rcs.domain.DispatchLog
import com.rcs.domain.Path
import com.rcs.domain.Pose
import com.rcs.domain.TaskController
import com.rcs.domain.TrafficReservation
import com.rcs.domain.TransportOrder
import com.rcs.domain.Vehicle
import com.rcs.service.CreateTransportOrderRequest
import com.rcs.service.DispatchService
import com.rcs.service.InMemoryRcsStore
import com.rcs.service.RoadNetGraph
import com.rcs.service.TrafficControl
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/rcs")
class RcsDemoController(
    private val store: InMemoryRcsStore,
    private val roadNetGraph: RoadNetGraph,
    private val trafficControl: TrafficControl,
    private val dispatchService: DispatchService
) {
    @GetMapping("/snapshot")
    fun snapshot(): RcsSnapshotResponse =
        RcsSnapshotResponse(
            vehicles = store.vehicles(),
            orders = store.orders(),
            controllers = store.controllers(),
            poses = roadNetGraph.allPoses(),
            paths = roadNetGraph.allPaths(),
            reservations = trafficControl.reservations(),
            dispatchLogs = store.logs()
        )

    @PostMapping("/orders")
    fun createOrder(@RequestBody request: CreateTransportOrderRequest): TransportOrder =
        dispatchService.createOrder(request)

    @PostMapping("/dispatch")
    fun dispatchNext(): DispatchLog = dispatchService.dispatchNext()
}

data class RcsSnapshotResponse(
    val vehicles: List<Vehicle>,
    val orders: List<TransportOrder>,
    val controllers: List<TaskController>,
    val poses: List<Pose>,
    val paths: List<Path>,
    val reservations: List<TrafficReservation>,
    val dispatchLogs: List<DispatchLog>
)

