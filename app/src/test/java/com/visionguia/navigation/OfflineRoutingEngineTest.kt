package com.visionguia.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineRoutingEngineTest {
    @Test fun choosesShortestAccessibleRoute() {
        val nodes = (1L..4L).map { RouteNode(it, GeoPoint(-12.0, -77.0 + it / 1000.0)) }
        val route = OfflineRoutingEngine(nodes, listOf(
            RouteEdge(1, 2, 50.0), RouteEdge(2, 4, 50.0),
            RouteEdge(1, 3, 20.0, accessible = false), RouteEdge(3, 4, 20.0),
        )).route(1, 4).getOrThrow()
        assertEquals(listOf(1L, 2L, 4L), route.nodes.map(RouteNode::id))
        assertEquals(100.0, route.distanceMeters, 0.01)
    }

    @Test fun calculatesGeographicDistance() {
        val meters = OfflineRoutingEngine.distanceMeters(GeoPoint(-12.0464, -77.0428), GeoPoint(-12.0464, -77.0418))
        assertTrue(meters in 100.0..120.0)
    }
}
