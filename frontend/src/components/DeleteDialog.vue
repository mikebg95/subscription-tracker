<script setup>
import { onMounted, ref } from 'vue'
import AppIcon from './AppIcon.vue'
import { deleteSubscription } from '../api/subscriptions.js'

const props = defineProps({
  subscription: { type: Object, required: true },
})
const emit = defineEmits(['deleted', 'close'])

const dialog = ref(null)
const deleting = ref(false)
const error = ref('')

onMounted(() => dialog.value.showModal())

async function confirm() {
  deleting.value = true
  error.value = ''
  try {
    await deleteSubscription(props.subscription.id)
    emit('deleted')
  } catch (e) {
    // already gone is fine: the list gets reloaded either way
    if (e.status === 404) emit('deleted')
    else error.value = 'Something went wrong. Please try again.'
  } finally {
    deleting.value = false
  }
}
</script>

<template>
  <dialog ref="dialog" class="modal center" @close="emit('close')">
    <div class="modal-icon danger"><AppIcon name="trash" :size="22" /></div>
    <h2 class="modal-title tight">Delete this subscription?</h2>
    <p class="modal-text">"{{ subscription.name }}" will be removed. This can't be undone.</p>
    <p v-if="error" class="field-error general"><AppIcon name="alert" :size="14" />{{ error }}</p>
    <div class="actions stretch">
      <button type="button" class="btn" @click="dialog.close()">Cancel</button>
      <button type="button" class="btn solid-danger" :disabled="deleting" @click="confirm">Delete</button>
    </div>
  </dialog>
</template>
