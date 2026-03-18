<template>
	<view class="page">
		<view class="card">
			<view class="title">x2-SerialHelper 串口通讯示例</view>

			<view class="section">
				<view class="label">当前设备串口列表</view>
				<button type="primary" size="mini" @click="loadPorts">刷新串口列表</button>
				<view class="port-list">
					<view v-for="(item, index) in ports" :key="index" class="port-item" @click="selectPort(item)">
						{{ item }}
					</view>
				</view>
			</view>

			<view class="section">
				<view class="label">串口路径</view>
				<input v-model="port" class="input" placeholder="请输入串口路径，例如 /dev/ttyS8" />
			</view>

			<view class="section row">
				<view class="field">
					<view class="label">波特率</view>
					<input v-model="baudRate" class="input" type="number" placeholder="9600" />
				</view>
				<view class="field">
					<view class="label">数据位</view>
					<input v-model="dataBits" class="input" type="number" placeholder="8" />
				</view>
			</view>

			<view class="section row">
				<view class="field">
					<view class="label">停止位</view>
					<input v-model="stopBits" class="input" type="number" placeholder="1" />
				</view>
				<view class="field">
					<view class="label">校验位</view>
					<input v-model="parity" class="input" type="number" placeholder="0" />
				</view>
			</view>

			<view class="section">
				<view class="label">流控</view>
				<input v-model="flowCon" class="input" type="number" placeholder="0" />
			</view>

			<view class="btn-row">
				<button type="primary" @click="initAndOpen">初始化并打开</button>
				<button type="warn" @click="closeSerial">关闭串口</button>
			</view>

			<view class="section">
				<view class="label">发送内容</view>
				<input v-model="sendText" class="input" placeholder="请输入发送内容，例如 A11T" />
			</view>

			<view class="btn-row">
				<button type="primary" @click="sendMessage">发送文本</button>
				<button @click="sendOpenCmd">发送开灯命令</button>
				<button @click="sendCloseCmd">发送关灯命令</button>
			</view>

			<view class="section">
				<view class="label">串口状态</view>
				<view class="status">{{ statusText }}</view>
			</view>

			<view class="section">
				<view class="label">接收数据</view>
				<scroll-view scroll-y class="log-box">
					<view v-for="(item, index) in receiveLogs" :key="'recv-' + index" class="log-item recv">
						{{ item }}
					</view>
				</scroll-view>
			</view>

			<view class="section">
				<view class="label">运行日志</view>
				<scroll-view scroll-y class="log-box">
					<view v-for="(item, index) in logs" :key="'log-' + index" class="log-item">
						{{ item }}
					</view>
				</scroll-view>
			</view>

			<view class="btn-row">
				<button size="mini" @click="clearLogs">清空日志</button>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		onMounted,
		onUnmounted
	} from "vue"
	import {
		GetAllDevicesPath,
		SerialHelper
	} from "@/uni_modules/x2-SerialHelper"

	let serialHelper = null

	const ports = ref([])
	const port = ref("/dev/ttyS8")
	const baudRate = ref("9600")
	const dataBits = ref("8")
	const stopBits = ref("1")
	const parity = ref("0")
	const flowCon = ref("0")
	const sendText = ref("A11T")
	const statusText = ref("未连接")
	const receiveLogs = ref([])
	const logs = ref([])

	const addLog = (text) => {
		const time = new Date().toLocaleTimeString()
		logs.value.unshift(`[${time}] ${text}`)
	}

	const addReceiveLog = (text) => {
		const time = new Date().toLocaleTimeString()
		receiveLogs.value.unshift(`[${time}] ${text}`)
	}

	const clearLogs = () => {
		logs.value = []
		receiveLogs.value = []
	}

	const loadPorts = () => {
		try {
			const list = GetAllDevicesPath()
			ports.value = list || []
			addLog(`获取串口列表成功：${JSON.stringify(ports.value)}`)
		} catch (e) {
			addLog(`获取串口列表失败：${e}`)
		}
	}

	const selectPort = (item) => {
		port.value = item
		addLog(`已选择串口：${item}`)
	}

	const destroySerial = () => {
		if (serialHelper) {
			try {
				serialHelper.closeAsync()
			} catch (e) {
				addLog(`关闭串口异常：${e}`)
			}
			try {
				serialHelper.release()
			} catch (e) {
				addLog(`释放串口异常：${e}`)
			}
			serialHelper = null
		}
		statusText.value = "未连接"
	}

	const initAndOpen = () => {
		try {
			if (!port.value) {
				addLog("串口路径不能为空")
				return
			}

			destroySerial()

			serialHelper = new SerialHelper(port.value, parseInt(baudRate.value))

			serialHelper.onData((text) => {
				console.log("收到串口数据:", text)
				addReceiveLog(text)
			})

			serialHelper.setDataBits(parseInt(dataBits.value))
			serialHelper.setStopBits(parseInt(stopBits.value))
			serialHelper.setParity(parseInt(parity.value))
			serialHelper.setFlowCon(parseInt(flowCon.value))

			serialHelper.openAsync()
			statusText.value = "串口已打开"
			addLog(
				`打开串口：port=${port.value}, baudRate=${baudRate.value}, dataBits=${dataBits.value}, stopBits=${stopBits.value}, parity=${parity.value}, flowCon=${flowCon.value}`
			)
		} catch (e) {
			statusText.value = "打开失败"
			addLog(`初始化或打开串口失败：${e}`)
		}
	}

	const closeSerial = () => {
		try {
			destroySerial()
			addLog("串口已关闭")
		} catch (e) {
			addLog(`关闭串口失败：${e}`)
		}
	}

	const sendMessage = () => {
		try {
			if (!serialHelper) {
				addLog("请先初始化并打开串口")
				return
			}
			serialHelper.sendAsync(sendText.value)
			addLog(`发送数据：${sendText.value}`)
		} catch (e) {
			addLog(`发送失败：${e}`)
		}
	}

	const sendOpenCmd = () => {
		sendText.value = "A11T"
		sendMessage()
	}

	const sendCloseCmd = () => {
		sendText.value = "A10T"
		sendMessage()
	}

	onMounted(() => {
		loadPorts()
	})

	onUnmounted(() => {
		destroySerial()
	})
