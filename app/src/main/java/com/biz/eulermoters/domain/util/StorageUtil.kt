package com.biz.eulermoters.domain.util

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.os.Build
import android.os.Environment
import java.io.File
import java.util.*
import kotlin.collections.HashSet

class StorageUtil {
    private val EXTERNAL_STORAGE = System.getenv("EXTERNAL_STORAGE")
    private val SECONDARY_STORAGES = System.getenv("SECONDARY_STORAGE")
    private val EMULATED_STORAGE_TARGET = System.getenv("EMULATED_STORAGE_TARGET")

    @SuppressLint("SdCardPath")
    private val KNOWN_PHYSICAL_PATHS = arrayOf(
        "/storage/usb_storage",
        "/storage/sdcard0",
        "/storage/sdcard1",  //Motorola Xoom
        "/storage/extsdcard",  //Samsung SGS3
        "/storage/sdcard0/external_sdcard",  //User request
        "/mnt/extsdcard",
        "/mnt/sdcard/external_sd",  //Samsung galaxy family
        "/mnt/sdcard/ext_sd",
        "/mnt/external_sd",
        "/mnt/usb_storage/*",
        "/mnt/media_rw/*",
        "/mnt/media_rw/FCA2-AA69",  //4.4.2 on CyanogenMod S3
        "/mnt/media_rw/sdcard1",  //4.4.2 on CyanogenMod S3
        "/removable/microsd",  //Asus transformer prime
        "/mnt/emmc",
        "/storage/external_SD",  //LG
        "/storage/ext_sd",  //HTC One Max
        "/storage/removable/sdcard1",  //Sony Xperia Z1
        "/data/sdext",
        "/data/sdext2",
        "/data/sdext3",
        "/data/sdext4",
        "/sdcard1",  //Sony Xperia Z
        "/sdcard2",  //HTC One M8s
        "/storage/microsd" //ASUS ZenFone 2
    )

    fun getStorageDirectories(context: Context): Array<String> {
        val availableDirectoriesSet: HashSet<in String?> = HashSet()
        if (!TextUtils.isEmpty(EMULATED_STORAGE_TARGET)) {
            availableDirectoriesSet.add(emulatedStorageTarget)
        } else {
            availableDirectoriesSet.addAll(getExternalStorage(context))
        }
        Collections.addAll(availableDirectoriesSet, *allSecondaryStorages)
        val storagesArray = arrayOfNulls<String>(availableDirectoriesSet.size)
        return availableDirectoriesSet.toArray<String>(storagesArray)
    }

    private fun getExternalStorage(context: Context): Set<String> {
        val availableDirectoriesSet: MutableSet<String> = HashSet()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val files = getExternalFilesDirs(context, null)
            for (file in files) {
                if (file != null) {
                    val applicationSpecificAbsolutePath = file.absolutePath
                    val rootPath = applicationSpecificAbsolutePath.substring(0, applicationSpecificAbsolutePath.indexOf("Android/data"))
                    availableDirectoriesSet.add(rootPath)
                }
            }
        } else {
            if (TextUtils.isEmpty(EXTERNAL_STORAGE)) {
                availableDirectoriesSet.addAll(availablePhysicalPaths)
            } else {
                availableDirectoriesSet.add(EXTERNAL_STORAGE as String)
            }
        }
        return availableDirectoriesSet
    }

    private val emulatedStorageTarget: String
        get() {
            var rawStorageId = ""
            val path = Environment.getExternalStorageDirectory().absolutePath
            val folders = path.split(File.separator.toRegex()).toTypedArray()
            val lastSegment = folders[folders.size - 1]
            if (!TextUtils.isEmpty(lastSegment) && TextUtils.isDigitsOnly(lastSegment)) {
                rawStorageId = lastSegment
            }
            return if (TextUtils.isEmpty(rawStorageId)) {
                EMULATED_STORAGE_TARGET as String
            } else {
                EMULATED_STORAGE_TARGET as String + File.separator + rawStorageId
            }
        }


    private val allSecondaryStorages: Array<String?>
        get() = if (!TextUtils.isEmpty(SECONDARY_STORAGES)) {
            SECONDARY_STORAGES?.split(File.pathSeparator.toRegex())!!.toTypedArray()
        } else arrayOfNulls(0)


    private val availablePhysicalPaths: List<String>
        get() {
            val availablePhysicalPaths: MutableList<String> = ArrayList()
            for (physicalPath in KNOWN_PHYSICAL_PATHS) {
                val file = File(physicalPath)
                if (file.exists()) {
                    availablePhysicalPaths.add(physicalPath)
                }
            }
            return availablePhysicalPaths
        }

    private fun getExternalFilesDirs(context: Context, type: String?): Array<File?> {
        return context.getExternalFilesDirs(type)
    }
}