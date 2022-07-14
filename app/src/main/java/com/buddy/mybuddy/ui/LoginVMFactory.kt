package com.buddy.mybuddy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LoginVMFactory:ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
