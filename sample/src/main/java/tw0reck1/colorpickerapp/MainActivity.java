package tw0reck1.colorpickerapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import java.util.Arrays;

import tw0reck1.colorpicker.ColorPickerSeekBar;
import tw0reck1.colorpicker.ColorPickerView;
import tw0reck1.colorpicker.OnColorPickedListener;

/** @author Adrian Tworkowski */
public class MainActivity extends Activity implements OnColorPickedListener {

    private View mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLayout = findViewById(R.id.layout);

        ColorPickerView colorPickerView1, colorPickerView2, colorPickerView3;
        ColorPickerSeekBar colorPickerSeekBar1, colorPickerSeekBar2, colorPickerSeekBar3;

        colorPickerView1 = (ColorPickerView) findViewById(R.id.colorview1);
        colorPickerView1.setOnColorPickedListener(this);

        colorPickerView2 = (ColorPickerView) findViewById(R.id.colorview2);
        colorPickerView2.setOnColorPickedListener(this);

        colorPickerView3 = (ColorPickerView) findViewById(R.id.colorview3);
        colorPickerView3.setOnColorPickedListener(this);

        colorPickerSeekBar1 = (ColorPickerSeekBar) findViewById(R.id.colorview4);
        colorPickerSeekBar1.setOnColorPickedListener(this);

        colorPickerSeekBar2 = (ColorPickerSeekBar) findViewById(R.id.colorview5);
        colorPickerSeekBar2.setOnColorPickedListener(this);

        colorPickerSeekBar3 = (ColorPickerSeekBar) findViewById(R.id.colorview6);
        colorPickerSeekBar3.setOnColorPickedListener(this);

        colorPickerView2.setColors(Arrays.asList(
                Color.DKGRAY, Color.WHITE, Color.WHITE, Color.GRAY, Color.DKGRAY, Color.DKGRAY, Color.WHITE
        ));
    }

    @Override
    public void onColorTouch(int color) {
        mLayout.setBackgroundColor(0x3fffffff & color);
    }

    @Override
    public void onColorClick(int color) {}

}