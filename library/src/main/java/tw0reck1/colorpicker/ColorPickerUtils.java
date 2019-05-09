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
package tw0reck1.colorpicker;

import android.graphics.Path;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

class ColorPickerUtils {

    private ColorPickerUtils() {}

    static List<PointF> getAllShapePoints(int width, int height, float shapeWidth, float shapeRadius, int diagonal, int count) {
        List<PointF> pointsList = new ArrayList<>();

        PointF startPoint = new PointF(width / 2 - (diagonal - 1) * shapeWidth, height / 2);

        for (int i = diagonal; i < count; i++) {
            pointsList.addAll(getShapePointsRow(startPoint, shapeWidth, shapeRadius, i));
            startPoint.offset(shapeWidth / 2, 1.5f * shapeRadius);
        }
        for (int i = count; i >= diagonal; i--) {
            pointsList.addAll(getShapePointsRow(startPoint, shapeWidth, shapeRadius, i));
            startPoint.offset(shapeWidth, 0);
        }

        return pointsList;
    }

    static int getCount(int radius) {
        int diameter = radius * 2 - 1;
        int count = 0;

        for (int i = radius; i < diameter; i++) {
            count += 2 * i;
        }
        count += diameter;

        return count;
    }

    private static List<PointF> getShapePointsRow(PointF startPoint, float shapeWidth, float radius, int count) {
        List<PointF> pointsList = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            PointF point = new PointF(startPoint.x + i * shapeWidth / 2,
                    startPoint.y - 1.5f * i * radius);
            pointsList.add(point);
        }

        return pointsList;
    }

    static Path getShapePath(PointF centerPoint, float radius) {
        int angleJump = 60;

        PointF point = getPointOnCircle(centerPoint.x, centerPoint.y, radius, 0);

        Path shape = new Path();
        shape.setFillType(Path.FillType.EVEN_ODD);
        shape.moveTo(point.x, point.y);

        for (int i = 0; i < 360; i += angleJump) {
            point = getPointOnCircle(centerPoint.x, centerPoint.y, radius, i);
            shape.lineTo(point.x, point.y);
        }
        shape.close();

        return shape;
    }

    private static PointF getPointOnCircle(float centerX, float centerY, float radius, int angle) {
        float resultX = (float) (radius * Math.cos((angle - 90) * Math.PI / 180F)) + centerX;
        float resultY = (float) (radius * Math.sin((angle - 90) * Math.PI / 180F)) + centerY;

        return new PointF(resultX, resultY);
    }

}