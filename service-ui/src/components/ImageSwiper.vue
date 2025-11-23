<template>
  <Swiper
    :modules="modules"
    :slides-per-view="slidesPerView"
    :space-between="spaceBetween"
    :loop="loop"
    :autoplay="autoplay"
    :speed="speed"
    :grab-cursor="grabCursor"
    :centered-slides="centeredSlides"
    :breakpoints="breakpoints"
  >
    <SwiperSlide v-for="(img, index) in images" :key="index">
      <img
        :src="img.src"
        :alt="img.alt"
        class="w-full h-40 object-contain rounded-xl shadow hover:shadow-lg transition-shadow duration-300 cursor-pointer hover:opacity-90 transition-opacity duration-300"
        @click="$emit('imageClick', img.src, img.alt)"
      />
    </SwiperSlide>
  </Swiper>
</template>

<script>
import { Swiper, SwiperSlide } from 'swiper/vue'
import { Autoplay } from 'swiper/modules'
import 'swiper/css'

export default {
  name: 'ImageSwiper',
  components: {
    Swiper,
    SwiperSlide
  },
  props: {
    images: {
      type: Array,
      required: true,
      default: () => []
    },
    slidesPerView: {
      type: [Number, String],
      default: 2.5
    },
    spaceBetween: {
      type: [Number, String],
      default: 12
    },
    loop: {
      type: Boolean,
      default: false
    },
    autoplay: {
      type: Object,
      default: () => ({
        delay: 3000,
        disableOnInteraction: false,
        pauseOnMouseEnter: true
      })
    },
    speed: {
      type: Number,
      default: 800
    },
    grabCursor: {
      type: Boolean,
      default: true
    },
    centeredSlides: {
      type: Boolean,
      default: false
    },
    breakpoints: {
      type: Object,
      default: () => ({
        320: {
          slidesPerView: 2.2,
          spaceBetween: 8
        },
        480: {
          slidesPerView: 2.5,
          spaceBetween: 12
        }
      })
    }
  },
  emits: ['imageClick'],
  setup() {
    return {
      modules: [Autoplay]
    }
  }
}
</script>

