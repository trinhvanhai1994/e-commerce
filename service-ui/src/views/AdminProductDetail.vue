<template>
  <AdminLayout>
    <div class="max-w-6xl mx-auto">
      <!-- Header -->
      <div class="mb-6 flex items-center justify-between">
        <div>
          <h2 class="text-2xl font-bold text-green-700">Chi tiết sản phẩm</h2>
        </div>
        <button 
          @click="goBack" 
          class="px-4 py-2 bg-gray-500 text-white rounded hover:bg-gray-600 font-semibold"
        >
          ← Quay lại
        </button>
      </div>

      <!-- Loading state -->
      <div v-if="loading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-green-600"></div>
        <p class="mt-4 text-gray-600">Đang tải dữ liệu...</p>
      </div>

      <!-- Error state -->
      <div v-else-if="error" class="bg-red-100 border border-red-400 text-red-700 px-6 py-4 rounded-lg mb-6">
        <p class="font-semibold mb-2">{{ error }}</p>
        <button @click="fetchProduct" class="text-red-600 underline hover:no-underline">Thử lại</button>
      </div>

      <!-- Product Form -->
      <div v-else class="bg-white rounded-xl shadow-lg p-8">
        <form @submit.prevent="submitForm">
          <!-- Basic Information Section -->
          <div class="mb-8">
            <h3 class="text-xl font-bold text-green-700 mb-4 pb-2 border-b">Thông tin cơ bản</h3>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block font-semibold mb-2 text-gray-700">
                  Tên sản phẩm <span class="text-red-500">*</span>
                </label>
                <input 
                  v-model="form.name" 
                  required 
                  class="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent" 
                  placeholder="Nhập tên sản phẩm"
                />
              </div>

              <div>
                <label class="block font-semibold mb-2 text-gray-700">Danh mục</label>
                <select 
                  v-model="form.category" 
                  class="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                >
                  <option value="ME_DEN">Mè đen</option>
                  <option value="HONG_DAU">Hồng đậu</option>
                  <option value="COMBO">Combo</option>
                </select>
              </div>

              <div>
                <label class="block font-semibold mb-2 text-gray-700">
                  Giá khuyến mãi (Giá bán) <span class="text-red-500">*</span>
                </label>
                <input 
                  v-model.number="form.price" 
                  type="number" 
                  min="0" 
                  required 
                  class="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent" 
                />
                <p class="text-xs text-gray-500 mt-1">Giá hiển thị cho khách hàng</p>
              </div>

              <div>
                <label class="block font-semibold mb-2 text-gray-700">Giá gốc (Giá cũ)</label>
                <input 
                  v-model.number="form.oldPrice" 
                  type="number" 
                  min="0" 
                  class="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent" 
                />
                <p class="text-xs text-gray-500 mt-1">Giá gốc trước khi giảm giá (để hiển thị giá gạch ngang)</p>
              </div>

              <div>
                <label class="block font-semibold mb-2 text-gray-700">Số lượng/Quy cách</label>
                <input 
                  v-model="form.quantity" 
                  placeholder="VD: 2 lon x 600g" 
                  class="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent" 
                />
              </div>

              <div>
                <label class="block font-semibold mb-2 text-gray-700">Giảm giá (%)</label>
                <input 
                  :value="calculatedDiscount" 
                  type="number" 
                  readonly
                  class="w-full border rounded-lg px-4 py-2 bg-gray-100 text-gray-700 cursor-not-allowed" 
                />
                <p class="text-xs text-gray-500 mt-1">Tự động tính từ giá khuyến mãi và giá gốc</p>
              </div>

              <div>
                <label class="block font-semibold mb-2 text-gray-700">Số đánh giá</label>
                <input 
                  v-model.number="form.reviewCount" 
                  type="number" 
                  min="0" 
                  class="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent" 
                />
              </div>

              <div>
                <label class="block font-semibold mb-2 text-gray-700">Ưu tiên (Priority)</label>
                <input 
                  v-model.number="form.priority" 
                  type="number" 
                  min="1" 
                  class="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent" 
                  placeholder="999"
                />
                <p class="text-xs text-gray-500 mt-1">
                  Số nhỏ hơn = ưu tiên cao hơn. Sản phẩm có priority 1-4 sẽ hiển thị ở trang chủ.
                </p>
              </div>
            </div>

            <div class="mt-6 grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block font-semibold mb-2 text-gray-700">Trạng thái</label>
                <select 
                  v-model="form.status" 
                  class="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
                >
                  <option value="ACTIVE">Hiển thị (ACTIVE)</option>
                  <option value="INACTIVE">Ẩn (INACTIVE)</option>
                </select>
              </div>

            </div>
          </div>

          <!-- Description Section -->
          <div class="mb-8">
            <h3 class="text-xl font-bold text-green-700 mb-4 pb-2 border-b">Mô tả sản phẩm</h3>
            
            <div class="space-y-6">
              <div>
                <label class="block font-semibold mb-2 text-gray-700">Mô tả ngắn</label>
                <textarea 
                  v-model="form.shortDesc" 
                  class="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent" 
                  rows="3" 
                  placeholder="Mô tả ngắn gọn về sản phẩm..."
                ></textarea>
                <p class="text-xs text-gray-500 mt-1">Mô tả ngắn hiển thị ở danh sách sản phẩm</p>
              </div>

              <div>
                <label class="block font-semibold mb-2 text-gray-700">Mô tả chi tiết sản phẩm</label>
                <textarea 
                  v-model="form.detailedUsage" 
                  class="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent" 
                  rows="5" 
                  placeholder="Mô tả chi tiết về sản phẩm..."
                ></textarea>
                <p class="text-xs text-gray-500 mt-1">Mô tả chi tiết hiển thị ở trang chi tiết sản phẩm</p>
              </div>
            </div>
          </div>

          <!-- Images Section -->
          <div class="mb-8">
            <h3 class="text-xl font-bold text-green-700 mb-4 pb-2 border-b">Hình ảnh sản phẩm</h3>
            
            <!-- Gallery Images - 11 ảnh -->
            <div>
              <label class="block font-semibold mb-3 text-gray-700">Ảnh mô tả (Gallery) - Tối đa 11 ảnh</label>
              <p class="text-xs text-gray-500 mb-4">Chọn ảnh để thay thế. Nếu có path cũ, ảnh mới sẽ thay thế ảnh cũ nhưng giữ nguyên path.</p>
              
              <!-- Grid 11 ảnh -->
              <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                <div 
                  v-for="(imageItem, index) in form.galleryImages" 
                  :key="index"
                  class="relative border-2 border-dashed border-gray-300 rounded-lg p-3 bg-gray-50 hover:bg-gray-100 transition"
                  :class="{ 'border-green-400 bg-green-50': imageItem.path || imageItem.preview }"
                >
                  <!-- Image Preview -->
                  <div class="relative w-full aspect-square mb-2 rounded-lg overflow-hidden bg-white border border-gray-200">
                    <div v-if="imageItem.loading" class="absolute inset-0 flex items-center justify-center bg-gray-100">
                      <div class="text-center">
                        <div class="inline-block animate-spin rounded-full h-6 w-6 border-b-2 border-blue-500 mb-1"></div>
                        <span class="text-xs text-gray-600 block">Đang tải...</span>
                      </div>
                    </div>
                    <img 
                      v-else-if="imageItem.preview || imageItem.path" 
                      :src="imageItem.preview && imageItem.preview.startsWith('data:') 
                        ? imageItem.preview 
                        : getImageUrlWithCacheBusting(getImageUrlFromApi(imageItem.path), imageCacheBuster)" 
                      :alt="`Gallery ${index + 1}`" 
                      class="w-full h-full object-cover"
                      @error="(e) => handleGalleryImageError(e, index)"
                      :key="`gallery-${index}-${imageItem.path || ''}-${imageCacheBuster}`"
                    />
                    <div v-else class="absolute inset-0 flex flex-col items-center justify-center text-gray-400">
                      <svg class="w-8 h-8 mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/>
                      </svg>
                      <span class="text-xs font-semibold">Ảnh {{ index + 1 }}</span>
                      <span class="text-xs text-gray-300 mt-0.5">Chưa có</span>
                    </div>
                    
                    <!-- Remove button -->
                    <button 
                      v-if="imageItem.path || imageItem.preview"
                      type="button"
                      @click="removeGalleryImage(index)"
                      class="absolute top-1 right-1 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs hover:bg-red-600 font-bold shadow-lg"
                    >
                      ×
                    </button>
                  </div>
                  
                  <!-- File name and path display -->
                  <div v-if="imageItem.path" class="text-xs mb-2">
                    <div class="font-semibold text-green-700 truncate" :title="imageItem.fileName || imageItem.path">
                      {{ imageItem.fileName || imageItem.path.split('/').pop() }}
                    </div>
                    <div class="text-gray-500 truncate text-xs mt-0.5" :title="imageItem.path">
                      {{ imageItem.path }}
                    </div>
                  </div>
                  <div v-else class="text-xs text-gray-400 mb-2">
                    Chưa có ảnh
                  </div>
                  
                  <!-- File input -->
                  <input 
                    :ref="el => { if (el) galleryFileInputs[index] = el }"
                    type="file" 
                    @change="(e) => handleGalleryFileUpload(e, index)" 
                    accept="image/*"
                    class="hidden"
                  />
                  
                  <!-- Select button -->
                  <button 
                    type="button"
                    @click="galleryFileInputs[index]?.click()"
                    class="w-full px-3 py-1.5 text-xs rounded font-semibold transition"
                    :class="imageItem.path || imageItem.preview 
                      ? 'bg-yellow-500 text-white hover:bg-yellow-600' 
                      : 'bg-blue-500 text-white hover:bg-blue-600'"
                  >
                    {{ imageItem.path || imageItem.preview ? 'Thay đổi ảnh' : 'Chọn ảnh' }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="flex justify-end gap-4 pt-6 border-t">
            <button 
              type="button" 
              @click="goBack" 
              class="px-6 py-3 rounded-lg border-2 border-gray-300 hover:bg-gray-50 font-semibold text-gray-700"
            >
              Hủy
            </button>
            <button 
              type="submit" 
              :disabled="submitting" 
              class="px-6 py-3 rounded-lg bg-green-600 text-white font-semibold hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {{ submitting ? 'Đang lưu...' : 'Lưu thay đổi' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Custom Popup Modal -->
    <div
      v-if="showPopup"
      class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
      @click.self="closePopup"
    >
      <div class="bg-white rounded-xl shadow-2xl max-w-md w-full transform transition-all">
        <div class="p-6">
          <!-- Icon và Message -->
          <div class="flex items-start mb-4">
            <div 
              :class="[
                'flex-shrink-0 w-12 h-12 rounded-full flex items-center justify-center mr-4',
                popupType === 'success' ? 'bg-green-100' : 'bg-red-100'
              ]"
            >
              <svg 
                v-if="popupType === 'success'"
                class="w-6 h-6 text-green-600" 
                fill="none" 
                stroke="currentColor" 
                viewBox="0 0 24 24"
              >
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
              </svg>
              <svg 
                v-else
                class="w-6 h-6 text-red-600" 
                fill="none" 
                stroke="currentColor" 
                viewBox="0 0 24 24"
              >
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </div>
            <div class="flex-1">
              <h3 
                :class="[
                  'text-lg font-bold mb-2',
                  popupType === 'success' ? 'text-green-700' : 'text-red-700'
                ]"
              >
                {{ popupType === 'success' ? 'Thành công' : 'Lỗi' }}
              </h3>
              <p class="text-gray-700 whitespace-pre-line">{{ popupMessage }}</p>
            </div>
          </div>
          
          <!-- Button -->
          <div class="flex justify-end">
            <button
              @click="closePopup"
              :class="[
                'px-6 py-2 rounded-lg font-medium transition-colors',
                popupType === 'success' 
                  ? 'bg-green-500 text-white hover:bg-green-600' 
                  : 'bg-red-500 text-white hover:bg-red-600'
              ]"
            >
              OK
            </button>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminLayout from './AdminLayout.vue'
