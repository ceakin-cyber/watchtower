package com.example.myapplication.ui.activitylog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ActivityLogViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the Activity Log screen. Here you can view all logged activities and incidents."
    }
    val text: LiveData<String> = _text
}