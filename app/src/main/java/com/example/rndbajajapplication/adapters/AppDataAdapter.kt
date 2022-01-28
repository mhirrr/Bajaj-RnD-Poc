package com.example.rndbajajapplication.adapters

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.widget.TextViewCompat
import androidx.core.widget.TextViewCompat.setAutoSizeTextTypeWithDefaults
import androidx.recyclerview.widget.RecyclerView
import com.example.rndbajajapplication.models.AppData
import com.example.rndbajajapplication.R

class AppDataAdapter(private var applications: MutableList<AppData>) : RecyclerView.Adapter<AppDataAdapter.AppDataHolder>() {

    @RequiresApi(Build.VERSION_CODES.O)
    inner class AppDataHolder(v: View): RecyclerView.ViewHolder(v) {
        var itemImage: ImageView
        var itemName: TextView
        var itemPackage: TextView
        var itemVersion: TextView
        var itemInstallDate: TextView
        var itemDataUsage: TextView
        var itemSize: TextView

        init {
            itemImage = v.findViewById(R.id.appLogo)
            itemName = v.findViewById(R.id.appName)
            itemPackage = v.findViewById(R.id.appPackageName)
            itemVersion = v.findViewById(R.id.appVersion)
            itemInstallDate = v.findViewById(R.id.appInstallDate)
            itemDataUsage = v.findViewById(R.id.appDataUsage)
            itemSize = v.findViewById(R.id.appSize)

            itemPackage.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)

            v.setOnClickListener {
                Log.d("HERE", itemName.text.toString())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppDataHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.app_data_layout, parent, false)
        return AppDataHolder(v)
    }

    override fun onBindViewHolder(holder: AppDataHolder, position: Int) {
        val application = applications[position]
        holder.itemImage.setImageDrawable(application.appLogo)
        holder.itemName.text = application.appName
        holder.itemPackage.text = application.appPackage
        holder.itemInstallDate.text = application.appInstallDate
        holder.itemDataUsage.text = application.totalDataUsage.toString()
        holder.itemVersion.text = application.appVersion
        holder.itemSize.text = application.appSize.toString()
    }

    override fun getItemCount(): Int {
        return applications.size
    }
}