import { productManagementAPI } from '@/utils/api.js'
import { getImageUrlFromApi, getImageUrlWithCacheBusting } from '@/utils/imageUtils.js'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5678'

const route = useRoute()
const router = useRouter()

const productId = ref(null)
const loading = ref(false)
const error = ref('')
const submitting = ref(false)
const fileInput = ref(null)
const imageLoading = ref(false)
const galleryFileInputs = ref([])
// Cache-busting timestamp - update mỗi khi cần force reload ảnh
const imageCacheBuster = ref(Date.now())

// Popup state
const showPopup = ref(false)
const popupMessage = ref('')
const popupType = ref('success') // 'success' or 'error'

// Show popup
function showSuccessPopup(message) {
  popupType.value = 'success'
  popupMessage.value = message
  showPopup.value = true
}

function showErrorPopup(message) {
  popupType.value = 'error'
  popupMessage.value = message
  showPopup.value = true
}

// Close popup
function closePopup() {
  showPopup.value = false
  popupMessage.value = ''
}

const form = reactive({
  id: null,
  name: '',
  price: 0,
  oldPrice: 0,
  bulkPrice: null,
  quantity: '',
  bulkQuantity: null,
  discount: 0,
  reviewCount: 0,
  shortDesc: '',
  benefits: '',
  targetUsers: '',
  usage: '',
  manufacturer: 'Công ty TNHH Thi Yên',
  ingredients: '',
  detailedUsage: '',
  specifications: '',
  technology: '',
  storage: '',
  image: '',
  gallery: [],
  galleryText: '',
  galleryImages: Array(11).fill(null).map(() => ({ path: null, preview: null, fileName: null, loading: false })),
  isNew: false,
  category: 'ME_DEN',
  status: 'ACTIVE',
  priority: 999,
  syncToPancake: false
})

