import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import Dialog from 'primevue/dialog'
import { PrimeVue } from '@primevue/core'
import Tailwind_PT from './components/tailwind'

const app = createApp(App)

app.use(PrimeVue, {
  unstyled: true,
  pt: Tailwind_PT
})

app.component('p-dialog', Dialog)
app.use(router)

app.mount('#app')
