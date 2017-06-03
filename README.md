# ColorPickerView
Custom Android View for picking colors.
### Sample
```java
...
    mColorPickerView.setOnColorPickedListener(this);
    mColorPickerView.setStrokeColor(Color.BLACK);
    mColorPickerView.setStrokeWidth(10);
    mColorPickerView.setRadius(2); // sets the color count on radius
    mColorPickerView.setColors(Arrays.asList(
            Color.DKGRAY, Color.WHITE, Color.WHITE, Color.GRAY, Color.DKGRAY, Color.DKGRAY, Color.WHITE
    ));
}

@Override
public void onColorPicked(int color) {
    mColorPickerView.setBackgroundColor(color);
}
```
![Screenshot](screenshot.png)
