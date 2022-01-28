package com.example.rndbajajapplication.models

import android.graphics.drawable.Drawable

data class AppData(
    val appName: String,
    val totalDataUsage: Double,
    val appSize: Double,
    val appVersion: String,
    val appLogo: Drawable,
    val appOpenNumber: Int,
    val appInstallDate: String?,
    val appPackage: String
)