// Computed property để tự động tính discount dựa trên price và oldPrice
const calculatedDiscount = computed(() => {
  const price = form.price || 0
  const oldPrice = form.oldPrice || 0
  
  // Nếu không có giá gốc hoặc giá gốc <= giá khuyến mãi, không có giảm giá
  if (!oldPrice || oldPrice <= price) {
    return 0
  }
  
  // Tính phần trăm giảm giá: ((oldPrice - price) / oldPrice) * 100
  const discount = ((oldPrice - price) / oldPrice) * 100
  
  // Làm tròn đến số nguyên
  return Math.round(discount)
})

// Watch để tự động cập nhật form.discount khi calculatedDiscount thay đổi
watch(calculatedDiscount, (newDiscount) => {
  form.discount = newDiscount
}, { immediate: true })

// Fetch product by ID
async function fetchProduct() {
  if (!productId.value) {
    error.value = 'Không có ID sản phẩm'
    return
  }

  loading.value = true
  error.value = ''
  try {
    const product = await productManagementAPI.getProductById(productId.value)
    
    // Debug: Kiểm tra dữ liệu product
    console.log('=== FETCH PRODUCT ===')
    console.log('Product:', product)
    console.log('Product.gallery:', product.gallery)
    console.log('Product.gallery type:', typeof product.gallery)
    console.log('Product.gallery is array:', Array.isArray(product.gallery))
    console.log('Product.mainImage:', product.mainImage)
    
    // Lấy ảnh chính
    const mainImage = product.mainImage || product.image || ''
    
    // Lấy gallery - đảm bảo là array
    let gallery = []
    if (product.gallery) {
      if (Array.isArray(product.gallery)) {
        gallery = product.gallery
      } else {
        console.warn('Product.gallery is not an array:', product.gallery)
        gallery = []
      }
    }
    
    // Hàm extract số từ tên file (ví dụ: /images/products/details/black/1.png -> 1)
    // Hỗ trợ cả format: 1.png, 0.png, 10.png, 11.png
    const extractImageNumber = (path) => {
      if (!path) return null
      // Match số ở cuối path trước extension (ví dụ: .../1.png, .../10.png)
      const match = path.match(/\/(\d+)\.(png|jpg|jpeg|jpeg)$/i)
      if (match) {
        const num = parseInt(match[1], 10)
        // Chỉ chấp nhận số từ 0-11 (vì có thể có 0.png trong mix folder)
        if (num >= 0 && num <= 11) {
          // Map 0 -> 1 (vì slot bắt đầu từ 1)
          return num === 0 ? 1 : num
        }
      }
      return null
    }
    
    // Tạo map từ số ảnh -> path để mapping chính xác
    const imageMap = new Map()
    
    // Thêm main_image vào map nếu có số trong tên file
    if (mainImage) {
      const mainImageNum = extractImageNumber(mainImage)
      if (mainImageNum !== null && mainImageNum >= 1 && mainImageNum <= 11) {
        imageMap.set(mainImageNum, mainImage)
      }
    }
    
    // Thêm gallery images vào map
    // Nếu gallery có ảnh trùng số với main_image, bỏ qua để tránh duplicate
    gallery.forEach((path, idx) => {
      // Bỏ qua nếu path trùng với main_image
      if (mainImage && path === mainImage) {
        console.log(`Gallery[${idx}]: Skipping ${path} (same as main_image)`)
        return
      }
      
      const num = extractImageNumber(path)
      console.log(`Gallery[${idx}]: ${path} -> extracted number: ${num}`)
      
      if (num !== null && num >= 1 && num <= 11) {
        // Chỉ thêm nếu chưa có số này trong map (để tránh duplicate với main_image)
        if (!imageMap.has(num)) {
          imageMap.set(num, path)
          console.log(`  -> Added to map: ${num} -> ${path}`)
        } else {
          console.log(`  -> Skipped (already in map): ${num}`)
        }
      } else {
        console.log(`  -> Skipped (invalid number or out of range): ${num}`)
      }
    })
    
    // Debug log để kiểm tra
    console.log('=== DEBUG GALLERY MAPPING ===')
    console.log('Product ID:', productId.value)
    console.log('Main image:', mainImage)
    console.log('Main image number:', extractImageNumber(mainImage))
    console.log('Gallery array:', gallery)
    console.log('Gallery length:', gallery?.length)
    console.log('Image map (before gallery):', Array.from(imageMap.entries()))
    console.log('Final image map:', Array.from(imageMap.entries()))
    
    // Khởi tạo galleryImages với 11 slots, map theo số trong tên file
    // Slot index 0 = ảnh số 1, index 1 = ảnh số 2, ...
    const galleryImages = Array(11).fill(null).map((_, index) => {
      // Số ảnh = index + 1 (vì ảnh bắt đầu từ 1, không phải 0)
      const imageNumber = index + 1
      const existingPath = imageMap.get(imageNumber) || null
      
      // Extract tên file từ path
      const fileName = existingPath ? existingPath.split('/').pop() : null
      
      return {
        path: existingPath,
        preview: null, // Không lưu preview URL với timestamp, sẽ generate mới mỗi lần render
        fileName: fileName, // Tên file để hiển thị
        loading: false
      }
    })
    
    // Debug: Log preview URLs sau khi khởi tạo
    const previewUrls = galleryImages.map((item, idx) => ({
      index: idx,
      path: item.path,
      preview: item.preview,
      previewUrl: item.preview || (item.path ? getImageUrlFromApi(item.path) : null)
    }))
    console.log('Preview URLs:', previewUrls)
    console.log('=== END DEBUG ===')
    
    // QUAN TRỌNG: Update cache-busting timestamp khi fetch product để force reload ảnh
    imageCacheBuster.value = Date.now()
    
    Object.assign(form, {
      id: product.id,
      name: product.name || '',
      price: product.price || 0,
      oldPrice: product.oldPrice || 0,
      bulkPrice: product.bulkPrice || null,
      quantity: product.quantity || '',
      bulkQuantity: product.bulkQuantity || null,
      discount: product.discount || 0,
      reviewCount: product.reviewCount || 0,
      shortDesc: product.shortDesc || '',
      benefits: product.benefits || '',
      targetUsers: product.targetUsers || '',
      usage: product.usage || '',
      manufacturer: product.manufacturer || 'Công ty TNHH Thi Yên',
      ingredients: product.ingredients || '',
      detailedUsage: product.description || product.detailedUsage || '',
      specifications: product.specifications || '',
      technology: product.technology || '',
      storage: product.storage || '',
      image: mainImage,
      gallery: gallery,
      galleryText: gallery.length > 0 ? gallery.join('\n') : '',
      galleryImages: galleryImages,
      isNew: product.isNew || false,
      category: product.category || 'ME_DEN',
      status: product.status || 'ACTIVE',
      priority: product.priority || 999,
      syncToPancake: false
    })
  } catch (e) {
    error.value = e.message || 'Có lỗi xảy ra khi tải dữ liệu sản phẩm'
  } finally {
    loading.value = false
  }
}

