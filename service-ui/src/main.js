import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './style.css'
import 'leaflet/dist/leaflet.css';

// Swiper components
import { Swiper, SwiperSlide } from 'swiper/vue'
import 'swiper/css'

// AOS Animation Library
import AOS from 'aos'
import 'aos/dist/aos.css'

// FontAwesome setup
import { library } from '@fortawesome/fontawesome-svg-core'
import { faPhoneVolume } from '@fortawesome/free-solid-svg-icons'
import { faFacebookF, faYoutube, faTiktok } from '@fortawesome/free-brands-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

library.add(faPhoneVolume, faFacebookF, faYoutube, faTiktok)

// Initialize AOS
AOS.init({
  duration: 800,
  easing: 'ease-in-out',
  once: true,
  offset: 100
})

// Visitor tracking is handled by router navigation guards
// No need to initialize here to avoid duplicate calls

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.component('font-awesome-icon', FontAwesomeIcon)
// Đăng ký Swiper components globally để tránh lỗi ref context
app.component('Swiper', Swiper)
app.component('SwiperSlide', SwiperSlide)
app.mount('#app')
