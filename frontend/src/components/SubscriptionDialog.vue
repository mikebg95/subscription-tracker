<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import AppIcon from './AppIcon.vue'
import { createSubscription, updateSubscription } from '../api/subscriptions.js'

const props = defineProps({
  // null means a new subscription
  subscription: { type: Object, default: null },
})
const emit = defineEmits(['saved', 'close'])

const dialog = ref(null)
const isEdit = computed(() => props.subscription !== null)

const form = reactive({
  name: props.subscription?.name ?? '',
  price: props.subscription ? props.subscription.price.toFixed(2) : '',
})
const errors = reactive({ name: '', price: '', general: '' })
const saving = ref(false)

onMounted(() => dialog.value.showModal())

// same rules as the backend's SubscriptionRequest
function validate() {
  const name = form.name.trim()
  const price = form.price.trim().replace(',', '.')

  errors.name = !name ? 'Name is required' : name.length > 100 ? 'Name can be at most 100 characters' : ''

  if (!price) errors.price = 'Price is required'
  else if (!/^\d{1,8}(\.\d{1,2})?$/.test(price)) errors.price = 'Price can have at most 8 digits and 2 decimals'
  else if (Number(price) <= 0) errors.price = 'Price must be greater than 0'
  else errors.price = ''

  return !errors.name && !errors.price ? { name, price: Number(price) } : null
}

async function save() {
  errors.general = ''
  const subscription = validate()
  if (!subscription) return

  saving.value = true
  try {
    if (isEdit.value) await updateSubscription(props.subscription.id, subscription)
    else await createSubscription(subscription)
    emit('saved')
  } catch (e) {
    if (e.status === 409) errors.name = e.message
    else errors.general = e.status === 404 ? 'This subscription no longer exists.' : 'Something went wrong. Please try again.'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <dialog ref="dialog" class="modal" @close="emit('close')">
    <form novalidate @submit.prevent="save">
      <h2 class="modal-title">{{ isEdit ? 'Edit subscription' : 'Add subscription' }}</h2>

      <div class="field">
        <label class="lab" for="name">Name</label>
        <input
          id="name"
          v-model="form.name"
          class="in"
          :class="{ invalid: errors.name }"
          placeholder="e.g. Netflix"
          maxlength="100"
          autocomplete="off"
          autofocus
        />
        <p v-if="errors.name" class="field-error"><AppIcon name="alert" :size="14" />{{ errors.name }}</p>
      </div>

      <div class="field last">
        <label class="lab" for="price">Monthly price</label>
        <div class="in" :class="{ invalid: errors.price }">
          <span class="currency">€</span>
          <input id="price" v-model="form.price" placeholder="0.00" inputmode="decimal" autocomplete="off" />
        </div>
        <p v-if="errors.price" class="field-error"><AppIcon name="alert" :size="14" />{{ errors.price }}</p>
      </div>

      <p v-if="errors.general" class="field-error general"><AppIcon name="alert" :size="14" />{{ errors.general }}</p>

      <div class="actions">
        <button type="button" class="btn" @click="dialog.close()">Cancel</button>
        <button type="submit" class="btn primary" :disabled="saving"><AppIcon name="check" />Save</button>
      </div>
    </form>
  </dialog>
</template>
