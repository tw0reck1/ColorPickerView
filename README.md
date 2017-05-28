# ColorPickerView
Custom Android View for picking colors.
### Sample
```java
...
    mColorPickerView.setOnColorPickedListener(this);
    mColorPickerView.setStrokeColor(Color.WHITE);
    mColorPickerView.setStrokeWidth(10);
    mColorPickerView.setRadius(3); // sets the color count on radius
}

@Override
public void onColorPicked(int color) {
    mColorPickerView.setBackgroundColor(color);
}
```
![Screenshot](screenshot.png)
