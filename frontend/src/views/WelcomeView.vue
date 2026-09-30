<template>
    <div style="width: 100vw;height: 100vh;overflow: hidden;display: flex">
      <div style="flex:1; background: linear-gradient(135deg, #1e3c72, #2a5298);"></div>
        <div class="welcome-title">
            <div style="font-size: 30px;font-weight: bold">欢迎来到校园论坛</div>
            <div style="margin-top: 10px">在这里分享你的校园生活</div>
            <div style="margin-top: 5px">在这里认识更多有趣的人</div>
        </div>
        <div class="right-card">
            <router-view v-slot="{ Component }">
                <transition name="el-fade-in-linear" mode="out-in">
                    <component :is="Component" style="height: 100%"/>
                </transition>
            </router-view>
        </div>
    </div>
</template>

<script setup>

</script>

<style scoped>
.right-card {
  width: 400px;
  z-index: 1;
  background-color: var(--el-bg-color);
}

.welcome-title {
    position: absolute;
    bottom: 30px;
    left: 30px;
    color: white;
    text-shadow: 0 0 10px black;
}
</style>