// Submit form
async function submitForm() {
  submitting.value = true
  try {
    // Xử lý ảnh chính
    let imagePath = form.image
    if (imagePath && imagePath.startsWith('data:')) {
      showErrorPopup('Vui lòng nhập đường dẫn ảnh (path) thay vì chọn file. Ví dụ: /images/products/product1.jpg')
      submitting.value = false
      return
    }
    
    // Xử lý gallery từ galleryImages array (11 ảnh)
    let galleryImagePaths = form.galleryImages
      .map(item => item.path || null)
      .filter(path => path && !path.startsWith('data:'))
    
    // Loại bỏ ảnh chính khỏi gallery nếu có
    if (imagePath) {
      galleryImagePaths = galleryImagePaths.filter(img => img !== imagePath)
    }
    
    const productData = {
      id: form.id,
      name: form.name,
      price: form.price,
      oldPrice: form.oldPrice || null,
      quantity: form.quantity || '',
      discount: form.discount || 0,
      reviewCount: form.reviewCount || 0,
      shortDesc: form.shortDesc || '',
      description: form.detailedUsage || form.shortDesc || '',
      mainImage: imagePath || null,
      gallery: galleryImagePaths,
      stock: 100,
      category: form.category || 'ME_DEN',
      status: form.status || 'ACTIVE',
      priority: form.priority || 999,
      benefits: form.benefits || '',
      ingredients: form.ingredients || '',
      specifications: form.specifications || '',
      technology: form.technology || '',
      storage: form.storage || '',
      syncToPancake: form.syncToPancake
    }

    await productManagementAPI.updateProduct(form.id, productData)
    
    // Show success message
    let message = 'Cập nhật sản phẩm thành công!'
    if (form.syncToPancake) {
      message += '\nĐã đồng bộ lên Pancake POS thành công!'
    }
    showSuccessPopup(message)
    
    // QUAN TRỌNG: Refresh lại data từ API để hiển thị thông tin mới nhất
    // Điều này đảm bảo tất cả ảnh và dữ liệu được load lại từ server với cache-busting
    await fetchProduct()
    
    // QUAN TRỌNG: Update cache-busting timestamp để force reload tất cả ảnh sau khi update
    imageCacheBuster.value = Date.now()
    
    // Đợi một chút để user thấy popup, rồi mới redirect
    setTimeout(() => {
      closePopup()
      router.push('/admin/products')
    }, 2000)
  } catch (e) {
    showErrorPopup('Lỗi: ' + e.message)
  } finally {
    submitting.value = false
  }
}

