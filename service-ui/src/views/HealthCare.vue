<template>
  <div>
    <!-- Hero Section - Outside container (CMS philosophy/hero) -->
    <section v-if="visible('hero')" class="hero-section relative w-full overflow-hidden">
      <!-- Background Image -->
      <img
        :src="imageOf('hero', extra('hero'))"
        alt="HealthCare Banner"
        class="w-full h-auto object-cover"
        @error="handleBannerError"
      />

      <!-- Content Overlay -->
      <div class="absolute inset-0 flex items-center justify-center">
        <div class="text-center" data-aos="fade-up">
          <h3 class="text-lg sm:text-xl md:text-3xl lg:text-4xl font-bold text-white mb-2 md:mb-4 lg:mb-4" style="text-shadow: 0 2px 4px rgba(0,0,0,0.1);">
            {{ extra('hero').title }}
          </h3>
          <p class="text-xs sm:text-sm md:text-2xl lg:text-4xl text-teal-500 font-medium tracking-wider leading-relaxed transform hover:scale-105 transition-all duration-300 ease-in-out drop-shadow-sm px-4" style="font-family: 'Great Vibes', cursive;">
            <span class="text-white text-sm sm:text-base md:text-2xl lg:text-4xl">{{ extra('hero').subtitle }}</span>
          </p>
        </div>
      </div>
    </section>

    <div class="healthcare-page" style="background-color: #fefce8;">

    <!-- Story Section (CMS philosophy/article) -->
    <section v-if="visible('article')" class="story-section py-12">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto">
          <div class="text-center mb-16" data-aos="fade-up">
            <h2 class="text-3xl md:text-4xl font-bold text-green-600 mb-4">{{ extra('article').title }}</h2>
            <div class="w-24 h-1 bg-green-500 mx-auto"></div>
          </div>

          <div class="mx-auto text-gray-700 leading-relaxed text-base md:text-lg" data-aos="fade-up" data-aos-delay="200">
            <template v-for="(block, idx) in articleBlocks" :key="idx">
              <h4 v-if="block.type === 'h4'" class="text-xl md:text-2xl font-bold text-gray-800 mt-8 mb-4">{{ block.text }}</h4>
              <div
                v-else
                :class="idx === articleBlocks.length - 1 ? 'text-base md:text-lg' : 'mb-6 text-base md:text-lg'"
                v-html="renderRichText(block.text)"
              ></div>
            </template>
          </div>
        </div>
      </div>
    </section>

    <!-- Core Values Section (CMS philosophy/principles) -->
    <section v-if="visible('principles')" class="core-values-section py-8">
      <div class="container mx-auto px-4">
        <div class="text-center mb-16" data-aos="fade-up">
          <h3 class="text-3xl md:text-4xl font-bold text-green-600 mb-4">{{ extra('principles').title }}</h3>
          <div class="w-24 h-1 bg-green-500 mx-auto"></div>
        </div>

        <div class="grid md:grid-cols-3 gap-8">
          <div
            v-for="(item, idx) in extra('principles').items || []"
            :key="idx"
            class="value-card bg-white rounded-lg shadow-lg p-8"
            data-aos="fade-up"
            :data-aos-delay="(idx + 1) * 100"
          >
            <div class="text-center mb-6">
              <div class="relative w-32 h-32 mx-auto mb-4 flex items-center justify-center">
                <div class="w-24 h-24 bg-green-50 rounded-full absolute"></div>
                <img :src="imageOf('principles', item, idx)" :alt="item.title" class="w-32 h-32 object-contain relative z-10" />
              </div>
              <h3 class="text-xl font-bold text-gray-800 mb-4">{{ item.title }}</h3>
            </div>
            <div class="text-gray-700 leading-relaxed text-base md:text-lg">
              {{ item.body }}
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Nutritional Values Section (CMS philosophy/food) -->
    <section v-if="visible('food')" class="nutrition-section py-8">
      <div class="container mx-auto px-4">
        <div class="text-center mb-16" data-aos="fade-up">
          <h3 class="text-3xl md:text-4xl font-bold text-green-600 mb-4">{{ extra('food').title }}</h3>
          <div class="w-24 h-1 bg-green-500 mx-auto"></div>
        </div>

        <div class="grid md:grid-cols-5 gap-6">
          <div
            v-for="(item, idx) in extra('food').items || []"
            :key="idx"
            class="nutrition-card text-center"
            data-aos="zoom-in"
            :data-aos-delay="(idx + 1) * 100"
          >
            <div class="nutrition-icon w-20 h-20 bg-green-100 rounded-full mx-auto mb-4 flex items-center justify-center">
              <span class="text-3xl">{{ item.icon }}</span>
            </div>
            <h3 class="text-lg font-semibold text-gray-800 mb-2">{{ item.title }}</h3>
            <p class="text-sm text-gray-600">{{ item.subtitle }}</p>
          </div>
        </div>
      </div>
    </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { usePageContent } from '../composables/usePageContent.js'
