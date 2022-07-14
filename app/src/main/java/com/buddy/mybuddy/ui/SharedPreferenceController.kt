package com.buddy.mybuddy.ui

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.lang.reflect.Type



    class SharedPreferenceController private constructor() {
        private val PreferenceName = "com.rhewum"
        private fun getSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
        }

        fun getValue(context: Context, key: String?): String? {
            return getSharedPreferences(context).getString(key, "")
        }

        fun getIntValue(context: Context, key: String?): Int {
            return getSharedPreferences(context).getInt(key, 0)
        }

        fun setValue(context: Context, key: String?, value: String?) {
            val editor = getSharedPreferences(context).edit()
            editor.putString(key, value)
            editor.commit()
            editor.apply()
        }

        fun setIntValue(context: Context, key: String?, value: Int) {
            val editor = getSharedPreferences(context).edit()
            editor.putInt(key, value)
            editor.commit()
        }

        fun getBoolean(context: Context, key: String?): Boolean {
            return getSharedPreferences(context).getBoolean(key, false)
        }

        fun getBoolean(context: Context, key: String?, defaultValue: Boolean): Boolean {
            return getSharedPreferences(context).getBoolean(key, defaultValue)
        }

        fun setBooleanValue(context: Context, key: String?, value: Boolean) {
            val editor = getSharedPreferences(context).edit()
            editor.putBoolean(key, value)
            editor.commit()
        }

        fun clear(context: Context) {
            val editor = getSharedPreferences(context).edit()
            editor.clear()
            editor.commit()
        }

        fun updateSession(context: Context, status: Boolean) {
            setBooleanValue(context, "session", status)
        }

        fun readSession(context: Context): Boolean {
            return getBoolean(context, "session")
        }

        fun storeEmailId(context: Context, string: String?) {
            setValue(context, "emailId", string)
        }

        fun getEmailId(context: Context): String? {
            return getValue(context, "emailId")
        }

        fun storeToken(context: Context, string: String?) {
            setValue(context, "token", string)
        }

        fun getToken(context: Context): String? {
            return getValue(context, "token")
        }

        fun storeFcmToken(context: Context, string: String?) {
            setValue(context, "fcmtoken", string)
        }

        fun getFcmToken(context: Context): String? {
            return getValue(context, "fcmtoken")
        }

        fun storemacAddress(context: Context, string: String?) {
            setValue(context, "mac", string)
        }

        fun getmacAddress(context: Context): String? {
            return getValue(context, "mac")
        }

        fun storePiNumber(context: Context, string: String?) {
            setValue(context, "pi", string)
        }

        fun getPiNumber(context: Context): String? {
            return getValue(context, "pi")
        }

        fun setNetworkState(context: Context, status: Boolean) {
            setBooleanValue(context, "network", status)
        }

        fun getNetworkState(context: Context): Boolean {
            return getBoolean(context, "network")
        }

        fun setCheckState(context: Context, status: Boolean) {
            setBooleanValue(context, "check", status)
        }

        fun getCheckState(context: Context): Boolean {
            return getBoolean(context, "check", true)
        }

        fun storeSubscriptionModel(context: Context, subscriptionModel: SubscriptionModel?) {
            setValue(context, "subscribeModel", Gson().toJson(subscriptionModel))
        }

        fun getSubscriptionModel(context: Context): SubscriptionModel {
            val type: Type = object : TypeToken<SubscriptionModel?>() {}.getType()
            return Gson().fromJson(getValue(context, "subscribeModel"), type)
        }
        fun setAppVersion(context: Context, value: String?) {
            setValue(context, "appVersion", value)
        }

        fun getAppVersion(context: Context): String? {
            return getValue(context, "appVersion")
        }

        companion object {
            private var controller: SharedPreferenceController? = null
            val instance: SharedPreferenceController?
                get() {
                    if (controller == null) {
                        controller = SharedPreferenceController()
                    }
                    return controller
                }
        }
    }
