# x2-SerialHelper 使用教程

这个插件的作用是：

* 获取 Android 设备上的串口列表
* 打开指定串口
* 设置波特率、数据位、停止位、校验位、流控
* 发送串口指令
* 接收串口返回数据

---

# 一、适用平台

这个插件目前是 **Android 专用**。


---

# 二、前提条件

在使用前，先确认这几件事：

## 1. 设备真的有串口

例如：

* 工控机
* 开发板
* 带串口的 Android 设备

普通 Android 模拟器一般不适合测试真实串口通信。

## 2. 串口节点有权限

这个库本身**不会帮你申请 root**，串口设备文件必须可访问。

例如：

```bash
adb shell chmod 666 /dev/ttyS8
```

如果权限不够，串口可能打不开。

## 3. 参数必须在 `open()` 前设置

比如：

* 数据位
* 停止位
* 校验位
* 流控

都要在打开串口前设置，否则不会生效。

---

# 三、插件提供的能力

你现在封装出来的主要能力有：

* `GetAllDevicesPath()`：获取所有串口路径
* `new SerialHelper(path, baudRate)`：创建串口对象
* `setDataBits()`：设置数据位
* `setStopBits()`：设置停止位
* `setParity()`：设置校验位
* `setFlowCon()`：设置流控
* `openAsync()`：异步打开串口
* `closeAsync()`：异步关闭串口
* `sendAsync()`：异步发送字符串转字节
* `sendTxtAsync()`：异步发送 ASCII 文本
* `sendHexAsync()`：异步发送十六进制字符串
* `onData(callback)`：监听串口返回数据
* `release()`：释放线程资源

---

# 四、最简单的使用方式

## 1. 导入插件

```js
import {
  GetAllDevicesPath,
  SerialHelper
} from "@/uni_modules/x2-SerialHelper"
```

---

## 2. 先查看当前设备有哪些串口

```js
console.log(GetAllDevicesPath())
```

可能打印出：

```js
["/dev/ttyUSB3", "/dev/ttyUSB2", "/dev/ttyUSB1", "/dev/ttyUSB0", "/dev/ttyS8"]
```

然后你再决定用哪个串口，比如 `/dev/ttyS8`。

---

## 3. 创建串口对象

```js
const serialHelper = new SerialHelper("/dev/ttyS8", 9600)
```

这里的参数分别是：

* 第一个：串口路径
* 第二个：波特率

---

## 4. 监听接收数据

```js
serialHelper.onData((text) => {
  console.log("收到串口数据:", text)
})
```

当设备有返回内容时，就会走这里。

比如设备返回：

```text
OUT1 ON
```

页面会打印：

```js
收到串口数据: OUT1 ON
```

---

## 5. 设置串口参数

```js
serialHelper.setDataBits(8)
serialHelper.setStopBits(1)
```

如果还需要，也可以设置：

```js
serialHelper.setParity(0)
serialHelper.setFlowCon(0)
```

常见值可以先这样理解：

* `setDataBits(8)`：8 位数据位
* `setStopBits(1)`：1 位停止位
* `setParity(0)`：无校验
* `setFlowCon(0)`：无流控

这些设置必须在 `openAsync()` 前调用。

---

## 6. 打开串口

```js
serialHelper.openAsync()
```

这是异步打开，不会阻塞页面线程。

---

## 7. 发送数据

### 发送普通字符串字节

```js
serialHelper.sendAsync("A11T")
```

### 发送 ASCII 文本

```js
serialHelper.sendTxtAsync("A11T")
```

### 发送十六进制

```js
serialHelper.sendHexAsync("A11T")
```

如果你的设备协议要求结尾有回车换行，也可以这样发：

```js
serialHelper.sendTxtAsync("A11T\r\n")
```

---

# 五、完整示例

这是一个最推荐的写法：**页面创建一次串口对象，整个页面期间复用，页面销毁时再释放**。

