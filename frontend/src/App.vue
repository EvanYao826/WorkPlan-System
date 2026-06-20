<template>
  <router-view />
</template>

<script setup>
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC',
    'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial,
    sans-serif;
  -webkit-font-smoothing: antialiased;
  background: #f5f7fa;
}

/* 全局滚动条美化 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}
</style>
