package com.my.hotel.server.commons

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.Point
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.util.GeometricShapeFactory

object SpatialUtils {
    fun createCircle(x: Double, y: Double, radius: Double): Geometry {
        val shapeFactory = GeometricShapeFactory()
        shapeFactory.setNumPoints(32)
        shapeFactory.setCentre(Coordinate(y, x))
        shapeFactory.setSize(radius * 2)
        return shapeFactory.createCircle()
    }
    fun isValidLatLang(latitude: Double?, longitude: Double?): Boolean {
        return latitude?.toInt() in -90 until 90 && longitude?.toInt() in -180 until 180
    }
    fun getPoint(latitude: Float?, longitude: Float?): Point? {
        if (latitude==null || longitude==null)
            return null
        val wellKnownText = "POINT (${longitude} ${latitude})"
        val fromText = WKTReader()
        var point: Point? = null
        try {
            point = fromText.read(wellKnownText) as Point
        }catch (e: Exception){
            e.stackTrace
        }
        return point
    }
}