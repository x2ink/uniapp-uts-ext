package com.x2

import android.util.Log
import java.util.concurrent.Executors
import kotlin.text.Charsets
import tp.xmaihh.serialport.SerialHelper
import tp.xmaihh.serialport.bean.ComBean
import io.dcloud.uts.console 
class Serial(sPort: String?, iBaudRate: Int) : SerialHelper(sPort, iBaudRate) {
	var onReceive: ((String) -> Unit)? = null

	private val ioExecutor = Executors.newSingleThreadExecutor()

	override fun onDataReceived(comBean: ComBean?) {
		if (comBean == null) return
		val text = String(comBean.bRec, Charsets.UTF_8).trim()
		onReceive?.invoke(text)
	}

	fun openAsync() {
		ioExecutor.execute {
			try {
				open()
			} catch (e: Throwable) {
				console.log(e.message)
			}
		}
	}

	fun closeAsync() {
		ioExecutor.execute {
			try {
				close()
			} catch (e: Throwable) {
				console.log(e.message)
			}
		}
	}

	fun sendAsync(text: String) {
		ioExecutor.execute {
			try {
				send(text.toByteArray())
			} catch (e: Throwable) {
			console.log(e.message)
			}
		}
	}

	fun sendTxtAsync(text: String) {
		ioExecutor.execute {
			try {
				sendTxt(text)
			} catch (e: Throwable) {
				console.log(e.message)
			}
		}
	}

	fun sendHexAsync(hex: String) {
		ioExecutor.execute {
			try {
				sendHex(hex)
			} catch (e: Throwable) {
				console.log(e.message)
			}
		}
	}
	fun release() {
	        ioExecutor.shutdown()
	    }
}