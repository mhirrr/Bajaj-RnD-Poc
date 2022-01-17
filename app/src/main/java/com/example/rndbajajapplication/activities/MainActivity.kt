package com.example.rndbajajapplication.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.text.SimpleDateFormat
import java.io.File

import android.content.pm.ApplicationInfo

import android.content.pm.PackageManager
import android.os.RemoteException

import android.graphics.drawable.Drawable
import android.provider.Settings

import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.content.Context
import android.os.Process
import android.app.usage.NetworkStatsManager
import android.os.Build
import androidx.annotation.RequiresApi
import android.content.pm.PackageInfo
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rndbajajapplication.R
import com.example.rndbajajapplication.adapters.AppDataAdapter
import com.example.rndbajajapplication.models.AppData
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pm = packageManager

        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val it: MutableIterator<ApplicationInfo> = packages.iterator()
        while (it.hasNext()) {
            val ai = it.next()
            if (ai.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                it.remove()
            }
        }

        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), packageName
        )
        if (mode != AppOpsManager.MODE_ALLOWED) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        var applications: MutableList<AppData> = mutableListOf<AppData>()

        for (application in packages) {
            val applicationInfo = pm.getApplicationInfo(application.packageName, 0)
            val packageInfo = pm.getPackageInfo(application.packageName, 0)

            // Name of App
            val appName = getAppName(applicationInfo)

            // Package of App
            val appPackage = getAppPackage(applicationInfo)

            // Size of App
            val appSize = getAppSize(applicationInfo)

            // Version of App
            val appVersion = getAppVersion(packageInfo)

            // App Install Date
            val appInstallDate = getAppInstallDate(packageInfo)

            // Logo of App
            val appLogo = getAppLogo(packageInfo)

            // Data Usage of App
            val appDataUsage = getAppDataUsage(applicationInfo)

            val appData = AppData(appName, appDataUsage, appSize, appVersion, appLogo, 0, appInstallDate, appPackage)
            applications.add(appData)
        }

        var recyclerView: RecyclerView = findViewById(R.id.appDataRecyclerView)
        var layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = AppDataAdapter(applications)

        updateOpenNumber()
    }

    fun getAppName(applicationInfo: ApplicationInfo): String {
        val appName = applicationInfo.loadLabel(packageManager).toString()
        return appName
    }

    fun getAppPackage(applicationInfo: ApplicationInfo): String {
        val appPackage = applicationInfo.packageName
        return appPackage
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getAppDataUsage(applicationInfo: ApplicationInfo): Long {
        val uid = applicationInfo.uid
        val totalDataUsage = getPackageBytesMobile(uid) + getPackageBytesWifi(uid)
        return totalDataUsage;
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getPackageBytesMobile(packageUid: Int): Long {
        val networkStatsManager: NetworkStatsManager =
            (applicationContext.getSystemService(NETWORK_STATS_SERVICE) as NetworkStatsManager)
        var networkStats: NetworkStats = try {
            networkStatsManager.queryDetailsForUid(
                0,
                null,
                0,
                System.currentTimeMillis(),
                packageUid
            )
        } catch (e: RemoteException) {
            return -1
        }
        val bucket = NetworkStats.Bucket()
        networkStats?.getNextBucket(bucket)
        return bucket.rxBytes + bucket.txBytes
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getPackageBytesWifi(packageUid: Int): Long {
        val networkStatsManager: NetworkStatsManager =
            (applicationContext.getSystemService(NETWORK_STATS_SERVICE) as NetworkStatsManager)
        var networkStats: NetworkStats = try {
            networkStatsManager.queryDetailsForUid(
                1,
                "",
                0,
                System.currentTimeMillis(),
                packageUid
            )
        } catch (e: RemoteException) {
            return -1
        }
        val bucket = NetworkStats.Bucket()
        networkStats?.getNextBucket(bucket)
        return bucket.rxBytes + bucket.txBytes
    }

    fun getAppSize(applicationInfo: ApplicationInfo): Double {
        val file = File(applicationInfo.publicSourceDir)
        val appSize = file.length().toInt() * 9.537 * 0.00000001;
        return appSize
    }

    fun getAppVersion(packageInfo: PackageInfo): String {
        val appVersion: String =
            packageInfo.versionName
        return appVersion
    }

    fun getAppLogo(packageInfo: PackageInfo): Drawable {
        val ico: Drawable = packageManager.getApplicationIcon(packageInfo.packageName)
        return ico
    }

    fun getAppInstallDate(packageInfo: PackageInfo): String? {
        var installedTime = packageInfo.firstInstallTime
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        val dateString: String = simpleDateFormat.format(installedTime)
        return dateString
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateOpenNumber() {
        val sharedPreferences = getSharedPreferences(
            "open_data", Context.MODE_PRIVATE)
        val edit = sharedPreferences.edit()
        val todayDate = LocalDate.now().toString()

        var opened = sharedPreferences.getInt(todayDate, 0)
        opened += 1
        edit.putInt(todayDate, opened)
        edit.apply()
    }

    class DateIterator(val startDate: LocalDate,
                       val endDateInclusive: LocalDate,
                       val step: Long): Iterator<LocalDate> {
        private var currentDate = startDate

        @RequiresApi(Build.VERSION_CODES.O)
        override fun hasNext() = currentDate <= endDateInclusive

        @RequiresApi(Build.VERSION_CODES.O)
        override fun next(): LocalDate {
            val next = currentDate
            currentDate = currentDate.plusDays(step)
            return next
        }
    }

    class DateProgression(override val start: LocalDate,
                          override val endInclusive: LocalDate,
                          val step: Long = 1) :
        Iterable<LocalDate>, ClosedRange<LocalDate> {

        override fun iterator(): Iterator<LocalDate> =
            DateIterator(start, endInclusive, step)

        infix fun step(days: Long) = DateProgression(start, endInclusive, days)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun findTotalOpenNumber(startDate: LocalDate, endDate: LocalDate): Int {
        operator fun LocalDate.rangeTo(other: LocalDate) = DateProgression(this, other)
        val sharedPreferences = getSharedPreferences("open_data", Context.MODE_PRIVATE)

        var totalOpens = 0
        for(date in startDate..endDate step 1) {
            totalOpens += sharedPreferences.getInt(date.toString(), 0)
        }
        return totalOpens
    }
}

