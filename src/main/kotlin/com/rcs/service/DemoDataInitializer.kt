package com.rcs.service

import com.rcs.domain.Path
import com.rcs.domain.Pose
import com.rcs.domain.Vehicle
import com.rcs.domain.VehicleStatus
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class DemoDataInitializer(
    private val store: InMemoryRcsStore,
    private val roadNetGraph: RoadNetGraph
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        roadNetGraph.reset(
            seedPoses = listOf(
                Pose("A", 0.0, 0.0, "warehouse"),
                Pose("B", 4.0, 0.0, "warehouse"),
                Pose("C", 8.0, 0.0, "warehouse"),
                Pose("D", 8.0, 4.0, "workshop"),
                Pose("E", 4.0, 4.0, "workshop"),
                Pose("F", 0.0, 4.0, "charging")
            ),
            seedPaths = listOf(
                Path("P-AB", "A", "B", 4.0),
                Path("P-BC", "B", "C", 4.0),
                Path("P-CD", "C", "D", 4.0),
                Path("P-DE", "D", "E", 4.0),
                Path("P-EF", "E", "F", 4.0),
                Path("P-FA", "F", "A", 4.0),
                Path("P-BE", "B", "E", 4.0)
            )
        )

        store.reset(
            seedVehicles = listOf(
                Vehicle("AMR-A", "AMR A", "A", 82, VehicleStatus.IDLE, "warehouse"),
                Vehicle("AMR-B", "AMR B", "D", 66, VehicleStatus.IDLE, "workshop"),
                Vehicle("AMR-C", "AMR C", "F", 18, VehicleStatus.CHARGING, "charging")
            )
        )
    }
}
