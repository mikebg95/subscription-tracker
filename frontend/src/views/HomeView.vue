<script setup>
import { onMounted, ref } from 'vue'
import AppIcon from '../components/AppIcon.vue'
import SubscriptionDialog from '../components/SubscriptionDialog.vue'
import DeleteDialog from '../components/DeleteDialog.vue'
import { getCount, getSubscriptions, getTotal } from '../api/subscriptions.js'
import { formatPrice } from '../format.js'

const subscriptions = ref([])
const count = ref(0)
const total = ref(0)
const loading = ref(true)
const failed = ref(false)

// 'new' for the add dialog, a subscription for the edit dialog
const editing = ref(null)
const deleting = ref(null)

async function load() {
  loading.value = true
  failed.value = false
  try {
    ;[subscriptions.value, count.value, total.value] = await Promise.all([getSubscriptions(), getCount(), getTotal()])
  } catch {
    failed.value = true
  } finally {
    loading.value = false
  }
}

function onSaved() {
  editing.value = null
  load()
}

function onDeleted() {
  deleting.value = null
  load()
}

onMounted(load)
</script>

<template>
  <Teleport defer to="#bar-actions">
    <button v-if="!loading && !failed && subscriptions.length" class="btn primary" @click="editing = 'new'">
      <AppIcon name="plus" />Add subscription
    </button>
  </Teleport>

  <p v-if="loading" class="loading">Loading…</p>

  <div v-else-if="failed" class="state">
    <div class="state-icon"><AppIcon name="warning" :size="26" /></div>
    <h1 class="state-title">Something went wrong</h1>
    <button class="btn primary" @click="load"><AppIcon name="home" />Back to home</button>
  </div>

  <div v-else-if="!subscriptions.length" class="state">
    <div class="state-icon brand"><AppIcon name="card" :size="28" /></div>
    <h1 class="state-title">No subscriptions yet</h1>
    <p class="state-text">Add your subscriptions to track what you spend each month.</p>
    <button class="btn primary" @click="editing = 'new'"><AppIcon name="plus" />Add subscription</button>
  </div>

  <template v-else>
    <div class="grid2">
      <div class="stat">
        <div class="sic"><AppIcon name="list" :size="20" /></div>
        <div>
          <div class="slab">Total subscriptions</div>
          <div class="sval">{{ count }}</div>
        </div>
      </div>
      <div class="stat">
        <div class="sic accent"><AppIcon name="euro" :size="20" /></div>
        <div>
          <div class="slab">Total monthly cost</div>
          <div class="sval">{{ formatPrice(total) }}</div>
        </div>
      </div>
    </div>

    <ul class="list">
      <li v-for="subscription in subscriptions" :key="subscription.id" class="row">
        <span class="name">{{ subscription.name }}</span>
        <div class="row-end">
          <span class="price">{{ formatPrice(subscription.price) }}<span class="mo"> /mo</span></span>
          <div class="row-actions">
            <button class="ico" :aria-label="`Edit ${subscription.name}`" @click="editing = subscription">
              <AppIcon name="edit" />
            </button>
            <button class="ico" :aria-label="`Delete ${subscription.name}`" @click="deleting = subscription">
              <AppIcon name="trash" />
            </button>
          </div>
        </div>
      </li>
    </ul>
  </template>

  <SubscriptionDialog
    v-if="editing"
    :subscription="editing === 'new' ? null : editing"
    @saved="onSaved"
    @close="editing = null"
  />
  <DeleteDialog v-if="deleting" :subscription="deleting" @deleted="onDeleted" @close="deleting = null" />
</template>
