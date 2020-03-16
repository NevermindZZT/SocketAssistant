package com.letter.serialport

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

    private val devices by lazy {
        val value = Vector<Driver>()
        val reader = LineNumberReader(FileReader("/proc/tty/drivers"))
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
            val devs = File("/dev")
            for (file in devs.listFiles()) {
                if (file.absolutePath.startsWith(root)) {
                    value.add(file)
                }
            }
            value
        }
    }
}