```vue
<template>
  <view class="content">
    <button @click="openLight">开灯</button>
    <button @click="closeLight">关灯</button>
  </view>
</template>

<script setup>
import { onMounted, onUnmounted } from "vue"
import { GetAllDevicesPath, SerialHelper } from "@/uni_modules/x2-SerialHelper"

let serialHelper = null

onMounted(() => {
  console.log("串口列表:", GetAllDevicesPath())

  serialHelper = new SerialHelper("/dev/ttyS8", 9600)

  serialHelper.onData((text) => {
    console.log("收到串口数据:", text)
  })

  serialHelper.setDataBits(8)
  serialHelper.setStopBits(1)
  serialHelper.setParity(0)
  serialHelper.setFlowCon(0)

  serialHelper.openAsync()
})

const openLight = () => {
  if (!serialHelper) return
  serialHelper.sendAsync("A11T")
}

const closeLight = () => {
  if (!serialHelper) return
  serialHelper.sendAsync("A10T")
}

onUnmounted(() => {
  if (!serialHelper) return
  serialHelper.closeAsync()
  serialHelper.release()
  serialHelper = null
})
</script>
```

---

# 六、开灯关灯示例说明

如果你的串口协议是：

* `A11T`：开灯
* `A10T`：关灯

那就可以这样调用：

```js
serialHelper.sendAsync("A11T")
serialHelper.sendAsync("A10T")
```

如果设备会返回状态，例如：

* `OUT1 ON`
* `OUT1 OFF`

那么你在 `onData()` 里就能拿到。

---

# 七、接收数据是什么

底层收到的数据原本是字节数组，比如：

```text
[79, 85, 84, 49, 32, 79, 78, 13, 10]
```

在 Kotlin 里你已经把它转成了字符串：

```kotlin
val text = String(comBean.bRec, Charsets.UTF_8).trim()
```

所以前端拿到的是处理后的文本，不是原始字节。

例如：

```text
OUT1 ON
```

---

# 八、注意事项

## 1. 不要发完立刻 `release()`

错误示例：

```js
serialHelper.openAsync()
serialHelper.sendAsync("A11T")
serialHelper.release()
```

这样容易导致：

* 发送还没完成
* 设备还没来得及回数据
* 线程池已经结束

正确做法是：

* 页面里长期持有 `serialHelper`
* 页面卸载时再 `release()`

---

## 2. `release()` 后对象不能继续用

一旦调用：

```js
serialHelper.release()
```

这个对象内部线程池就关闭了，不能再继续：

* `openAsync()`
* `sendAsync()`
* `closeAsync()`

否则可能报：

```text
RejectedExecutionException
```

---

## 3. 有些设备不会回包

如果你发了指令，设备动作执行了，但 `onData()` 没有打印，不一定是插件有问题，也可能是：

* 设备本身不返回
* 指令格式不对
* 少了 `\r` 或 `\r\n`

这时候可以试试：

```js
serialHelper.sendTxtAsync("A11T\r\n")
```

---

## 4. 一个页面最好只创建一个串口对象

不建议每点一次按钮就：

* `new SerialHelper(...)`
* `openAsync()`
* `sendAsync()`
* `release()`

更推荐：

* 页面初始化时创建一次
* 后面一直复用
* 页面销毁时释放

---

# 九、常见问题

## 1. `GetAllDevicesPath()` 返回空数组

可能原因：

* 当前设备没有串口
* 你在模拟器上测试
* 串口节点权限不够

---

## 2. `console.log(serialHelper)` 打印 `{}` 是正常的吗

是正常现象。
因为这是 UTS 导出的类实例，跨到 uni-app 页面层后，`console.log` 不一定能完整展开对象结构。重点是方法能不能正常调用。

---

## 3. 为什么有时收不到 `onData()` 回调

常见原因：

* 设备本身没有回包
* 指令格式不对
* 打开串口后立刻释放了对象
* 串口权限不足
* 协议需要 `\r` / `\r\n`

---

## 4. 为什么必须先设置参数再打开串口

因为底层库本身就是这么设计的。串口打开后，再改这些参数通常不会生效。

---

# 十、推荐调用顺序

最推荐按这个顺序来：

```js
const serialHelper = new SerialHelper("/dev/ttyS8", 9600)

serialHelper.onData((text) => {
  console.log("收到串口数据:", text)
})

serialHelper.setDataBits(8)
serialHelper.setStopBits(1)
serialHelper.setParity(0)
serialHelper.setFlowCon(0)

serialHelper.openAsync()

serialHelper.sendAsync("A11T")
```

页面销毁时：

```js
serialHelper.closeAsync()
serialHelper.release()
```

