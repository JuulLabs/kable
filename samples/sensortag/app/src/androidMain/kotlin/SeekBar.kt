package com.juul.sensortag

import android.widget.SeekBar

fun SeekBar.onStopTracking(action: SeekBar.() -> Unit) {
    val listener = object : SeekBar.OnSeekBarChangeListener {
        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {
            action.invoke(seekBar)
        }
    }
    setOnSeekBarChangeListener(listener)
}
