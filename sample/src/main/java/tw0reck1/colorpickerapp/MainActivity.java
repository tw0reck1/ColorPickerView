package tw0reck1.colorpickerapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import tw0reck1.colorpickerview.ColorPickerView;
import tw0reck1.colorpickerview.OnColorPickedListener;

/** @author Adrian Tworkowski */
public class MainActivity extends AppCompatActivity implements OnColorPickedListener {

    private ColorPickerView mColorPickerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mColorPickerView = (ColorPickerView) findViewById(R.id.colorview);
        mColorPickerView.setOnColorPickedListener(this);
    }

    @Override
    public void onColorPicked(int color) {
        mColorPickerView.setBackgroundColor(color);
    }

}