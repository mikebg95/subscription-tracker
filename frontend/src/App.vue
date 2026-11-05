<script setup>
import AppIcon from './components/AppIcon.vue'
</script>

<template>
  <header class="bar">
    <RouterLink to="/" class="logo">
      <span class="mark"><AppIcon name="card" :size="18" /></span>
      Subscription Tracker
    </RouterLink>
    <div id="bar-actions"></div>
  </header>
  <main class="main">
    <RouterView />
  </main>
</template>
