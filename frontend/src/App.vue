<template>
  <CommonHeader @scroll="scrollToSection" />
  <router-view />
</template>

<script setup>
import CommonHeader from "./components/CommonHeader.vue";

const scrollToSection = (id) => {
  const el = document.getElementById(id);
  if (el) el.scrollIntoView({ behavior: "smooth", block: "start" });
};
</script>

<style>
/* 전역 앱 스타일 설정 영역 */
</style>
