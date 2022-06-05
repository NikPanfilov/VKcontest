package com.example.vk

import android.content.Context
import android.content.SharedPreferences

class PinCodeProcessing(context: Context) {
    private val preferences: SharedPreferences
    private val hasAssignedSetting = "hasAssigned"
    private val truePinCodeSetting = "truePinCode"
    private val settingsFileName = "settings"

    init {
        preferences = context.getSharedPreferences(
            settingsFileName,
            Context.MODE_PRIVATE
        )
    }

    fun checkPinCode(pinCode:String):PinCheckResult {
        val truePinCode = preferences.getString(truePinCodeSetting, "no data")
        if (truePinCode == "no data") {
            assignPinCode(pinCode)
            return PinCheckResult.ASSIGNED
        }
        return if (truePinCode==pinCode)
            PinCheckResult.SUCCESS
        else
            PinCheckResult.FAILED

    }
    private fun assignPinCode(pinCode:String) {
        val e: SharedPreferences.Editor = preferences.edit()
        e.putBoolean(hasAssignedSetting, true)
        e.putString(truePinCodeSetting, pinCode)
        e.apply()
    }
}