</script>

<style>
	.page {
		padding: 24rpx;
		background: #f6f7fb;
		min-height: 100vh;
		box-sizing: border-box;
	}

	.card {
		background: #ffffff;
		border-radius: 20rpx;
		padding: 24rpx;
		box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
	}

	.title {
		font-size: 36rpx;
		font-weight: bold;
		margin-bottom: 24rpx;
		color: #222;
	}

	.section {
		margin-bottom: 24rpx;
	}

	.label {
		font-size: 28rpx;
		color: #444;
		margin-bottom: 12rpx;
	}

	.row {
		display: flex;
		gap: 20rpx;
	}

	.field {
		flex: 1;
	}

	.input {
		height: 76rpx;
		border: 1px solid #dcdfe6;
		border-radius: 12rpx;
		padding: 0 20rpx;
		background: #fff;
		font-size: 28rpx;
		box-sizing: border-box;
	}

	.btn-row {
		display: flex;
		flex-wrap: wrap;
		gap: 16rpx;
		margin-bottom: 24rpx;
	}

	.port-list {
		background: #fafafa;
		border-radius: 12rpx;
		padding: 12rpx;
		max-height: 240rpx;
		overflow: hidden;
	}

	.port-item {
		padding: 14rpx 12rpx;
		font-size: 26rpx;
		border-bottom: 1px solid #eee;
		color: #333;
	}

	.port-item:last-child {
		border-bottom: none;
	}

	.status {
		font-size: 28rpx;
		color: #007aff;
	}

	.log-box {
		height: 260rpx;
		background: #111827;
		border-radius: 12rpx;
		padding: 16rpx;
		box-sizing: border-box;
	}

	.log-item {
		font-size: 24rpx;
		color: #d1d5db;
		line-height: 1.6;
		margin-bottom: 8rpx;
		word-break: break-all;
	}

	.recv {
		color: #34d399;
	}
</style>