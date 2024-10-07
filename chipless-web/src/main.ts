import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import vModal from './components/v-modal.vue'

const app = createApp(App)

app.component('v-modal', vModal)

app.use(router)

app.mount('#app')
