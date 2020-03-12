package com.letter.serialport

import android.util.Log
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception

/**
 * 串口
 * @property mFd FileDescriptor? 文件描述符
 * @property inputStream FileInputStream? 输入流
 * @property outputStream FileOutputStream? 输出流
 *
 * @author Letter(nevermindzzt@gmail.com)
 * @since 1.0.0
 */
class SerialPort {

    companion object {
        private val TAG = "SerialPort"
    }

    private var mFd: FileDescriptor ?= null
    var inputStream: FileInputStream ?= null
    var outputStream: FileOutputStream ?= null

    init {
        System.loadLibrary("serial_port")
    }

    private external fun serialOpen(path: String,
                                    baudRate: Int,
                                    flags: Int,
                                    dataBits: Int,
                                    parity: String,
                                    stopBits: Int): FileDescriptor

    private external fun serialClose()

    /**
     * 打开串口
     * @param path String 串口路径
     * @param baudRate Int 波特率
     * @param flags Int 标志
     * @param dataBits Int 数据位
     * @param parity String 校验
     * @param stopBits Int 停止位
     * @return Boolean 是否连接成功
     */
    fun open(path: String,
             baudRate: Int = 115200,
             flags: Int = 0,
             dataBits: Int = 8,
             parity: String = "N",
             stopBits: Int = 1): Boolean {
        val device = File(path)
        if (!device.canRead() || !device.canWrite()) {
            try {
                val su = Runtime.getRuntime().exec("/system/bin/su")
                su.outputStream.write("chmod 666 ${device.absolutePath}\nexit\n".toByteArray())
            } catch (e: Exception) {
                Log.e(TAG, "", e)
                return false
            }
        }
        mFd = serialOpen(path, baudRate, flags, dataBits, parity, stopBits)
        if (mFd != null) {
            inputStream = FileInputStream(mFd!!)
            outputStream = FileOutputStream(mFd!!)
            return true
        }
        return false
    }

    /**
     * 关闭串口
     */
    fun close() {
        inputStream?.close()
        outputStream?.close()
        serialClose()
        mFd = null
    }

    /**
     * 是否已打开
     * @return Boolean
     */
    fun isOpen() = mFd != null
}