package com.rcs.domain

import java.time.Instant
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 车辆当前可被调度系统观察到的运行状态。
 */
enum class VehicleStatus {
    /** 空闲，可参与新订单候选计算和派单。 */
    IDLE,

    /** 正在执行任务，通常不参与新订单派单，除非后续支持插单或队列预排。 */
    BUSY,

    /** 正在充电或等待充电，第一阶段不参与普通搬运订单派单。 */
    CHARGING,

    /** 离线、故障或通信不可达，不能参与调度。 */
    OFFLINE
}

/**
 * 调度订单从创建到结束的生命周期状态。
 */
enum class TransportOrderStatus {
    /** 订单刚创建，还没有分配给任何车辆。 */
    CREATED,

    /** 调度器已选中车辆，订单已进入对应 TaskController 队列。 */
    DISPATCHED,

    /** 车辆已经开始执行该订单。 */
    RUNNING,

    /** 订单已完成，车辆到达目标点并释放相关运行资源。 */
    FINISHED,

    /** 订单被人工或系统取消，不再参与调度和执行。 */
    CANCELLED
}

/**
 * 候选项的评估结果。
 */
enum class CandidateStatus {
    /** 候选车辆满足基础约束，可以进入打分和排序。 */
    QUALIFIED,

    /** 候选车辆不满足约束，原因写入 Candidate.reasons。 */
    REJECTED
}

/**
 * 交通控制对路径或资源申请的许可结果。
 */
enum class PermitStatus {
    /** 申请通过，路径资源已被预留或允许通行。 */
    GRANTED,

    /** 申请被拒绝，通常表示路径冲突、资源占用或交通策略不允许。 */
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