import { renderRichText } from '../utils/richText.js'

const { extra, visible, imageOf } = usePageContent('philosophy')
const articleBlocks = computed(() => extra('article').blocks || [])

function handleBannerError(event) {
  // Hide the image and show fallback background
  event.target.style.display = 'none'
}

onMounted(() => {
  // Initialize AOS if available
  if (typeof AOS !== 'undefined') {
    AOS.refresh()
  }
})
</script>

<style scoped>
.healthcare-page {
  overflow-x: hidden;
}

/* Hero section improvements */
.hero-section {
  position: relative;
  width: 100vw;
  left: 50%;
  right: 50%;
  margin-left: -50vw;
  margin-right: -50vw;
  background-color: #fefce8; /* Fallback background */
}

/* Ensure banner image displays properly */
.hero-section img {
  width: 100%;
  height: auto;
  display: block;
  object-fit: cover;
  object-position: center;
}

/* Ensure text is always readable */
.hero-section h1,
.hero-section p {
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

/* Smooth transitions for responsive elements */
.circle-logo,
.hero-section h1,
.hero-section p {
  transition: all 0.3s ease;
}

.value-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.value-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.nutrition-card {
  transition: transform 0.3s ease;
}

.nutrition-card:hover {
  transform: scale(1.05);
}

.nutrition-icon {
  transition: all 0.3s ease;
}

.nutrition-card:hover .nutrition-icon {
  background-color: #f97316;
  transform: rotate(5deg);
}

.circle-logo {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

/* Responsive adjustments for hero section */
@media (max-width: 640px) {
  .logo-container {
    margin-bottom: 0.5rem !important;
  }
  
  .circle-logo {
    width: 3rem !important;
    height: 3rem !important;
  }
  
  .circle-logo img {
    width: 1.5rem !important;
    height: 1.5rem !important;
  }
  
  h3 {
    font-size: 1.125rem !important;
    margin-bottom: 0.375rem !important;
    line-height: 1.2 !important;
  }
  
  p {
    font-size: 0.6875rem !important;
    line-height: 1.3 !important;
    padding: 0 0.75rem !important;
  }
}

@media (min-width: 641px) and (max-width: 768px) {
  .logo-container {
    margin-bottom: 0.75rem !important;
  }
  
  .circle-logo {
    width: 3.5rem !important;
    height: 3.5rem !important;
  }
  
  .circle-logo img {
    width: 2rem !important;
    height: 2rem !important;
  }
  
  h3 {
    font-size: 1.375rem !important;
    margin-bottom: 0.5rem !important;
  }
  
  p {
    font-size: 0.8125rem !important;
    line-height: 1.4 !important;
  }
}

@media (min-width: 769px) and (max-width: 1023px) {
  .logo-container {
    margin-bottom: 2rem !important;
  }
  
  .circle-logo {
    width: 8rem !important;
    height: 8rem !important;
  }
  
  .circle-logo img {
    width: 7rem !important;
    height: 7rem !important;
  }
  
  h3 {
    font-size: 3.5rem !important;
    margin-bottom: 1.5rem !important;
  }
  
  p {
    font-size: 1.75rem !important;
    line-height: 1.6 !important;
  }
}

@media (min-width: 1024px) {
  /* Desktop styles if needed */
}

/* Hide scrollbar */
::-webkit-scrollbar {
  display: none;
}

/* For Firefox */
* {
  scrollbar-width: none;
}

/* For IE and Edge */
* {
  -ms-overflow-style: none;
}
</style> 