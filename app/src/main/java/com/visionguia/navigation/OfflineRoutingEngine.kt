package com.visionguia.navigation

import java.util.PriorityQueue
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class GeoPoint(val latitude: Double, val longitude: Double)
data class RouteNode(val id: Long, val point: GeoPoint)
data class RouteEdge(val from: Long, val to: Long, val meters: Double, val accessible: Boolean = true)
data class Route(val nodes: List<RouteNode>, val distanceMeters: Double)

/** Pure local Dijkstra routing over an imported OpenStreetMap-derived pedestrian graph. */
class OfflineRoutingEngine(nodes: Collection<RouteNode>, edges: Collection<RouteEdge>) {
    private val byId = nodes.associateBy(RouteNode::id)
    private val adjacency = edges.filter(RouteEdge::accessible).groupBy(RouteEdge::from)

    fun route(startId: Long, destinationId: Long): Result<Route> = runCatching {
        require(startId in byId && destinationId in byId) { "Nodo de inicio o destino desconocido" }
        val distances = mutableMapOf(startId to 0.0)
        val previous = mutableMapOf<Long, Long>()
        val queue = PriorityQueue(compareBy<Pair<Long, Double>> { it.second }).apply { add(startId to 0.0) }
        while (queue.isNotEmpty()) {
            val (node, distance) = queue.remove()
            if (distance != distances[node]) continue
            if (node == destinationId) break
            adjacency[node].orEmpty().forEach { edge ->
                val candidate = distance + edge.meters
                if (candidate < distances.getOrDefault(edge.to, Double.POSITIVE_INFINITY)) {
                    distances[edge.to] = candidate
                    previous[edge.to] = node
                    queue += edge.to to candidate
                }
            }
        }
        val distance = distances[destinationId] ?: error("No existe una ruta peatonal accesible")
        val ids = mutableListOf(destinationId)
        while (ids.last() != startId) ids += previous[ids.last()] ?: error("Ruta incompleta")
        Route(ids.asReversed().map { byId.getValue(it) }, distance)
    }

    companion object {
        fun distanceMeters(a: GeoPoint, b: GeoPoint): Double {
            val earth = 6_371_000.0
            val lat1 = Math.toRadians(a.latitude); val lat2 = Math.toRadians(b.latitude)
            val dLat = lat2 - lat1; val dLon = Math.toRadians(b.longitude - a.longitude)
            val h = sin(dLat / 2) * sin(dLat / 2) + cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
            return earth * 2 * atan2(sqrt(h), sqrt(1 - h))
        }
    }
}
