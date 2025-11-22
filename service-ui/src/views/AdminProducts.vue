<template>
  <AdminLayout>
    <div>
      <h2 class="text-2xl font-bold text-green-700 text-center mb-8">Sản Phẩm</h2>
      <div class="mb-4 flex justify-end">
        <button @click="openAdd" class="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 font-semibold">+ Thêm sản phẩm</button>
      </div>
      
      <!-- Loading state -->
      <div v-if="loading" class="text-center py-8">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-green-600"></div>
        <p class="mt-2 text-gray-600">Đang tải dữ liệu...</p>
      </div> 

      <!-- Error state -->
      <div v-else-if="error" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
        <p>{{ error }}</p>
        <button @click="fetchProducts" class="mt-2 text-red-600 underline">Thử lại</button>
      </div>

      <!-- Products table -->
      <div v-else class="bg-white rounded-xl shadow p-6 mb-6">
        <table class="min-w-full text-sm border rounded-xl">
          <thead class="bg-green-50">
            <tr>
              <th class="px-3 py-2 text-left font-bold">ID</th>
              <th class="px-3 py-2 text-left font-bold">Tên sản phẩm</th>
              <th class="px-3 py-2 text-left font-bold">Giá</th>
              <th class="px-3 py-2 text-left font-bold">Giá cũ</th>
              <th class="px-3 py-2 text-left font-bold">Quy cách</th>
              <th class="px-3 py-2 text-left font-bold">Giảm giá</th>
              <th class="px-3 py-2 text-left font-bold">Danh mục</th>
              <th class="px-3 py-2 text-left font-bold">Trạng thái</th>
              <th class="px-3 py-2 text-left font-bold">Ưu tiên</th>
              <th class="px-3 py-2 text-center font-bold">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="product in products" :key="product.id" class="border-b hover:bg-green-50">
              <td class="px-3 py-2 font-semibold">{{ product.id }}</td>
              <td class="px-3 py-2">{{ product.name }}</td>
              <td class="px-3 py-2">{{ formatPrice(product.price) }}</td>
              <td class="px-3 py-2">{{ formatPrice(product.oldPrice) }}</td>
              <td class="px-3 py-2">{{ product.quantity }}</td>
              <td class="px-3 py-2">{{ product.discount }}%</td>
              <td class="px-3 py-2">{{ getCategoryName(product.category) }}</td>
              <td class="px-3 py-2">
                <span 
                  :class="{
                    'px-2 py-1 rounded text-xs font-semibold': true,
                    'bg-green-100 text-green-800': product.status === 'ACTIVE',
                    'bg-gray-100 text-gray-800': product.status === 'INACTIVE'
                  }"
                >
                  {{ product.status === 'ACTIVE' ? 'Hiển thị' : 'Ẩn' }}
                </span>
              </td>
              <td class="px-3 py-2">
                <span class="px-2 py-1 rounded text-xs font-semibold bg-blue-100 text-blue-800">
                  {{ product.priority || 999 }}
                </span>
              </td>
              <td class="px-3 py-2 text-center">
                <button @click="goToDetail(product.id)" class="text-blue-600 hover:underline mr-2">Sửa</button>
                <button @click="toggleStatus(product.id)" class="text-yellow-600 hover:underline mr-2">
                  {{ product.status === 'ACTIVE' ? 'Ẩn' : 'Hiện' }}
                </button>
                <button @click="remove(product.id)" class="text-red-600 hover:underline">Xoá</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Modal Thêm/Sửa -->
      <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
        <div class="bg-white rounded-xl shadow-lg p-6 w-full max-w-md relative max-h-[80vh] overflow-y-auto">
          <h3 class="text-lg font-bold mb-4">{{ isEdit ? 'Sửa sản phẩm' : 'Thêm sản phẩm' }}</h3>
          <form @submit.prevent="submitForm">
            <div class="mb-3">
              <label class="block font-semibold mb-1">Tên sản phẩm *</label>
              <input v-model="form.name" required class="w-full border rounded px-3 py-2" />
            </div>
            <div class="mb-3">
              <label class="block font-semibold mb-1">Giá khuyến mãi (Giá bán) *</label>
              <input v-model.number="form.price" type="number" min="0" required class="w-full border rounded px-3 py-2" />
              <p class="text-xs text-gray-500 mt-1">Giá hiển thị cho khách hàng</p>
            </div>
            <div class="mb-3">
              <label class="block font-semibold mb-1">Giá gốc (Giá cũ)</label>
              <input v-model.number="form.oldPrice" type="number" min="0" class="w-full border rounded px-3 py-2" />
              <p class="text-xs text-gray-500 mt-1">Giá gốc trước khi giảm giá (để hiển thị giá gạch ngang)</p>
            </div>
            <div class="mb-3">
              <label class="block font-semibold mb-1">Số lượng/Quy cách</label>
              <input v-model="form.quantity" placeholder="VD: 2 lon x 600g" class="w-full border rounded px-3 py-2" />
            </div>
            <div class="mb-3">
              <label class="block font-semibold mb-1">Giảm giá (%)</label>
              <input v-model.number="form.discount" type="number" min="0" max="100" class="w-full border rounded px-3 py-2" />
            </div>
            <div class="mb-3">
              <label class="block font-semibold mb-1">Số đánh giá</label>
              <input v-model.number="form.reviewCount" type="number" min="0" class="w-full border rounded px-3 py-2" />
            </div>
            <div class="mb-3">
              <label class="block font-semibold mb-1">Mô tả ngắn</label>
              <textarea v-model="form.shortDesc" class="w-full border rounded px-3 py-2" rows="3" placeholder="Mô tả ngắn gọn về sản phẩm..."></textarea>
              <p class="text-xs text-gray-500 mt-1">Mô tả ngắn hiển thị ở danh sách sản phẩm</p>
            </div>
            <div class="mb-3">
              <label class="block font-semibold mb-1">Mô tả chi tiết sản phẩm</label>
              <textarea v-model="form.detailedUsage" class="w-full border rounded px-3 py-2" rows="4" placeholder="Mô tả chi tiết về sản phẩm..."></textarea>
              <p class="text-xs text-gray-500 mt-1">Mô tả chi tiết hiển thị ở trang chi tiết sản phẩm</p>
            </div>
            <div class="mb-3">
              <label class="block font-semibold mb-1">Công dụng</label>
              <textarea v-model="form.benefits" class="w-full border rounded px-3 py-2" rows="2"></textarea>
            </div>
            <div class="mb-3">
              <label class="block font-semibold mb-1">Đối tượng sử dụng</label>
              <textarea v-model="form.targetUsers" class="w-full border rounded px-3 py-2" rows="2"></textarea>
            </div>
            <div class="mb-3">
              <label class="block font-semibold mb-1">Cách sử dụng</label>
              <textarea v-model="form.usage" class="w-full border rounded px-3 py-2" rows="2"></textarea>
            </div>
            <div class="mb-3">
              <label class="block font-semibold mb-1">Nhà sản xuất</label>
              <input v-model="form.manufacturer" class="w-full border rounded px-3 py-2" />
            </div>
            <div class="mb-3">
              <label class="block font-semibold mb-1">Thành phần</label>
              <textarea v-model="form.ingredients" class="w-full border rounded px-3 py-2" rows="2"></textarea>
            </div>
            <div class="mb-3">
              <label class="block font-semibold mb-1">Hướng dẫn chi tiết</label>
              <textarea v-model="form.detailedUsage" class="w-full border rounded px-3 py-2" rows="2"></textarea>
            </div>
            <div class="mb-3">
              <label class="block font-semibold mb-1">Thông số kỹ thuật</label>
              <textarea v-model="form.specifications" class="w-full border rounded px-3 py-2" rows="2"></textarea>
            </div>
            <div class="mb-3">
              <label class="block font-semibold mb-1">Công nghệ</label>
              <textarea v-model="form.technology" class="w-full border rounded px-3 py-2" rows="2"></textarea>
            </div>
            <div class="mb-3">
              <label class="block font-semibold mb-1">Bảo quản</label>
              <textarea v-model="form.storage" class="w-full border rounded px-3 py-2" rows="2"></textarea>
            </div>
            
            <div class="mb-3">
              <label class="block font-semibold mb-1">Ảnh mô tả (Gallery)</label>
              <p class="text-xs text-gray-500 mb-2">Nhập các đường dẫn ảnh, mỗi ảnh một dòng</p>
              <textarea 
                v-model="form.galleryText" 
                class="w-full border rounded px-3 py-2" 
                rows="4" 
                placeholder="/images/products/product1.jpg&#10;/images/products/product2.jpg&#10;/images/products/product3.jpg"
              ></textarea>
              <p class="text-xs text-gray-500 mt-1">Mỗi dòng là một đường dẫn ảnh. Ảnh đầu tiên sẽ là ảnh chính nếu chưa có.</p>
              <div v-if="form.gallery && form.gallery.length > 0" class="mt-2">
                <div class="text-xs font-semibold mb-1">Ảnh hiện tại ({{ form.gallery.length }} ảnh):</div>
                <div class="flex flex-wrap gap-2">
                  <div v-for="(img, index) in form.gallery" :key="index" class="relative">
                    <img 
                      :src="img.startsWith('data:') ? img : getImageUrlWithCacheBusting(getImageUrlFromApi(img))" 
                      :alt="`Gallery ${index + 1}`" 
                      class="w-16 h-16 object-cover rounded border" 
                      @error="handleImageError" 
                    />
                    <button 
                      type="button"
                      @click="removeGalleryImage(index)"
                      class="absolute -top-1 -right-1 bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs hover:bg-red-600"
                    >
                      ×
                    </button>
                  </div>
                </div>
              </div>
            </div>
            <div class="mb-3">
              <label class="block font-semibold mb-1">Danh mục</label>
              <select v-model="form.category" class="w-full border rounded px-3 py-2">
                <option value="">Chọn danh mục</option>
                <option value="ME_DEN">Mè đen</option>
                <option value="HONG_DAU">Hồng đậu</option>
                <option value="COMBO">Combo</option>
              </select>
            </div>
            
            <div class="mb-3">
              <label class="block font-semibold mb-1">Trạng thái</label>
              <select v-model="form.status" class="w-full border rounded px-3 py-2">
                <option value="ACTIVE">Hiển thị (ACTIVE)</option>
                <option value="INACTIVE">Ẩn (INACTIVE)</option>
              </select>
            </div>
            <div class="mb-3">
              <label class="block font-semibold mb-1">Ưu tiên (Priority)</label>
              <input 
                v-model.number="form.priority" 
                type="number" 
                min="1" 
                class="w-full border rounded px-3 py-2" 
                placeholder="999"
              />
              <p class="text-xs text-gray-500 mt-1">
                Số nhỏ hơn = ưu tiên cao hơn. Sản phẩm có priority 1-4 sẽ hiển thị ở trang chủ (tối đa 4 sản phẩm).
              </p>
            </div>
            <div class="flex justify-end gap-2 mt-4">
              <button type="button" @click="closeModal" class="px-4 py-2 rounded border">Huỷ</button>
              <button type="submit" :disabled="submitting" class="px-4 py-2 rounded bg-green-600 text-white font-semibold disabled:opacity-50">
                {{ submitting ? 'Đang lưu...' : 'Lưu' }}
              </button>
            </div>
          </form>
          <button @click="closeModal" class="absolute top-2 right-2 text-gray-400 hover:text-black">✕</button>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from './AdminLayout.vue'
