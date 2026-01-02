package com.example.prototyp_1

import android.widget.NumberPicker
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

//Für die Uhr
@Composable
fun WheelPicker(
    modifier: Modifier = Modifier,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    //Ermöglicht Legacy Android-Widgets in Jetpack Compose zu integrieren
    AndroidView(
        modifier = Modifier.width(64.dp),
        factory = { context ->
            NumberPicker(context).apply {
                minValue = range.first
                maxValue = range.last
                wrapSelectorWheel = true

                setOnValueChangedListener { _, _, newVal ->
                    onValueChange(newVal)
                }
            }
        },
        update = { view ->

            view.setOnValueChangedListener(null)
            view.value = value
            view.setOnValueChangedListener { _, _, newVal ->
                onValueChange(newVal)
            }
        }
    )
}