package com.rcs.service

import com.rcs.domain.Path
import com.rcs.domain.Pose
import com.rcs.domain.Route
import org.springframework.stereotype.Component
import java.util.PriorityQueue

@Component
class RoadNetGraph {
    private val poses = linkedMapOf<String, Pose>()
    private val paths = linkedMapOf<String, Path>()

    fun reset(seedPoses: List<Pose>, seedPaths: List<Path>) {
        poses.clear()
        paths.clear()
        seedPoses.forEach { poses[it.id] = it }
        seedPaths.forEach { paths[it.id] = it }
    }

    fun allPoses(): List<Pose> = poses.values.toList()

    fun allPaths(): List<Path> = paths.values.toList()

    fun pose(id: String): Pose? = poses[id]

    fun planRoute(fromPoseId: String, toPoseId: String): Route? {
        if (fromPoseId !in poses || toPoseId !in poses) return null
        if (fromPoseId == toPoseId) return Route(listOf(fromPoseId), emptyList(), 0.0)

        val distances = mutableMapOf(fromPoseId to 0.0)
        val previousPose = mutableMapOf<String, String>()
        val previousPath = mutableMapOf<String, String>()
        val queue = PriorityQueue(compareBy<NodeCost> { it.cost })
        queue.add(NodeCost(fromPoseId, 0.0))

        while (queue.isNotEmpty()) {
            val current = queue.poll()
            if (current.cost > (distances[current.poseId] ?: Double.MAX_VALUE)) continue
            if (current.poseId == toPoseId) break

            outgoing(current.poseId).forEach { edge ->
                val nextCost = current.cost + edge.path.distance
                if (nextCost < (distances[edge.toPoseId] ?: Double.MAX_VALUE)) {
                    distances[edge.toPoseId] = nextCost
                    previousPose[edge.toPoseId] = current.poseId
                    previousPath[edge.toPoseId] = edge.path.id
                    queue.add(NodeCost(edge.toPoseId, nextCost))
                }
            }
        }

        val total = distances[toPoseId] ?: return null
        val poseIds = ArrayDeque<String>()
        val pathIds = ArrayDeque<String>()
        var cursor = toPoseId
        poseIds.addFirst(cursor)
        while (cursor != fromPoseId) {
            val prev = previousPose[cursor] ?: return null
            pathIds.addFirst(previousPath[cursor] ?: return null)
            cursor = prev
            poseIds.addFirst(cursor)
        }
        return Route(poseIds.toList(), pathIds.toList(), total)
    }

    private fun outgoing(poseId: String): List<GraphEdge> =
        paths.values.flatMap { path ->
            buildList {
                if (path.fromPoseId == poseId) add(GraphEdge(path.toPoseId, path))
                if (path.bidirectional && path.toPoseId == poseId) add(GraphEdge(path.fromPoseId, path))
            }
        }

    private data class NodeCost(val poseId: String, val cost: Double)

    private data class GraphEdge(val toPoseId: String, val path: Path)
}

