package tw0reck1.colorpickerapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import tw0reck1.colorpicker.ColorPickerView;
import tw0reck1.colorpicker.OnColorPickedListener;

/** @author Adrian Tworkowski */
public class MainActivity extends AppCompatActivity implements OnColorPickedListener {

    private View mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLayout = findViewById(R.id.layout);

        ColorPickerView colorPickerView1, colorPickerView2, colorPickerView3, colorPickerView4;

        colorPickerView1 = (ColorPickerView) findViewById(R.id.colorview1);
        colorPickerView1.setOnColorPickedListener(this);

        colorPickerView2 = (ColorPickerView) findViewById(R.id.colorview2);
        colorPickerView2.setOnColorPickedListener(this);

        colorPickerView3 = (ColorPickerView) findViewById(R.id.colorview3);
        colorPickerView3.setOnColorPickedListener(this);

        colorPickerView4 = (ColorPickerView) findViewById(R.id.colorview4);
        colorPickerView4.setOnColorPickedListener(this);
    }

    @Override
    public void onColorPicked(int color) {
        mLayout.setBackgroundColor(color);
    }

}