// Handle file upload
function handleFileUpload(event) {
  const file = event.target.files[0]
  if (!file) return
  
  if (!file.type.startsWith('image/')) {
    showErrorPopup('Vui lòng chọn file ảnh hợp lệ')
    return
  }
  
  if (file.size > 5 * 1024 * 1024) {
    showErrorPopup('File ảnh không được lớn hơn 5MB')
    return
  }
  
  imageLoading.value = true
  
  const fileName = file.name
  const timestamp = Date.now()
  const sanitizedFileName = fileName.replace(/[^a-zA-Z0-9.-]/g, '_')
  const imagePath = `/images/products/${timestamp}_${sanitizedFileName}`
  
  form.image = imagePath
  
  const reader = new FileReader()
  reader.onload = (e) => {
    const previewDataUrl = e.target.result
    setTimeout(() => {
      const previewImg = document.querySelector('.preview-image')
      if (previewImg) {
        previewImg.src = previewDataUrl
      }
    }, 100)
    imageLoading.value = false
  }
    reader.onerror = () => {
      showErrorPopup('Có lỗi xảy ra khi đọc file')
      imageLoading.value = false
    }
  reader.readAsDataURL(file)
  
  console.log('Path sẽ được lưu:', imagePath)
}

// Clear image
function clearImage() {
  form.image = ''
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

// Helper function: Resize image
function resizeImage(file, maxWidth = 1920, maxHeight = 1920, quality = 0.9) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        // Calculate new dimensions
        let width = img.width
        let height = img.height
        
        if (width > maxWidth || height > maxHeight) {
          const ratio = Math.min(maxWidth / width, maxHeight / height)
          width = width * ratio
          height = height * ratio
        }
        
        // Create canvas to resize
        const canvas = document.createElement('canvas')
        canvas.width = width
        canvas.height = height
        const ctx = canvas.getContext('2d')
        ctx.drawImage(img, 0, 0, width, height)
        
        // Convert to blob
        canvas.toBlob(
          (blob) => {
            if (blob) {
              resolve(blob)
            } else {
              reject(new Error('Failed to resize image'))
            }
          },
          'image/jpeg',
          quality
        )
      }
      img.onerror = () => reject(new Error('Failed to load image'))
      img.src = e.target.result
    }
    reader.onerror = () => reject(new Error('Failed to read file'))
    reader.readAsDataURL(file)
  })
}

