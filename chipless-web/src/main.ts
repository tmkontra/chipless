import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import Dialog from 'primevue/dialog'
import PickList from 'primevue/picklist'
import { PrimeVue } from '@primevue/core'
import Tailwind_PT from './components/tailwind'

const app = createApp(App)

app.use(PrimeVue, {
  unstyled: true,
  pt: Tailwind_PT
})

app.component('p-dialog', Dialog)
app.component('p-pick-list', PickList)
app.use(router)

app.mount('#app')