import { productManagementAPI } from '@/utils/api.js'
import { getImageUrlFromApi, getImageUrlWithCacheBusting } from '@/utils/imageUtils.js'

const router = useRouter()

const products = ref([])
const loading = ref(false)
const error = ref('')
const submitting = ref(false)

const showModal = ref(false)
const isEdit = ref(false)
const fileInput = ref(null)
const imageLoading = ref(false)
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
  isNew: false,
  category: 'me-den',
  status: 'ACTIVE',
  priority: 999,
  syncToPancake: false
})

// Fetch products from API
async function fetchProducts() {
  loading.value = true
  error.value = ''
  try {
    const data = await productManagementAPI.getProducts()
    const allProducts = Array.isArray(data) ? data : (data.data || [])
    // CHỈ lọc sản phẩm đã bị xóa (soft delete)
    // KHÔNG lọc theo status - hiển thị cả ACTIVE và INACTIVE trong admin list
    // Sản phẩm INACTIVE sẽ không hiển thị cho user nhưng vẫn hiển thị cho admin
    products.value = allProducts.filter(product => !product.deleted)
  } catch (e) {
    error.value = e.message || 'Có lỗi xảy ra khi tải dữ liệu'
  } finally {
    loading.value = false
  }
}

// Create new product
async function createProduct(productData) {
  try {
    const data = await productManagementAPI.createProduct(productData)
    return data
  } catch (e) {
    throw new Error(e.message || 'Có lỗi xảy ra khi tạo sản phẩm')
  }
}

