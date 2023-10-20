package tw0reck1.colorpickerapp

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import tw0reck1.colorpicker.ColorPickerSeekBar
import tw0reck1.colorpicker.ColorPickerView

/** @author Adrian Tworkowski
 */
class MainActivity : Activity(),
    ColorPickerView.OnColorPickListener,
    ColorPickerSeekBar.OnColorPickListener {

    private lateinit var layout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        layout = findViewById(R.id.layout)

        listOf<ColorPickerView>(
            findViewById(R.id.colorview1),
            findViewById(R.id.colorview2),
            findViewById(R.id.colorview3),
        )
            .forEach { colorPickerView ->
                colorPickerView.setOnColorPickListener(this)
            }

        listOf<ColorPickerSeekBar>(
            findViewById(R.id.colorview4),
            findViewById(R.id.colorview5),
            findViewById(R.id.colorview6),
        )
            .forEach { colorPickerView ->
                colorPickerView.setOnColorPickListener(this)
            }

        findViewById<ColorPickerView>(R.id.colorview2)
            .setColors(
                listOf(
                    Color.DKGRAY,
                    Color.WHITE,
                    Color.WHITE,
                    Color.GRAY,
                    Color.DKGRAY,
                    Color.DKGRAY,
                    Color.WHITE
                )
            )
    }

    override fun onColorTouch(color: Int) {
        layout.setBackgroundColor(0x3fffffff and color)
    }

    override fun onColorClick(color: Int) {}

    override fun onColorDrag(color: Int) {
        layout.setBackgroundColor(0x3fffffff and color)
    }

    override fun onColorPick(color: Int) {}
}
