<script setup>
import AppIcon from '../components/AppIcon.vue'
</script>

<template>
  <div class="state">
    <div class="code">404</div>
    <h1 class="state-title">Page not found</h1>
    <RouterLink to="/" class="btn primary"><AppIcon name="home" />Back to home</RouterLink>
  </div>
</template>