// Update product
async function updateProduct(id, productData) {
  try {
    const data = await productManagementAPI.updateProduct(id, productData)
    return data
  } catch (e) {
    throw new Error(e.message || 'Có lỗi xảy ra khi cập nhật sản phẩm')
  }
}

// Delete product
async function deleteProduct(id) {
  try {
    const data = await productManagementAPI.deleteProduct(id)
    return data
  } catch (e) {
    throw new Error(e.message || 'Có lỗi xảy ra khi xóa sản phẩm')
  }
}

function openAdd() {
  isEdit.value = false
  Object.assign(form, {
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
    isNew: false,
    category: 'ME_DEN',
    status: 'ACTIVE',
    priority: 999,
    syncToPancake: false
  })
  showModal.value = true
}

function goToDetail(id) {
  router.push(`/admin/products/${id}`)
}

function openEdit(product) {
  // Navigate to detail page instead of opening modal
  goToDetail(product.id)
}

function closeModal() {
  showModal.value = false
}

async function submitForm() {
  submitting.value = true
  try {
    // Xử lý ảnh chính
    let imagePath = form.image
    if (imagePath && imagePath.startsWith('data:')) {
      alert('Vui lòng nhập đường dẫn ảnh (path) thay vì chọn file. Ví dụ: /images/products/product1.jpg')
      submitting.value = false
      return
    }
    
    // Xử lý gallery từ textarea
    let galleryImages = []
    if (form.galleryText && form.galleryText.trim()) {
      // Tách từng dòng và lọc bỏ dòng trống
      galleryImages = form.galleryText
        .split('\n')
        .map(line => line.trim())
        .filter(line => line && !line.startsWith('data:')) // Bỏ qua base64 và dòng trống
    } else if (form.gallery && form.gallery.length > 0) {
      // Nếu không có text nhưng có gallery array, dùng array
      galleryImages = form.gallery.filter(img => img && !img.startsWith('data:'))
    }
    
    // Loại bỏ ảnh chính khỏi gallery nếu có (để tránh trùng lặp)
    if (imagePath) {
      galleryImages = galleryImages.filter(img => img !== imagePath)
    }
    
    const productData = {
      id: form.id, // Include ID for update
      name: form.name,
      price: form.price,
      oldPrice: form.oldPrice || null,
      quantity: form.quantity || '',
      discount: form.discount || 0,
      reviewCount: form.reviewCount || 0,
      shortDesc: form.shortDesc || '',
      description: form.detailedUsage || form.shortDesc || '', // Mô tả chi tiết
      mainImage: imagePath || null, // Ảnh chính
      gallery: galleryImages, // Danh sách ảnh mô tả
      stock: 100, // Default stock if not provided
      category: form.category || 'ME_DEN',
      status: form.status || 'ACTIVE',
      priority: form.priority || 999,
      benefits: form.benefits || '',
      ingredients: form.ingredients || '',
      specifications: form.specifications || '',
      technology: form.technology || '',
      storage: form.storage || '',
      syncToPancake: form.syncToPancake // Flag đồng bộ với Pancake POS
    }

    if (isEdit.value) {
      // Update existing product
      await updateProduct(form.id, productData)
    } else {
      // Create new product
      await createProduct(productData)
    }

    // Refresh the products list
    await fetchProducts()
    showModal.value = false
    
    // Show success message
    let message = isEdit.value ? 'Cập nhật sản phẩm thành công!' : 'Thêm sản phẩm thành công!'
    if (form.syncToPancake) {
      message += '\nĐã đồng bộ lên Pancake POS thành công!'
    }
    alert(message)
  } catch (e) {
    alert('Lỗi: ' + e.message)
  } finally {
    submitting.value = false
  }
}