// Helper function: Convert blob to data URL for preview
function blobToDataURL(blob) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = () => reject(new Error('Failed to convert blob'))
    reader.readAsDataURL(blob)
  })
}

// Handle gallery file upload
async function handleGalleryFileUpload(event, index) {
  const file = event.target.files[0]
  if (!file) return
  
  if (!file.type.startsWith('image/')) {
    showErrorPopup('Vui lòng chọn file ảnh hợp lệ')
    return
  }
  
  const imageItem = form.galleryImages[index]
  if (!imageItem) return
  
  imageItem.loading = true
  
  try {
    // Resize ảnh nếu quá lớn (max 1920x1920, quality 0.9)
    const resizedBlob = await resizeImage(file, 1920, 1920, 0.9)
    console.log(`Original size: ${(file.size / 1024 / 1024).toFixed(2)}MB`)
    console.log(`Resized size: ${(resizedBlob.size / 1024 / 1024).toFixed(2)}MB`)
    
    // Tạo path mới dựa trên category và số ảnh (luôn đổi tên thành số.png)
    // Nếu có path cũ, giữ nguyên folder nhưng đổi tên file thành số.png
    // Nếu không có path cũ, tạo path mới dựa trên category
    const imageNumber = index + 1
    let newPath = imageItem.path
    
    if (newPath) {
      // Có path cũ: giữ nguyên folder, chỉ đổi tên file thành số.png
      const pathParts = newPath.split('/')
      pathParts[pathParts.length - 1] = `${imageNumber}.png` // Đổi tên file cuối cùng
      newPath = pathParts.join('/')
    } else {
      // Không có path cũ: tạo path mới dựa trên category
      const categoryMap = {
        'ME_DEN': 'black',
        'HONG_DAU': 'pink',
        'COMBO': 'mix'
      }
      const categoryFolder = categoryMap[form.category] || 'mix'
      newPath = `/images/products/details/${categoryFolder}/${imageNumber}.png`
    }
    
    const newFileName = `${imageNumber}.png`
    
    // Tạo preview tạm thời từ blob đã resize (để hiển thị ngay trước khi upload)
    const previewDataUrl = await blobToDataURL(resizedBlob)
    
    // Cập nhật imageItem tạm thời với preview từ blob (base64)
    // Sau khi upload thành công, preview sẽ được clear để load từ server
    imageItem.preview = previewDataUrl
    imageItem.path = newPath
    imageItem.fileName = newFileName
    imageItem.loading = false
    imageItem.resizedBlob = resizedBlob // Lưu blob để có thể upload sau
    
    console.log(`Ảnh ${index + 1} đã được resize và đổi tên thành: ${newFileName}`)
    console.log(`Path: ${newPath}`)
    
    // Tự động upload file lên server
    try {
      // Extract relative path (bỏ /images/ ở đầu)
      const relativePath = newPath.startsWith('/images/') 
        ? newPath.substring(8) // Bỏ "/images/"
        : newPath.startsWith('images/')
          ? newPath.substring(7) // Bỏ "images/"
          : newPath
      
      // Convert blob to File for upload
      const fileToUpload = new File([resizedBlob], newFileName, { type: 'image/jpeg' })
      
      // Upload to server
      const uploadedPath = await productManagementAPI.uploadImage(fileToUpload, relativePath)
      
      console.log(`✅ Upload thành công: ${uploadedPath}`)
      
      // Cập nhật path với path từ server
      imageItem.path = uploadedPath
      
      // QUAN TRỌNG: Clear preview và force reload từ server với cache-busting mới
      // Đợi một chút để server xử lý file xong, rồi mới cập nhật
      setTimeout(() => {
        // Force Vue reactivity update bằng cách tạo object mới
        // Clear preview để template sẽ generate URL mới với cache-busting timestamp mới
        const updatedItem = {
          ...imageItem,
          preview: null, // Clear preview để force reload từ server với cache-busting mới mỗi lần render
          path: uploadedPath
        }
        form.galleryImages[index] = updatedItem
        
        // QUAN TRỌNG: Update cache-busting timestamp để force Vue re-render tất cả ảnh
        imageCacheBuster.value = Date.now()
        
        console.log(`🔄 Đã cập nhật path: ${uploadedPath}, cache-buster: ${imageCacheBuster.value}`)
      }, 500) // Đợi 500ms để server xử lý file
      
    } catch (uploadError) {
      console.error(`❌ Lỗi khi upload ảnh ${index + 1}:`, uploadError)
      // Vẫn giữ preview và path, nhưng hiển thị cảnh báo
      showErrorPopup(`Lưu ý: Ảnh ${index + 1} đã được resize nhưng chưa upload lên server. Vui lòng thử lại hoặc upload thủ công.\nLỗi: ${uploadError.message}`)
    }
    
  } catch (error) {
    console.error('Error processing image:', error)
    showErrorPopup('Có lỗi xảy ra khi xử lý ảnh: ' + error.message)
    imageItem.loading = false
  }
  
  // Reset file input để có thể chọn lại file cùng một vị trí
  if (galleryFileInputs.value[index]) {
    galleryFileInputs.value[index].value = ''
  }
}

