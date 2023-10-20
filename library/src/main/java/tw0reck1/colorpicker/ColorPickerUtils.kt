/*
 * Copyright 2017 Adrian Tworkowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tw0reck1.colorpicker

import android.graphics.Path
import android.graphics.PointF
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal object ColorPickerUtils {

    fun getAllShapePoints(
        width: Int,
        height: Int,
        shapeWidth: Float,
        shapeRadius: Float,
        diagonal: Int,
        count: Int
    ): List<PointF> {
        val pointsList: MutableList<PointF> = ArrayList()

        val startPoint = PointF(width / 2f - (diagonal - 1) * shapeWidth, (height / 2f))

        for (i in diagonal until count) {
            pointsList.addAll(getShapePointsRow(startPoint, shapeWidth, shapeRadius, i))
            startPoint.offset(shapeWidth / 2, 1.5f * shapeRadius)
        }
        for (i in count downTo diagonal) {
            pointsList.addAll(getShapePointsRow(startPoint, shapeWidth, shapeRadius, i))
            startPoint.offset(shapeWidth, 0f)
        }

        return pointsList
    }

    fun getCount(radius: Int): Int {
        val diameter = radius * 2 - 1
        return diameter + (radius until diameter).sumOf { i -> 2 * i }
    }

    private fun getShapePointsRow(
        startPoint: PointF,
        shapeWidth: Float,
        radius: Float,
        count: Int
    ): List<PointF> =
        (0 until count).map { i ->
            PointF(
                startPoint.x + i * shapeWidth / 2,
                startPoint.y - 1.5f * i * radius
            )
        }

    fun getShapePath(centerPoint: PointF, radius: Float): Path {
        var point = getPointOnCircle(centerPoint.x, centerPoint.y, radius, 0)

        val shape = Path()
        shape.fillType = Path.FillType.EVEN_ODD
        shape.moveTo(point.x, point.y)

        for (i in 60..360 step 60) {
            point = getPointOnCircle(centerPoint.x, centerPoint.y, radius, i)
            shape.lineTo(point.x, point.y)
        }
        shape.close()

        return shape
    }

    private fun getPointOnCircle(centerX: Float, centerY: Float, radius: Float, angle: Int): PointF {
        val resultX = (radius * cos((angle - 90f) * PI / 180f)).toFloat() + centerX
        val resultY = (radius * sin((angle - 90f) * PI / 180f)).toFloat() + centerY
        return PointF(resultX, resultY)
    }
}