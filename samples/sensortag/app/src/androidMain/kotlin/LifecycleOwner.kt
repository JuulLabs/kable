package com.juul.sensortag

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> LifecycleOwner.observe(
    liveData: LiveData<T>,
    observer: (value: T) -> Unit
) {
    liveData.observe(this, Observer {
        observer.invoke(it)
    })
}