// Remove gallery image
function removeGalleryImage(index) {
  const imageItem = form.galleryImages[index]
  if (imageItem) {
    imageItem.path = null
    imageItem.preview = null
    imageItem.fileName = null
    imageItem.loading = false
  }
  
  // Reset file input
  if (galleryFileInputs.value[index]) {
    galleryFileInputs.value[index].value = ''
  }
}

// Handle gallery image error
function handleGalleryImageError(event, index) {
  console.error(`Lỗi khi load ảnh gallery ${index + 1}:`, event.target.src)
  const imageItem = form.galleryImages[index]
  if (imageItem) {
    // Nếu preview là URL từ API và lỗi, thử lại với path gốc
    if (imageItem.preview && imageItem.preview.startsWith('http')) {
      console.log('Retrying with original path:', imageItem.path)
      // Không làm gì, để browser tự xử lý fallback
    }
    // Ẩn ảnh lỗi
    event.target.style.display = 'none'
  }
}

// Handle image error
function handleImageError(event) {
  event.target.style.display = 'none'
  event.target.parentElement.innerHTML = `
    <div class="w-full h-full flex items-center justify-center text-red-500 text-xs">
      <div class="text-center">
        <svg class="w-6 h-6 mx-auto mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z"/>
        </svg>
        Lỗi tải ảnh
      </div>
    </div>
  `
}

// Go back
function goBack() {
  router.push('/admin/products')
}

// Initialize
onMounted(() => {
  productId.value = route.params.id
  if (productId.value) {
    fetchProduct()
  } else {
    error.value = 'Không có ID sản phẩm'
  }
})
</script>

