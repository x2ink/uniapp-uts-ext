
# x2-AppList

一个用于 **uni-app x / uni-app UTS Android** 的应用列表插件，用来获取设备已安装应用信息，并支持按分类筛选、按关键词搜索。

## 功能特性

- 获取已安装应用列表
- 支持按应用类型筛选
  - 全部应用：`all`
  - 用户应用：`user`
  - 系统应用：`system`
- 支持按应用名 / 包名搜索
- 返回应用图标、名称、包名、版本、安装包路径、文件大小等信息

---

## 插件方法

### 1. 获取安装应用列表

```ts
GetInstalledAppList(filter: string | null): Array<AppItem>
````

#### 参数

| 参数名    | 类型               | 说明                                                     |
| ------ | ---------------- | ------------------------------------------------------ |
| filter | `string \| null` | 应用筛选类型，可传 `all` / `user` / `system`，传 `null` 时默认 `all` |

#### 示例

```ts
import { GetInstalledAppList } from "@/uni_modules/x2-AppList"

const list = GetInstalledAppList("all")
console.log(list)
```

---

### 2. 搜索安装应用

```ts
SearchInstalledApps(keyword: string, filter: string | null): Array<AppItem>
```

#### 参数

| 参数名     | 类型               | 说明                                                     |
| ------- | ---------------- | ------------------------------------------------------ |
| keyword | `string`         | 搜索关键字，同时匹配应用名和包名                                       |
| filter  | `string \| null` | 应用筛选类型，可传 `all` / `user` / `system`，传 `null` 时默认 `all` |

#### 示例

```ts
import { SearchInstalledApps } from "@/uni_modules/x2-AppList"

const result = SearchInstalledApps("微信", "user")
console.log(result)
```

---

## 返回数据结构

### AppItem

```ts
type AppItem = {
  icon: string
  name: string
  packageName: string
  version: string
  path: string
  flags: number
  size: number
}
```

#### 字段说明

| 字段名         | 类型       | 说明                      |
| ----------- | -------- | ----------------------- |
| icon        | `string` | 应用图标缓存路径                |
| name        | `string` | 应用名称                    |
| packageName | `string` | 应用包名                    |
| version     | `string` | 应用版本号                   |
| path        | `string` | 安装包文件路径                 |
| flags       | `number` | `ApplicationInfo.flags` |
| size        | `number` | 安装包文件大小，单位字节            |

---

## 重要说明

### 关于 `item.path`

`item.path` 返回的是 **原始文件路径**，例如：

```text
/data/app/xxx/base.apk
```

如果你需要把它当作文件 URI 使用，**必须手动在前面拼接 `file://`**，例如：

```ts
const fileUri = "file://" + item.path
```

否则它只是一个普通字符串路径，不是完整的文件 URI。

---

## 页面调用示例

### 示例 1：获取全部应用

```vue
<script setup lang="uts">
import { GetInstalledAppList } from "@/uni_modules/x2-AppList"

const test = () => {
	const list = GetInstalledAppList("all")
	console.log("全部应用：", list)
}
</script>

<template>
	<view>
		<button @click="test()">获取全部应用</button>
	</view>
</template>
```

---

### 示例 2：按条件搜索

```vue
<script setup lang="uts">
import { SearchInstalledApps } from "@/uni_modules/x2-AppList"

const testSearch = () => {
	const list = SearchInstalledApps("qq", "user")
	console.log("搜索结果：", list)
}
</script>

<template>
	<view>
		<button @click="testSearch()">搜索应用</button>
	</view>
</template>
```

---

### 示例 3：搜索 + 分类 + 列表展示

