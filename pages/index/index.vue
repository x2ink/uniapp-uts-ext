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
				<text
					class="filter-text"
					:class="activeFilter == 'all' ? 'filter-text-active' : ''"
				>
					全部
				</text>
			</view>

			<view
				class="filter-item"
				:class="activeFilter == 'user' ? 'filter-item-active' : ''"
				@click="changeFilter('user')"
			>
				<text
					class="filter-text"
					:class="activeFilter == 'user' ? 'filter-text-active' : ''"
				>
					用户
				</text>
			</view>

			<view
				class="filter-item"
				:class="activeFilter == 'system' ? 'filter-item-active' : ''"
				@click="changeFilter('system')"
			>
				<text
					class="filter-text"
					:class="activeFilter == 'system' ? 'filter-text-active' : ''"
				>
					系统
				</text>
			</view>
		</view>

		<view class="summary-row">
			<text class="summary-text">当前分类：{{ getFilterLabel(activeFilter) }}</text>
			<text class="summary-text">数量：{{ appList.length }}</text>
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
				<view v-else class="app-icon placeholder"></view>

				<view class="app-info">
					<text class="app-name">{{ item.name }}</text>
					<text class="app-desc">包名：{{ item.packageName }}</text>
					<text class="app-desc">版本：{{ item.version }}</text>
					<text class="app-desc">大小：{{ formatSize(item.size) }}</text>
					<text class="app-desc">路径：{{ item.path }}</text>
				</view>
			</view>

			<view v-if="appList.length == 0" class="empty-wrap">
				<text class="empty-text">暂无数据</text>
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

	// 如果你要默认系统，把这里改成 "system"
	// const activeFilter = ref("system")

	const loadList = (): void => {
		try {
			const key = keyword.value.trim()
			if (key.length > 0) {
				appList.value = SearchInstalledApps(key, activeFilter.value)
			} else {
				appList.value = GetInstalledAppList(activeFilter.value)
			}
			console.log("appList:", appList.value)
		} catch (e) {
			console.log("loadList error:", e)
			appList.value = []
		}
	}

	const handleSearch = (): void => {
		loadList()
	}

	const changeFilter = (filter: string): void => {
		if (activeFilter.value == filter) {
			return
		}
		activeFilter.value = filter
		loadList()
	}

	const getFilterLabel = (filter: string): string => {
		if (filter == "user") {
			return "用户"
		}
		if (filter == "system") {
			return "系统"
		}
		return "全部"
	}

	const formatSize = (size: number): string => {
		if (size < 1024) {
			return size + " B"
		}
		if (size < 1024 * 1024) {
			return (size / 1024).toFixed(2) + " KB"
		}
		if (size < 1024 * 1024 * 1024) {
			return (size / 1024 / 1024).toFixed(2) + " MB"
		}
		return (size / 1024 / 1024 / 1024).toFixed(2) + " GB"
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

	.filter-text {
		font-size: 14px;
		color: #333333;
	}

	.filter-text-active {
		color: #007aff;
		font-weight: bold;
	}

	.summary-row {
		display: flex;
		flex-direction: row;
		justify-content: space-between;
		margin-bottom: 12px;
	}

	.summary-text {
		font-size: 13px;
		color: #666666;
	}

	.list {
		height: calc(100% - 100px);
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

	.placeholder {
		background-color: #e5e5e5;
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

	.empty-wrap {
		padding-top: 40px;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.empty-text {
		font-size: 14px;
		color: #999999;
	}
</style>