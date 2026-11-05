<script setup>
defineProps({
  name: { type: String, required: true },
  size: { type: Number, default: 16 },
})

const paths = {
  card: ['M3 10h18'],
  plus: ['M12 5v14M5 12h14'],
  list: ['M9 6h11M9 12h11M9 18h11', 'M5 6h.01M5 12h.01M5 18h.01'],
  euro: ['M17.2 7.5A6 6 0 1 0 17 16.8', 'M5 10.5h9M5 13.5h8'],
  edit: ['M4 20h4L18.5 9.5a1.5 1.5 0 0 0-4-4L4 16v4zM13.5 6.5l4 4'],
  trash: ['M4 7h16M10 11v6M14 11v6M5 7l1 12a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2l1-12M9 7V4h6v3'],
  check: ['M5 12l5 5L20 7'],
  alert: ['M12 8v4M12 16h.01'],
  warning: ['M12 4l9 16H3z', 'M12 10v4M12 18h.01'],
  home: ['M5 12l7-7 7 7M6 10v9h4v-5h4v5h4v-9'],
}
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="2"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
    class="icon"
  >
    <rect v-if="name === 'card'" x="3" y="5" width="18" height="14" rx="3" />
    <circle v-if="name === 'alert'" cx="12" cy="12" r="9" />
    <path v-for="d in paths[name]" :key="d" :d="d" />
  </svg>
</template>
