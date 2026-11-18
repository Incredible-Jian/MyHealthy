<template>
  <div :class="{'health-application': /^\/(health-|simulator)/.test(route.path)}"><router-view /></div>
</template>

<script setup>
import {useRoute} from 'vue-router'
import '@/assets/styles/health-design.scss'
const route=useRoute()
import useSettingsStore from '@/store/modules/settings'
import { handleThemeStyle } from '@/utils/theme'

onMounted(() => {
  nextTick(() => {
    // 初始化主题样式
    handleThemeStyle(useSettingsStore().theme)
  })
})
</script>
