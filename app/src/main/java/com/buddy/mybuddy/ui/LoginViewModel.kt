package com.buddy.mybuddy.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
    var _loginstatus = MutableLiveData<Boolean>()


    val loginstatus: LiveData<Boolean>
        get() = _loginstatus

}