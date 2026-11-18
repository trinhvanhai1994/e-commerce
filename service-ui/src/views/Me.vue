<template>
  <div>
    <!-- Hero Section - Outside container (CMS story/hero) -->
    <section v-if="visible('hero')" class="hero-section relative w-full overflow-hidden">
      <!-- Background Image -->
      <img
        :src="imageOf('hero', extra('hero'))"
        alt="Thi Yên Banner"
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

    <div class="veyo-page" style="background-color: #fefce8;">

    <!-- Story Section (CMS story/intro, story/feature_1, story/feature_2) -->
    <section v-if="visible('intro') || visible('feature_1') || visible('feature_2')" class="story-section py-8">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto">
          <div v-if="visible('intro')" class="text-center mb-8" data-aos="fade-up">
            <h2 class="text-3xl md:text-4xl font-bold text-green-600 mb-4">
              {{ extra('intro').title }}
            </h2>
            <div class="w-24 h-1 bg-green-500 mx-auto"></div>
          </div>

          <div
            class="mx-auto text-gray-700 leading-relaxed text-base md:text-lg"
            data-aos="fade-up"
            data-aos-delay="200"
          >
            <template v-if="visible('intro')">
              <div
                v-for="(text, idx) in paragraphs('intro')"
                :key="`intro-${idx}`"
                :class="idx === paragraphs('intro').length - 1 ? 'text-gray-700 mb-4' : 'mb-6'"
                v-html="renderRichText(text)"
              ></div>
            </template>

            <!-- Block với ảnh bên phải -->
            <div v-if="visible('feature_1')" class="flex flex-col md:flex-row items-start gap-6">
              <!-- Nội dung bên trái -->
              <div class="flex-1">
                <h4 class="text-xl md:text-2xl font-bold text-gray-800 mt-4 mb-4">
                  {{ extra('feature_1').title }}
                </h4>
                <div
                  v-for="(text, idx) in paragraphs('feature_1')"
                  :key="`f1-${idx}`"
                  :class="idx === 0 ? 'text-gray-700 mb-4 text-base md:text-lg' : 'mb-6 text-base md:text-lg'"
                  v-html="renderRichText(text)"
                ></div>
              </div>

              <!-- Ảnh bên phải -->
              <div v-if="imageOf('feature_1', extra('feature_1'))" class="w-full md:w-64 shrink-0">
                <img
                  :src="imageOf('feature_1', extra('feature_1'))"
                  :alt="extra('feature_1').imageAlt"
                  class="rounded-lg shadow-lg w-full cursor-pointer hover:opacity-90 transition-opacity duration-300"
                  @click="openImageModal(imageOf('feature_1', extra('feature_1')), `${extra('feature_1').imageAlt} - ${extra('feature_1').title}`)"
                />
              </div>
            </div>

            <!-- Block với ảnh bên trái desktop, dưới mobile -->
            <div v-if="visible('feature_2')" class="flex flex-col md:flex-row items-start gap-6">
              <!-- Nội dung bên phải (desktop) / trên (mobile) -->
              <div class="flex-1 order-1 md:order-2">
                <h4 class="text-xl md:text-2xl font-bold text-gray-800 mt-4 mb-4">
                  {{ extra('feature_2').title }}
                </h4>
                <div
                  v-for="(text, idx) in paragraphs('feature_2')"
                  :key="`f2-${idx}`"
                  :class="idx === paragraphs('feature_2').length - 1 ? 'text-base md:text-lg' : 'mb-6 text-base md:text-lg'"
                  v-html="renderRichText(text)"
                ></div>
              </div>

              <!-- Ảnh bên trái (desktop) / dưới (mobile) -->
              <div v-if="imageOf('feature_2', extra('feature_2'))" class="w-full md:w-64 shrink-0 order-2 md:order-1">
                <img
                  :src="imageOf('feature_2', extra('feature_2'))"
                  :alt="extra('feature_2').imageAlt"
                  class="rounded-lg shadow-lg w-full cursor-pointer hover:opacity-90 transition-opacity duration-300"
                  @click="openImageModal(imageOf('feature_2', extra('feature_2')), extra('feature_2').imageAlt)"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Vision Section (CMS story/vision) -->
    <section v-if="visible('vision')" class="vision-section py-4">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto">
          <div class="text-center mb-8" data-aos="fade-up">
            <h2 class="text-3xl md:text-4xl font-bold text-green-600 mb-2">{{ extra('vision').title }}</h2>
            <div class="w-24 h-1 bg-green-500 mx-auto"></div>
          </div>

          <div class="bg-gray-50 rounded-lg shadow-lg p-8" data-aos="fade-up" data-aos-delay="200">
            <div class="mx-auto text-gray-700 leading-relaxed text-base md:text-lg">
              <div
                v-for="(text, idx) in paragraphs('vision')"
                :key="`vision-${idx}`"
                :class="idx === paragraphs('vision').length - 1 ? '' : 'mb-6'"
                v-html="renderRichText(text)"
              ></div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Mission Section (CMS story/mission) -->
    <section v-if="visible('mission')" class="mission-section py-4">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto">
          <div class="text-center mb-8" data-aos="fade-up">
            <h2 class="text-3xl md:text-4xl font-bold text-green-600 mb-2">{{ extra('mission').title }}</h2>
            <div class="w-24 h-1 bg-green-500 mx-auto"></div>
          </div>

          <div class="bg-gray-50 rounded-lg shadow-lg p-8" data-aos="fade-up" data-aos-delay="200">
            <div class="mx-auto text-gray-700 leading-relaxed text-base md:text-lg">
              <div v-if="extra('mission').intro" class="mb-6" v-html="renderRichText(extra('mission').intro)"></div>
              <h3 v-if="extra('mission').commitTitle" class="text-xl md:text-2xl font-bold text-gray-800 mb-2">{{ extra('mission').commitTitle }}</h3>
              <ul class="space-y-2 text-gray-700 text-base md:text-lg">
                <li v-for="(item, idx) in extra('mission').items || []" :key="idx">• {{ item }}</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Core Values Section (CMS story/values) -->
    <section v-if="visible('values')" class="core-values-section py-4">
      <div class="container mx-auto px-4">
        <div class="text-center mb-8" data-aos="fade-up">
          <h2 class="text-3xl md:text-4xl font-bold text-green-600 mb-4">{{ extra('values').title }}</h2>
          <div class="w-24 h-1 bg-green-500 mx-auto"></div>
        </div>

        <div class="grid md:grid-cols-3 gap-8">
          <div
            v-for="(item, idx) in extra('values').items || []"
            :key="idx"
            class="value-card bg-white rounded-lg shadow-lg p-8"
            data-aos="fade-up"
            :data-aos-delay="(idx + 1) * 100"
          >
            <div class="text-center mb-6">
              <div class="relative w-32 h-32 mx-auto mb-4 flex items-center justify-center">
                <div class="w-24 h-24 bg-green-50 rounded-full absolute"></div>
                <img :src="imageOf('values', item, idx)" :alt="item.title" class="w-32 h-32 object-contain relative z-10" />
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

    <!-- Nutritional Values Section (CMS story/nutrition) -->
    <section v-if="visible('nutrition')" class="nutrition-section py-4">
      <div class="container mx-auto px-4">
        <div class="text-center mb-8" data-aos="fade-up">
          <h2 class="text-3xl md:text-4xl font-bold text-green-600 mb-4">{{ extra('nutrition').title }}</h2>
          <div class="w-24 h-1 bg-green-500 mx-auto"></div>
        </div>

        <div class="grid md:grid-cols-5 gap-6">
          <div
            v-for="(item, idx) in extra('nutrition').items || []"
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

    <!-- Ingredients Section (CMS story/ingredients) -->
    <section v-if="visible('ingredients')" class="ingredients-section py-2">
      <div class="container mx-auto px-4">
        <div class="max-w-6xl mx-auto">
          <div class="text-center mb-8" data-aos="fade-up">
            <h2 class="text-4xl md:text-5xl font-bold text-green-600 mb-6 gradient-text-green">{{ ing.title }}</h2>
            <div class="w-32 h-1 bg-gradient-to-r from-green-400 to-green-600 mx-auto rounded-full"></div>
            <div v-if="ing.subtitle" class="text-lg text-gray-600 mt-4 max-w-2xl mx-auto">{{ ing.subtitle }}</div>
          </div>

          <div class="bg-gradient-to-br from-white to-green-50 rounded-3xl shadow-2xl p-8 md:p-12 border border-green-100" data-aos="fade-up" data-aos-delay="200">
            <div class="grid lg:grid-cols-4 gap-8">
              <!-- Tab Navigation -->
              <div class="lg:col-span-1">
                <div class="space-y-3">
                  <button
                    v-for="(tab, index) in ingTabs"
                    :key="index"
                    @click="activeTab = index"
                    :class="[
                      'w-full text-left px-6 py-4 rounded-2xl transition-all duration-300 font-semibold text-sm md:text-base',
                      activeTab === index
                        ? 'bg-gradient-to-r from-green-500 to-green-600 text-white shadow-lg transform scale-105'
                        : 'bg-white text-gray-700 hover:bg-green-50 hover:text-green-600 border border-gray-200 hover:border-green-200'
                    ]"
                  >
                    <div class="flex items-center space-x-3">
                      <div :class="[
                        'w-2 h-2 rounded-full transition-all duration-300',
                        activeTab === index ? 'bg-white' : 'bg-green-500'
                      ]"></div>
                      <span>{{ tab }}</span>
                    </div>
                  </button>
                </div>
              </div>

              <!-- Tab Content -->
              <div class="lg:col-span-3">
                <div class="tab-content bg-white rounded-2xl p-8 shadow-lg">
                  <div v-if="activeTab === 0" class="space-y-6">
                    <div class="flex items-center space-x-3 mb-6">
                      <div class="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center">
                        <span class="text-green-600 text-xl">🌿</span>
                      </div>
                      <h3 class="text-2xl font-bold text-gray-800">{{ ing.composition?.heading }}</h3>
                    </div>
                    <div v-if="(ing.composition?.items || []).length" class="bg-gradient-to-r from-green-50 to-blue-50 p-6 rounded-xl border-l-4 border-green-500">
                      <div class="space-y-4">
                        <div
                          v-for="(item, idx) in ing.composition.items"
                          :key="idx"
                          class="bg-white p-4 rounded-lg border"
                          :class="idx % 2 ? 'border-pink-200' : 'border-green-200'"
                        >
                          <h4 class="font-bold mb-2" :class="idx % 2 ? 'text-pink-800' : 'text-green-800'">{{ item.name }}</h4>
                          <p class="text-gray-700 leading-relaxed text-sm" v-html="renderRichText(item.body)"></p>
                        </div>
                      </div>
                    </div>
                    <div v-if="ing.composition?.note" class="bg-amber-50 p-4 rounded-lg border border-amber-200">
                      <p class="text-amber-800 font-medium">
                        <span class="font-bold">⚠️ Lưu ý:</span> {{ ing.composition.note }}
                      </p>
                    </div>
                  </div>

                  <div v-if="activeTab === 1" class="space-y-6">
                    <div class="flex items-center space-x-3 mb-6">
                      <div class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                        <span class="text-blue-600 text-xl">📋</span>
                      </div>
                      <h3 class="text-2xl font-bold text-gray-800">{{ ing.info?.heading }}</h3>
                    </div>
                    <div class="grid md:grid-cols-2 gap-6">
                      <div
                        v-for="(group, gIdx) in ing.info?.groups || []"
                        :key="gIdx"
                        class="p-6 rounded-xl"
                        :class="gIdx % 2 ? 'bg-gradient-to-br from-green-50 to-emerald-50' : 'bg-gradient-to-br from-blue-50 to-indigo-50'"
                      >
                        <h4 class="font-bold text-gray-800 mb-3 flex items-center">
                          <span
                            class="w-6 h-6 rounded-full flex items-center justify-center text-white text-sm mr-3"
                            :class="gIdx % 2 ? 'bg-green-500' : 'bg-blue-500'"
                          >{{ gIdx + 1 }}</span>
                          {{ group.title }}
                        </h4>
                        <ul class="space-y-2 text-gray-700">
                          <li v-for="(line, lIdx) in group.items || []" :key="lIdx" class="flex items-center">
                            <span class="w-2 h-2 rounded-full mr-3" :class="gIdx % 2 ? 'bg-green-500' : 'bg-blue-500'"></span>{{ line }}
                          </li>
                        </ul>
                      </div>
                    </div>
                  </div>

                  <div v-if="activeTab === 2" class="space-y-6">
                    <div class="flex items-center space-x-3 mb-6">
                      <div class="w-10 h-10 bg-purple-100 rounded-full flex items-center justify-center">
                        <span class="text-purple-600 text-xl">📊</span>
                      </div>
                      <h3 class="text-2xl font-bold text-gray-800">{{ ing.facts?.heading }}</h3>
                    </div>
                    <div v-if="ing.facts?.note" class="bg-gradient-to-r from-purple-50 to-pink-50 p-6 rounded-xl border-l-4 border-purple-500">
                      <p class="text-gray-700 leading-relaxed mb-4">
                        <strong v-if="ing.facts.noteLabel">{{ ing.facts.noteLabel }}</strong> {{ ing.facts.note }}
                      </p>
                    </div>
                    <div v-if="ing.facts?.valueTitle || ing.facts?.valueBody" class="bg-gradient-to-br from-gray-50 to-gray-100 p-6 rounded-xl">
                      <div class="flex items-center space-x-3 mb-4">
                        <span class="text-2xl">📈</span>
                        <h4 class="text-lg font-bold text-gray-800">{{ ing.facts.valueTitle }}</h4>
                      </div>
                      <p class="text-gray-600 font-medium">{{ ing.facts.valueBody }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Product Showcase Section (CMS story/products) -->
    <section v-if="visible('products')" class="product-showcase py-2">
      <div class="container mx-auto px-4">
        <div class="max-w-7xl mx-auto">
          <div class="text-center mb-8" data-aos="fade-up">
            <h2 class="text-4xl md:text-5xl font-bold text-green-600 mb-6 gradient-text-green">{{ prod.title }}</h2>
            <div class="w-32 h-1 bg-gradient-to-r from-green-400 to-green-600 mx-auto rounded-full"></div>
            <p v-if="prod.subtitle" class="text-lg text-gray-600 mt-4 max-w-2xl mx-auto">{{ prod.subtitle }}</p>
          </div>

          <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
            <div
              v-for="(item, idx) in prod.items || []"
              :key="idx"
              class="product-card bg-gradient-to-br from-white rounded-3xl p-8 shadow-2xl border"
              :class="idx % 2 ? 'to-pink-50 border-pink-100' : 'to-green-50 border-green-100'"
              :data-aos="idx % 2 ? 'fade-left' : 'fade-right'"
            >
              <div class="relative overflow-hidden rounded-2xl mb-6">
                <img
                  v-if="imageOf('products', item, idx)"
                  :src="imageOf('products', item, idx)"
                  :alt="item.imageAlt || item.name"
                  class="w-full h-80 object-contain bg-white rounded-2xl shadow-lg hover:shadow-xl transition-all duration-500 cursor-pointer hover:scale-105"
                  @click="openImageModal(imageOf('products', item, idx), item.imageAlt || item.name)"
                />
                <div
                  v-if="item.productId"
                  class="absolute inset-0 bg-gradient-to-t from-black/20 to-transparent opacity-0 hover:opacity-100 transition-opacity duration-300 rounded-2xl flex items-end justify-center pb-4"
                >
                  <button
                    @click="goToProductDetail(Number(item.productId))"
                    class="bg-white/90 px-4 py-2 rounded-full font-semibold text-sm"
                    :class="idx % 2 ? 'text-pink-600' : 'text-green-600'"
                  >
                    Xem chi tiết
                  </button>
                </div>
              </div>
              <div class="text-center">
                <h3 class="text-2xl font-bold text-gray-800 mb-3">{{ item.name }}</h3>
                <p class="text-gray-600 leading-relaxed mb-4">{{ item.body }}</p>
                <div v-if="(item.tags || []).length" class="flex justify-center space-x-4">
                  <span
                    v-for="(tag, tIdx) in item.tags"
                    :key="tIdx"
                    class="px-3 py-1 rounded-full text-sm font-medium"
                    :class="tagClass(idx, tIdx)"
                  >{{ tag }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Call to Action -->
          <div v-if="prod.ctaTitle || prod.ctaButton" class="text-center mt-8" data-aos="fade-up" data-aos-delay="300">
            <div class="bg-gradient-to-r from-green-500 to-green-600 rounded-2xl p-6 text-white">
              <h3 class="text-2xl md:text-3xl font-bold mb-4">{{ prod.ctaTitle }}</h3>
              <p v-if="prod.ctaBody" class="text-lg mb-6 opacity-90">{{ prod.ctaBody }}</p>
              <button
                v-if="prod.ctaButton"
                @click="goToProducts"
                class="bg-white text-green-600 px-8 py-3 rounded-full font-bold text-lg hover:bg-gray-100 transition-colors duration-300 shadow-lg"
              >
                {{ prod.ctaButton }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Meal gallery (CMS story/gallery) -->
    <section v-if="visible('gallery') && images.length" class="max-w-8xl mx-auto px-4 py-2" data-aos="fade-up">
      <h2 class="text-2xl md:text-3xl font-bold text-green-600 mb-6 text-center gradient-text-green" data-aos="fade-up">{{ extra('gallery').title }}</h2>
      <!-- Desktop: ảnh vuông to, ẩn trên mobile -->
      <div class="hidden md:grid md:grid-cols-4 md:gap-6">
        <img
          v-for="(img, index) in images"
          :key="'d' + index"
          :src="img.src"
          :alt="img.alt"
          class="w-full aspect-square object-contain bg-gray-50 rounded-xl shadow hover:shadow-lg transition-shadow duration-300 cursor-pointer hover:opacity-90 transition-opacity duration-300"
          @click="openImageModal(img.src, img.alt)"
        />
      </div>

      <!-- Mobile: swiper slider, ẩn trên desktop -->
      <div class="md:hidden">
        <swiper
          :modules="modules"
          :slides-per-view="2.5"
          space-between="12"
          :loop="shouldLoop"
          :autoplay="{
            delay: 3000,
            disableOnInteraction: false,
            pauseOnMouseEnter: true
          }"
          :speed="800"
          :grab-cursor="true"
          :centered-slides="false"
          :breakpoints="{
            320: {
              slidesPerView: 2.2,
              spaceBetween: 8
            },
            480: {
              slidesPerView: 2.5,
              spaceBetween: 12
            }
          }"
        >
          <swiper-slide v-for="(img, index) in loopImages" :key="'m' + index">
            <img
              :src="img.src"
              :alt="img.alt"
              class="w-full h-40 object-contain rounded-xl shadow hover:shadow-lg transition-shadow duration-300 cursor-pointer hover:opacity-90 transition-opacity duration-300"
              @click="openImageModal(img.src, img.alt)"
            />
          </swiper-slide>
        </swiper>
      </div>
    </section>
    </div>

    <!-- Image Zoom Modal -->
    <div
      v-if="showImageModal"
      class="fixed inset-0 z-[100] flex items-center justify-center bg-black bg-opacity-90 backdrop-blur-sm"
      @click="closeImageModal"
    >
      <div class="relative w-full h-full flex items-center justify-center p-4">
        <!-- Close Button -->
        <button
          @click="closeImageModal"
          class="absolute top-4 right-4 z-10 bg-white rounded-full p-3 shadow-lg hover:bg-gray-100 transition-colors duration-200"
        >
          <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
        
        <!-- Zoomed Image Container -->
        <div class="relative max-w-[90vw] max-h-[90vh] flex items-center justify-center">
          <img
            :src="zoomedImageSrc"
            :alt="zoomedImageAlt"
            class="max-w-full max-h-full object-contain rounded-lg shadow-2xl zoom-image"
            @click.stop
          />
        </div>
        
        <!-- Image Info -->
        <div v-if="zoomedImageAlt" class="absolute bottom-4 left-4 right-4 bg-black bg-opacity-70 text-white p-3 rounded-lg">
          <p class="text-sm md:text-base text-center">{{ zoomedImageAlt }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Swiper, SwiperSlide } from 'swiper/vue';
import { Autoplay } from 'swiper/modules';
import 'swiper/css';
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { navigateToMainDomain, navigateToSubdomain } from '../utils/domainUtils';
import { getImageUrlFromApi } from '../utils/imageUtils.js';
import { usePageContent } from '../composables/usePageContent.js';
import { renderRichText } from '../utils/richText.js';

export default {
  name: 'Me',
  components: {
    Swiper,
    SwiperSlide
  },
  setup() {
    const router = useRouter();
    
    const goToProducts = () => {
      const domainChanged = navigateToMainDomain('/products');
      if (!domainChanged) {
        router.push('/products');
      }
    };
    
    const goToProductDetail = (productId) => {
      const path = `/products/${productId}`;
      
      // Check if this is a special product (ID 1, 2, 3, or 4)
      if (productId === 1 || productId === 2 || productId === 3 || productId === 4) {
        const domainChanged = navigateToSubdomain(path, productId);
        if (!domainChanged) {
          router.push(path);
        }
      } else {
        // Regular product, use main domain
        const domainChanged = navigateToMainDomain(path);
        if (!domainChanged) {
          router.push(path);
        }
      }
    };
    
    // Swiper modules
    const modules = [Autoplay];

    // Story sections editable in admin (Câu chuyện Thi Yên); falls back to packaged content
    const { extra, visible, imageOf } = usePageContent('story');
    const paragraphs = (key) => extra(key).paragraphs || [];
    const ing = computed(() => extra('ingredients'));
    const ingTabs = computed(() => (ing.value.tabs || []).slice(0, 3));
    const prod = computed(() => extra('products'));
    const TAG_CLASSES = [
      ['bg-green-100 text-green-800', 'bg-blue-100 text-blue-800'],
      ['bg-pink-100 text-pink-800', 'bg-purple-100 text-purple-800'],
    ];
    const tagClass = (cardIdx, tagIdx) => TAG_CLASSES[cardIdx % 2][tagIdx % 2];

    return {
      ing,
      ingTabs,
      prod,
      tagClass,
      goToProducts,
      goToProductDetail,
      modules,
      getImageUrlFromApi,
      extra,
      visible,
      imageOf,
      paragraphs,
      renderRichText
    };
  },
  data() {
    return {
      activeTab: 0,
      showImageModal: false,
      zoomedImageSrc: '',
      zoomedImageAlt: ''
    }
  },
  methods: {
    handleBannerError(event) {
      // Hide the image and show fallback background
      event.target.style.display = 'none'
    }
  },
  computed: {
    // Gallery images from CMS story/gallery; an item without a resolvable image is skipped
    images() {
      return (this.extra('gallery').items || [])
        .map((item, idx) => ({ src: this.imageOf('gallery', item, idx), alt: item.alt || '' }))
        .filter((img) => img.src)
    },
    // Computed property để duplicate images nếu cần cho loop
    // Loop cần ít nhất 2 * slidesPerView slides để hoạt động đúng
    loopImages() {
      const minSlidesForLoop = Math.ceil(2 * 2.5) // 2 * slidesPerView, làm tròn lên
      if (this.images.length === 0 || this.images.length >= minSlidesForLoop) {
        return this.images
      }
      // Duplicate images để đủ cho loop
      const duplicated = []
      while (duplicated.length < minSlidesForLoop) {
        duplicated.push(...this.images)
      }
      return duplicated.slice(0, minSlidesForLoop)
    },
    // Luôn bật loop khi đã duplicate đủ slides
    shouldLoop() {
      return true
    }
  },
  methods: {
    handleBannerError(event) {
      // Hide the image and show fallback background
      event.target.style.display = 'none'
    },
    openImageModal(src, alt = '') {
      // Đảm bảo ảnh được load trước khi hiển thị
      const img = new Image()
      img.onload = () => {
        this.zoomedImageSrc = src
        this.zoomedImageAlt = alt
        this.showImageModal = true
        // Prevent body scroll when modal is open
        document.body.style.overflow = 'hidden'
      }
      img.onerror = () => {
        // Fallback nếu ảnh không load được
        this.zoomedImageSrc = src
        this.zoomedImageAlt = alt
        this.showImageModal = true
        document.body.style.overflow = 'hidden'
      }
      img.src = src
    },
    closeImageModal() {
      this.showImageModal = false
      this.zoomedImageSrc = ""
      this.zoomedImageAlt = ""
      // Restore body scroll
      document.body.style.overflow = 'auto'
    }
  },
  mounted() {
    // Initialize AOS if available
    if (typeof AOS !== 'undefined') {
      AOS.refresh()
    }
  }
}
</script>

<style scoped>
.veyo-page {
  overflow-x: hidden;
  position: relative;
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

/* Logo ở góc - ảnh bên trong nhỏ */
.absolute.top-4.left-4 .logo-circle img {
  width: 1.5rem !important;
  height: 1.5rem !important;
}

@media (min-width: 640px) {
  .absolute.top-4.left-4 .logo-circle img {
    width: 1.75rem !important;
    height: 1.75rem !important;
  }
}

/* Ensure text is always readable */
.hero-section h1,
.hero-section p {
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

/* Smooth transitions for responsive elements */
.logo-circle,
.hero-section h1,
.hero-section p {
  transition: all 0.3s ease;
}

.product-card, .value-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.product-card:hover, .value-card:hover {
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

.logo-circle {
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
  
  .logo-circle {
    width: 3rem !important;
    height: 3rem !important;
  }
  
  .logo-circle img {
    width: 1.5rem !important;
    height: 1.5rem !important;
  }
  
  h1 {
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
  
  .logo-circle {
    width: 3.5rem !important;
    height: 3.5rem !important;
  }
  
  .logo-circle img {
    width: 2rem !important;
    height: 2rem !important;
  }
  
  h1 {
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
  
  .logo-circle {
    width: 8rem !important;
    height: 8rem !important;
  }
  
  .logo-circle img {
    width: 7rem !important;
    height: 7rem !important;
  }
  
  h1 {
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

.tab-content {
  min-height: 200px;
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

/* Gradient text effect */
.gradient-text-green {
  background: linear-gradient(45deg, #059669, #10b981);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Enhanced Tab Styling */
.tab-content {
  min-height: 300px;
  transition: all 0.3s ease;
}

/* Product Card Enhancements */
.product-card {
  transition: all 0.4s ease;
  position: relative;
  overflow: hidden;
}

.product-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.product-card:hover::before {
  opacity: 1;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15);
}

/* Tab Button Enhancements */
.tab-button {
  position: relative;
  overflow: hidden;
}

.tab-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.5s ease;
}

.tab-button:hover::before {
  left: 100%;
}

/* Enhanced Image Hover Effects */
.product-card img {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.product-card:hover img {
  transform: scale(1.05);
}

/* Call to Action Button */
.cta-button {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #10b981, #059669);
  transition: all 0.3s ease;
}

.cta-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.6s ease;
}

.cta-button:hover::before {
  left: 100%;
}

.cta-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(16, 185, 129, 0.3);
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .product-card {
    margin-bottom: 1rem;
  }
  
  .tab-content {
    min-height: 250px;
  }
  
  .product-card img {
    height: 60vh;
    max-height: 300px;
  }
  
  /* Reduce spacing on mobile */
  .story-section,
  .vision-section,
  .mission-section,
  .core-values-section,
  .nutrition-section,
  .ingredients-section,
  .product-showcase {
    padding-top: 1rem !important;
    padding-bottom: 1rem !important;
  }
  
  .text-center.mb-8 {
    margin-bottom: 1.5rem !important;
  }
  
  .text-center.mb-16 {
    margin-bottom: 2rem !important;
  }
}

@media (max-width: 640px) {
  .product-card {
    padding: 1.5rem;
  }
  
  .product-card img {
    height: 50vh;
    max-height: 250px;
  }
  
  .tab-content {
    padding: 1.5rem;
  }
  
  /* Further reduce spacing on small mobile */
  .story-section,
  .vision-section,
  .mission-section,
  .core-values-section,
  .nutrition-section,
  .ingredients-section,
  .product-showcase {
    padding-top: 0.5rem !important;
    padding-bottom: 0.5rem !important;
  }
  
  .text-center.mb-8 {
    margin-bottom: 1rem !important;
  }
  
  .text-center.mb-16 {
    margin-bottom: 1.5rem !important;
  }
}

/* Image Zoom Modal Styles */
.zoom-image {
  animation: zoomIn 0.3s ease-out;
  transform-origin: center;
  width: auto;
  height: auto;
  max-width: 90vw;
  max-height: 90vh;
  object-fit: contain;
  object-position: center;
  border-radius: 0.5rem;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

@keyframes zoomIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* Modal backdrop animation */
.fixed.inset-0 {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* Close button hover effect */
.absolute.top-4.right-4 button:hover {
  transform: scale(1.1);
  background-color: #f3f4f6;
}

/* Image hover effects for clickable images */
img[class*="cursor-pointer"]:hover {
  transform: scale(1.02);
  filter: brightness(1.05);
}

/* Smooth transitions for all clickable images */
img[class*="cursor-pointer"] {
  transition: all 0.3s ease;
}

/* Responsive adjustments for modal */
@media (max-width: 768px) {
  .zoom-image {
    max-width: 95vw;
    max-height: 85vh;
  }
  
  .absolute.top-4.right-4 {
    top: 1rem;
    right: 1rem;
  }
  
  .absolute.top-4.right-4 button {
    padding: 0.75rem;
  }
  
  .absolute.top-4.right-4 svg {
    width: 1.25rem;
    height: 1.25rem;
  }
  
  .absolute.bottom-4 {
    bottom: 1rem;
    left: 1rem;
    right: 1rem;
  }
}

/* Swiper mobile optimizations */
@media (max-width: 767px) {
  .swiper {
    overflow: visible;
  }
  
  .swiper-slide {
    transition: transform 0.3s ease;
  }
  
  .swiper-slide:hover {
    transform: scale(1.02);
  }
  
  /* Ensure smooth autoplay */
  .swiper-wrapper {
    transition-timing-function: ease-in-out;
  }
}

@media (min-width: 768px) {
  .swiper-container {
    display: none !important;
  }
}

/* Enhanced image styling for better visual appeal */
.image-container {
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.image-container:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

/* Aspect ratio container */
.aspect-4-3 {
  aspect-ratio: 4 / 3;
  position: relative;
  overflow: hidden;
}

/* Image styling */
.image-container img {
  transition: all 0.3s ease;
  object-fit: contain;
  object-position: center;
}

.image-container:hover img {
  transform: scale(1.02);
}

/* Enhanced shadow effects */
.shadow-xl {
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.shadow-2xl {
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

/* Content card enhancements */
.content-card {
  background: linear-gradient(135deg, #f0fdf4 0%, #ffffff 100%);
  border: 1px solid rgba(16, 185, 129, 0.2);
  transition: all 0.3s ease;
}

.content-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  border-color: rgba(16, 185, 129, 0.3);
}

/* Quote styling */
.quote-box {
  position: relative;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.8) 0%, rgba(240, 253, 244, 0.6) 100%);
  border-left: 4px solid #10b981;
  transition: all 0.3s ease;
}

.quote-box::before {
  content: '"';
  position: absolute;
  top: -10px;
  left: 15px;
  font-size: 3rem;
  color: #10b981;
  opacity: 0.3;
  font-family: serif;
}

.quote-box:hover {
  transform: translateX(5px);
  box-shadow: 0 10px 25px rgba(16, 185, 129, 0.1);
}

/* Responsive image adjustments */
@media (max-width: 1024px) {
  .image-gallery {
    margin-bottom: 2rem;
  }
  
  .content-card {
    padding: 1.5rem;
  }
}

/* Single image design */
.image-container {
  position: relative;
}

/* Responsive adjustments for single image layout */
@media (max-width: 768px) {
  .bg-gradient-to-br {
    padding: 1rem !important;
  }
  
  .space-y-6 > * + * {
    margin-top: 1.5rem !important;
  }
  
  .bg-gray-50 {
    padding: 1rem !important;
  }
  
  .grid.grid-cols-1.sm\\:grid-cols-2 {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }
}

@media (max-width: 480px) {
  .bg-gradient-to-br {
    padding: 0.75rem !important;
  }
  
  .space-y-6 > * + * {
    margin-top: 1rem !important;
  }
  
  .bg-gray-50 {
    padding: 0.75rem !important;
  }
  
  .text-2xl {
    font-size: 1.5rem !important;
  }
}

</style>
  
  
  
  
  