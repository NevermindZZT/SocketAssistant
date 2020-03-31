package com.letter.serialport

import android.util.Log
import java.io.File
import java.io.FileReader
import java.io.LineNumberReader
import java.util.*

/**
 * 串口搜索类
 * @property devices Vector<Driver> 设备
 * @property deviceNameList MutableList<String> 设备名列表
 *
 * @author Letter(nevermindzzt@gmail.com)
 * @since 1.0.0
 */
class SerialPortFinder {

    companion object {
        private const val TAG = "SerialPortFinder"
    }

    private val devices by lazy {
        val value = Vector<Driver>()
        val file = File("/proc/tty/drivers")
        if (!file.canRead()) {
            try {
                val su = Runtime.getRuntime().exec("/system/bin/su")
                su.outputStream.write("chmod 666 ${file.absolutePath}\nexit\n".toByteArray())
            } catch (e: Exception) {
                Log.e(TAG, "", e)
            }
        }
        if (file.canRead()) {
            try {
                val reader = LineNumberReader(FileReader(file))
                var line: String ?= reader.readLine()
                while (line != null) {
                    val driverName = line?.substring(0, 0x15)?.trim()
                    val words = line?.split(" +".toRegex())
                    if (words != null) {
                        if (words.size >= 5
                            && words[words.size - 1].equals("serial")) {
                            value.add(Driver(driverName, words[words.size - 4]))
                        }
                    }
                    line = reader.readLine()
                }
                reader.close()
            } catch (e: Exception) {
                Log.e(TAG, "", e)
            }
        }
        value
    }

    val deviceNameList by lazy {
        val deviceNameList = mutableListOf<String>()
        for (driver in devices) {
            for (file in driver.devices) {
                deviceNameList.add(file.absolutePath)
            }
        }
        deviceNameList
    }


    class Driver constructor(var name: String?, val root: String) {
        val devices by lazy {
            val value = Vector<File>()
            try {
                val devs = File("/dev")
                for (file in devs.listFiles()) {
                    if (file.absolutePath.startsWith(root)) {
                        value.add(file)
                    }
                }
            } catch (e : Exception) {
                Log.w(TAG, "", e)
            }
            value
        }
    }
}