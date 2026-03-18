<template>
	<view class="content">
		<image class="logo" src="/static/logo.png"></image>
		<view class="text-area">
			<button @click="openLight()">开灯</button>
			<button @click="closeLight()">关灯</button>
		</view>
	</view>
</template>

<script setup>
	import {
		GetAllDevicesPath,
		SerialHelper
	} from "@/uni_modules/x2-SerialHelper"
	const openLight = () => {
		var serialHelper = new SerialHelper("/dev/ttyS8", 9600)
		serialHelper.onData((text) => {
			console.log("收到串口数据:", text)
		})
		serialHelper.setDataBits(8)
		serialHelper.setStopBits(1)
		serialHelper.openAsync()
		serialHelper.sendAsync("A11T")
		setTimeout(() => {
			serialHelper.release()
		}, 3000)
	}
	const closeLight = () => {
		var serialHelper = new SerialHelper("/dev/ttyS8", 9600)
		serialHelper.onData((text) => {
			console.log("收到串口数据:", text)
		})
		serialHelper.setDataBits(8)
		serialHelper.setStopBits(1)
		serialHelper.openAsync()
		serialHelper.sendAsync("A10T")
		setTimeout(() => {
			serialHelper.release()
		}, 3000)
	}
</script>