async function remove(id) {
  if (confirm('Bạn có chắc muốn xoá sản phẩm này?')) {
    try {
      await deleteProduct(id)
      await fetchProducts() // Refresh the list
      alert('Xóa sản phẩm thành công!')
    } catch (e) {
      alert('Lỗi: ' + e.message)
    }
  }
}

async function toggleStatus(id) {
  try {
    await productManagementAPI.toggleProductStatus(id)
    await fetchProducts() // Refresh the list
    alert('Thay đổi trạng thái sản phẩm thành công!')
  } catch (e) {
    alert('Lỗi: ' + e.message)
  }
}

function formatPrice(val) {
  if (!val) return '0₫'
  return Number(val).toLocaleString('vi-VN') + '₫'
}

function getCategoryName(categoryCode) {
  const categoryMap = {
    'ME_DEN': 'Mè đen',
    'HONG_DAU': 'Hồng đậu',
    'COMBO': 'Combo'
  }
  return categoryMap[categoryCode] || categoryCode || 'Chưa phân loại'
}

// Handle file upload - chỉ tạo path từ tên file, không lưu base64
function handleFileUpload(event) {
  const file = event.target.files[0]
  if (!file) return
  
  // Validate file type
  if (!file.type.startsWith('image/')) {
    alert('Vui lòng chọn file ảnh hợp lệ')
    return
  }
  
  // Validate file size (max 5MB)
  if (file.size > 5 * 1024 * 1024) {
    alert('File ảnh không được lớn hơn 5MB')
    return
  }
  
  imageLoading.value = true
  
  // Tạo path từ tên file - giả sử file được lưu trong /images/products/
  const fileName = file.name
  const timestamp = Date.now()
  const sanitizedFileName = fileName.replace(/[^a-zA-Z0-9.-]/g, '_')
  const imagePath = `/images/products/${timestamp}_${sanitizedFileName}`
  
  // Chỉ lưu path vào form.image, không lưu base64
  form.image = imagePath
  
  // Tạo preview URL tạm thời để hiển thị (chỉ để preview)
  const reader = new FileReader()
  reader.onload = (e) => {
    // Lưu tạm vào một biến để preview, nhưng form.image vẫn là path
    const previewDataUrl = e.target.result
    // Cập nhật preview image nếu có
    setTimeout(() => {
      const previewImg = document.querySelector('.preview-image')
      if (previewImg) {
        previewImg.src = previewDataUrl
      }
    }, 100)
    imageLoading.value = false
  }
  reader.onerror = () => {
    alert('Có lỗi xảy ra khi đọc file')
    imageLoading.value = false
  }
  reader.readAsDataURL(file)
  
  // Thông báo cho user
  console.log('Path sẽ được lưu:', imagePath)
  console.log('Lưu ý: Bạn cần upload file lên server tại path này trước khi lưu sản phẩm')
}

// Clear image
function clearImage() {
  form.image = ''
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

// Remove gallery image
function removeGalleryImage(index) {
  if (form.gallery && form.gallery.length > index) {
    form.gallery.splice(index, 1)
    // Update galleryText
    form.galleryText = form.gallery.join('\n')
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

onMounted(() => {
  fetchProducts()
})
</script> 