```vue
<template>
	<view class="page">
		<view class="search-row">
			<input
				class="search-input"
				v-model="keyword"
				placeholder="搜索应用名或包名"
				confirm-type="search"
				@confirm="handleSearch"
			/>
			<button class="search-btn" @click="handleSearch">搜索</button>
		</view>

		<view class="filter-row">
			<view
				class="filter-item"
				:class="activeFilter == 'all' ? 'filter-item-active' : ''"
				@click="changeFilter('all')"
			>
				<text>全部</text>
			</view>

			<view
				class="filter-item"
				:class="activeFilter == 'user' ? 'filter-item-active' : ''"
				@click="changeFilter('user')"
			>
				<text>用户</text>
			</view>

			<view
				class="filter-item"
				:class="activeFilter == 'system' ? 'filter-item-active' : ''"
				@click="changeFilter('system')"
			>
				<text>系统</text>
			</view>
		</view>

		<scroll-view class="list" scroll-y="true">
			<view
				v-for="(item, index) in appList"
				:key="item.packageName + '_' + index"
				class="app-item"
			>
				<image
					v-if="item.icon != null && item.icon.length > 0"
					class="app-icon"
					:src="item.icon"
					mode="aspectFit"
				/>
				<view class="app-info">
					<text class="app-name">{{ item.name }}</text>
					<text class="app-desc">包名：{{ item.packageName }}</text>
					<text class="app-desc">版本：{{ item.version }}</text>
					<text class="app-desc">路径：{{ item.path }}</text>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script setup lang="uts">
import { ref, onMounted } from "vue"
import { GetInstalledAppList, SearchInstalledApps } from "@/uni_modules/x2-AppList"

type AppItem = {
	icon: string
	name: string
	packageName: string
	version: string
	path: string
	flags: number
	size: number
}

const keyword = ref("")
const activeFilter = ref("all")
const appList = ref<AppItem[]>([])

const loadList = (): void => {
	const key = keyword.value.trim()
	if (key.length > 0) {
		appList.value = SearchInstalledApps(key, activeFilter.value)
	} else {
		appList.value = GetInstalledAppList(activeFilter.value)
	}
}

const handleSearch = (): void => {
	loadList()
}

const changeFilter = (filter: string): void => {
	activeFilter.value = filter
	loadList()
}

onMounted(() => {
	loadList()
})
</script>

<style>
.page {
	height: 100%;
	padding: 12px;
	box-sizing: border-box;
	background-color: #f7f8fa;
}

.search-row {
	display: flex;
	flex-direction: row;
	align-items: center;
	margin-bottom: 12px;
}

.search-input {
	flex: 1;
	height: 38px;
	background-color: #ffffff;
	border-radius: 8px;
	border: 1px solid #dddddd;
	padding-left: 12px;
	padding-right: 12px;
	box-sizing: border-box;
}

.search-btn {
	margin-left: 10px;
	height: 38px;
	line-height: 38px;
	padding-left: 14px;
	padding-right: 14px;
	font-size: 14px;
}

.filter-row {
	display: flex;
	flex-direction: row;
	align-items: center;
	margin-bottom: 12px;
}

.filter-item {
	padding-top: 8px;
	padding-bottom: 8px;
	padding-left: 14px;
	padding-right: 14px;
	background-color: #ffffff;
	border-radius: 18px;
	margin-right: 10px;
	border: 1px solid #dddddd;
}

.filter-item-active {
	border-color: #007aff;
	background-color: #eaf3ff;
}

.app-item {
	display: flex;
	flex-direction: row;
	padding: 12px;
	margin-bottom: 10px;
	border-radius: 10px;
	background-color: #ffffff;
}

.app-icon {
	width: 48px;
	height: 48px;
	border-radius: 10px;
	background-color: #f0f0f0;
}

.app-info {
	flex: 1;
	margin-left: 12px;
	display: flex;
	flex-direction: column;
}

.app-name {
	font-size: 15px;
	font-weight: bold;
	color: #111111;
	margin-bottom: 4px;
}

.app-desc {
	font-size: 12px;
	color: #666666;
	margin-bottom: 2px;
	word-break: break-all;
}

.list {
	height: calc(100% - 100px);
}
</style>
```

---

## 注意事项

### 1. 权限申请

使用插件之前请确保声明了android.permission.QUERY_ALL_PACKAGES权限，并且用户已经授权
```ts
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
```

### 2. 图标路径说明

`icon` 字段为插件缓存生成的本地图标文件路径，通常已经是可直接用于 `<image>` 的路径格式。

### 3. 安装包路径说明

`path` 字段是原始 APK 文件路径。
如果你要把它作为文件 URI 使用，记得拼接：

```ts
const fileUri = "file://" + item.path
```

---

## 推荐调用方式

### 获取全部应用

```ts
const list = GetInstalledAppList("all")
```

### 获取用户应用

```ts
const list = GetInstalledAppList("user")
```

### 获取系统应用

```ts
const list = GetInstalledAppList("system")
```

### 搜索全部应用

```ts
const list = SearchInstalledApps("chrome", "all")
```

### 搜索用户应用

```ts
const list = SearchInstalledApps("微信", "user")
```

### 搜索系统应用

```ts
const list = SearchInstalledApps("设置", "system")
```

---

