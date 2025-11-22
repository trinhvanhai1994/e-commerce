<template>
  <AdminLayout>
    <div>
      <h2 class="text-2xl font-bold text-green-700 text-center mb-8">Đơn Hàng</h2>
      
      <!-- Loading state -->
      <div v-if="loading" class="text-center py-12">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-green-500 mx-auto"></div>
        <p class="mt-4 text-gray-600">Đang tải dữ liệu đơn hàng...</p>
      </div>

      <!-- Error state -->
      <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
        <div class="flex">
          <svg class="w-5 h-5 text-red-400 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"></path>
          </svg>
          <div class="ml-3">
            <h3 class="text-sm font-medium text-red-800">Lỗi tải dữ liệu</h3>
            <p class="text-sm text-red-700 mt-1">{{ error }}</p>
            <button @click="loadOrders" class="mt-2 text-sm text-red-600 hover:text-red-500 font-medium">
              Thử lại
            </button>
          </div>
        </div>
      </div>

      <!-- Main content -->
      <div v-else class="bg-white rounded-xl shadow p-6 mb-6">
        <form class="flex flex-wrap gap-4 items-end justify-center mb-4">
          <div>
            <label class="block text-gray-700 text-sm font-semibold mb-1">Mã Đơn Hàng</label>
            <input v-model="filters.orderId" class="border rounded px-3 py-2 w-40" />
          </div>
          <div>
            <label class="block text-gray-700 text-sm font-semibold mb-1">Trạng Thái</label>
            <select v-model="filters.status" class="border rounded px-3 py-2 w-40">
              <option value="">Tất Cả</option>
              <option :value="ORDER_STATUS.ORDER_STATUS_PENDING">Chờ xác nhận</option>
              <option :value="ORDER_STATUS.ORDER_STATUS_CONFIRMED">Đã xác nhận</option>
              <option :value="ORDER_STATUS.ORDER_STATUS_SHIPPING">Đang giao</option>
              <option :value="ORDER_STATUS.ORDER_STATUS_DELIVERED">Đã giao</option>
              <option :value="ORDER_STATUS.ORDER_STATUS_CANCELLED">Đã hủy</option>
            </select>
          </div>
          <div>
            <label class="block text-gray-700 text-sm font-semibold mb-1">Thanh Toán</label>
            <select v-model="filters.paymentMethod" class="border rounded px-3 py-2 w-32">
              <option value="">Tất Cả</option>
              <option value="COD">COD</option>
              <option value="TRANSFER">TRANSFER</option>
            </select>
          </div>
          <div>
            <label class="block text-gray-700 text-sm font-semibold mb-1">Ngày Tạo Đơn</label>
            <input v-model="filters.dateFrom" type="date" class="border rounded px-3 py-2 w-36" />
          </div>
          <div>
            <label class="block text-gray-700 text-sm font-semibold mb-1">&nbsp;</label>
            <input v-model="filters.dateTo" type="date" class="border rounded px-3 py-2 w-36" />
          </div>
          <div>
            <label class="block text-gray-700 text-sm font-semibold mb-1">Sắp xếp theo</label>
            <select v-model="sortBy" class="border rounded px-3 py-2 w-36">
              <option value="date">Ngày đặt hàng</option>
              <option value="status">Trạng thái</option>
              <option value="total">Tổng tiền</option>
              <option value="id">Mã đơn hàng</option>
            </select>
          </div>
          <div>
            <label class="block text-gray-700 text-sm font-semibold mb-1">Thứ tự</label>
            <select v-model="sortOrder" class="border rounded px-3 py-2 w-28">
              <option value="desc">Giảm dần</option>
              <option value="asc">Tăng dần</option>
            </select>
          </div>
          <button type="button" @click="applyFilter" class="bg-green-600 hover:bg-green-700 text-white font-bold px-6 py-2 rounded-lg transition">Tìm kiếm</button>
        </form>
        
        <div class="overflow-x-auto">
          <table class="w-full text-sm border rounded-xl table-fixed">
            <thead class="bg-green-50">
              <tr>
                <th class="px-3 py-2 text-left font-bold w-24">
                  Mã Đơn
                  <span v-if="sortBy === 'id'" class="ml-1 text-xs">
                    <svg v-if="sortOrder === 'desc'" class="w-3 h-3 inline" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"/>
                    </svg>
                    <svg v-else class="w-3 h-3 inline" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M14.707 12.707a1 1 0 01-1.414 0L10 9.414l-3.293 3.293a1 1 0 01-1.414-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 010 1.414z"/>
                    </svg>
                  </span>
                </th>
                <th class="px-3 py-2 text-left font-bold w-48">Thông Tin Khách Hàng</th>
                <th class="px-3 py-2 text-left font-bold w-32">
                  Ngày Đặt Hàng
                  <span v-if="sortBy === 'date'" class="ml-1 text-xs">
                    <svg v-if="sortOrder === 'desc'" class="w-3 h-3 inline" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"/>
                    </svg>
                    <svg v-else class="w-3 h-3 inline" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M14.707 12.707a1 1 0 01-1.414 0L10 9.414l-3.293 3.293a1 1 0 01-1.414-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 010 1.414z"/>
                    </svg>
                  </span>
                </th>
                <th class="px-3 py-2 text-left font-bold w-64">Địa Chỉ & Ghi Chú</th>
                <th class="px-3 py-2 text-left font-bold w-40">
                  Trạng Thái
                  <span v-if="sortBy === 'status'" class="ml-1 text-xs">
                    <svg v-if="sortOrder === 'desc'" class="w-3 h-3 inline" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"/>
                    </svg>
                    <svg v-else class="w-3 h-3 inline" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M14.707 12.707a1 1 0 01-1.414 0L10 9.414l-3.293 3.293a1 1 0 01-1.414-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 010 1.414z"/>
                    </svg>
                  </span>
                </th>
                <th class="px-3 py-2 text-left font-bold w-32">Phương Thức Thanh Toán</th>
                <th class="px-3 py-2 text-left font-bold w-28">
                  Tổng Tiền
                  <span v-if="sortBy === 'total'" class="ml-1 text-xs">
                    <svg v-if="sortOrder === 'desc'" class="w-3 h-3 inline" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"/>
                    </svg>
                    <svg v-else class="w-3 h-3 inline" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M14.707 12.707a1 1 0 01-1.414 0L10 9.414l-3.293 3.293a1 1 0 01-1.414-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 010 1.414z"/>
                    </svg>
                  </span>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(order, index) in pagedOrders" :key="getOrderId(order)" class="border-b hover:bg-green-50 cursor-pointer" @click="showOrderDetail(order)">
                <td class="px-3 py-2 text-green-700 font-semibold">{{ getOrderId(order) }}</td>
                <td class="px-3 py-2">
                  <div class="space-y-1">
                    <div class="font-semibold text-gray-900">
                      {{ getCustomerName(order) }}
                    </div>
                    <div class="text-sm text-gray-600">
                      📞 {{ getCustomerPhone(order) }}
                    </div>
                  </div>
                </td>
                <td class="px-3 py-2">{{ formatDate(order.createdAt || order.date) }}</td>
                <td class="px-3 py-2">
                  <div class="space-y-1">
                    <div class="text-sm">
                      📍 {{ getCustomerAddress(order) }}
                    </div>
                    <div v-if="getCustomerNotes(order)" class="text-xs text-blue-600 bg-blue-50 px-2 py-1 rounded">
                      💬 {{ getCustomerNotes(order) }}
                    </div>
                  </div>
                </td>
                <td class="px-3 py-2">
                  <!-- Status update dropdown for admin -->
                  <select 
                    @click.stop
                    @change="handleStatusChange(order, $event, index)"
                    :value="getOrderStatus(order)"
                    class="px-3 py-2 text-sm border rounded focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500 w-full font-medium cursor-pointer"
                    :class="getStatusSelectClass(getOrderStatus(order))"
                    :data-order-id="getOrderId(order)"
                  >
                    <option :value="ORDER_STATUS.ORDER_STATUS_PENDING">Chờ xác nhận</option>
                    <option :value="ORDER_STATUS.ORDER_STATUS_CONFIRMED">Đã xác nhận</option>
                    <option :value="ORDER_STATUS.ORDER_STATUS_SHIPPING">Đang giao</option>
                    <option :value="ORDER_STATUS.ORDER_STATUS_DELIVERED">Đã giao</option>
                    <option :value="ORDER_STATUS.ORDER_STATUS_CANCELLED">Đã hủy</option>
                  </select>
                </td>
                <td class="px-3 py-2">{{ order.paymentMethod || order.payment }}</td>
                <td class="px-3 py-2 font-semibold">{{ formatPrice(order.total) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <div class="flex justify-between items-center mt-4">
          <div class="text-xs text-gray-500">
            Hiển thị: {{ startIdx + 1 }} ~ {{ endIdx }} / Tổng Số Bản ghi là: {{ filteredOrders.length }}
          </div>
          <div class="flex gap-1">
            <button @click="goToPage(1)" :disabled="page === 1" class="px-2 py-1 rounded border text-xs" :class="page === 1 ? 'bg-gray-100 text-gray-400' : 'bg-white hover:bg-green-50'">Trang Đầu</button>
            <button @click="goToPage(page - 1)" :disabled="page === 1" class="px-2 py-1 rounded border text-xs" :class="page === 1 ? 'bg-gray-100 text-gray-400' : 'bg-white hover:bg-green-50'">Trước</button>
            <button v-for="p in totalPages" :key="p" @click="goToPage(p)" :class="['px-2 py-1 rounded border text-xs', page === p ? 'bg-green-500 text-white' : 'bg-white hover:bg-green-50']">{{ p }}</button>
            <button @click="goToPage(page + 1)" :disabled="page === totalPages" class="px-2 py-1 rounded border text-xs" :class="page === totalPages ? 'bg-gray-100 text-gray-400' : 'bg-white hover:bg-green-50'">Sau</button>
            <button @click="goToPage(totalPages)" :disabled="page === totalPages" class="px-2 py-1 rounded border text-xs" :class="page === totalPages ? 'bg-gray-100 text-gray-400' : 'bg-white hover:bg-green-50'">Trang Cuối</button>
          </div>
        </div>
      </div>

      <!-- Confirmation Modal -->
      <div v-if="showConfirmModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white rounded-lg p-6 max-w-md w-full mx-4">
          <div class="flex items-center mb-4">
            <div class="flex-shrink-0">
              <svg class="h-6 w-6 text-yellow-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
              </svg>
            </div>
            <div class="ml-3">
              <h3 class="text-lg font-medium text-gray-900">Xác nhận cập nhật trạng thái</h3>
            </div>
          </div>
          
          <div class="mb-6">
            <p class="text-sm text-gray-600 mb-2">
              Bạn có chắc chắn muốn cập nhật trạng thái đơn hàng <strong>{{ pendingUpdate.orderId }}</strong>?
            </p>
            <div class="bg-gray-50 rounded-lg p-3">
              <div class="flex items-center justify-between">
                <span class="text-sm text-gray-600">Trạng thái hiện tại:</span>
                <span class="px-2 py-1 text-xs rounded-full font-medium" :class="getStatusClass(pendingUpdate.currentStatus)">
                  {{ getStatusText(pendingUpdate.currentStatus) }}
                </span>
              </div>
              <div class="flex items-center justify-between mt-2">
                <span class="text-sm text-gray-600">Trạng thái mới:</span>
                <span class="px-2 py-1 text-xs rounded-full font-medium" :class="getStatusClass(pendingUpdate.newStatus)">
                  {{ getStatusText(pendingUpdate.newStatus) }}
                </span>
              </div>
            </div>
          </div>
          
          <div class="flex justify-end space-x-3">
            <button 
              @click="cancelUpdateStatus" 
              class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 border border-gray-300 rounded-md hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"
            >
              Hủy
            </button>
            <button 
              @click="confirmUpdateStatusAction" 
              class="px-4 py-2 text-sm font-medium text-white bg-green-600 border border-transparent rounded-md hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
            >
              Xác nhận
            </button>
          </div>
        </div>
      </div>

      <!-- Order Detail Modal -->
      <div v-if="showOrderDetailModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white rounded-lg p-6 max-w-4xl w-full mx-4 max-h-[90vh] overflow-y-auto">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-xl font-bold text-gray-900">Chi tiết đơn hàng #{{ selectedOrder?.id }}</h3>
            <button @click="closeOrderDetail" class="text-gray-400 hover:text-gray-600">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </button>
          </div>
          
          <div v-if="selectedOrder" class="space-y-6">
            <!-- Customer Information -->
            <div class="bg-gray-50 rounded-lg p-4">
              <h4 class="text-lg font-semibold text-gray-900 mb-3">Thông tin khách hàng</h4>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="text-sm font-medium text-gray-600">Tên khách hàng:</label>
                  <p class="text-gray-900">{{ getCustomerName(selectedOrder) }}</p>
                </div>
                <div>
                  <label class="text-sm font-medium text-gray-600">Số điện thoại:</label>
                  <p class="text-gray-900">{{ getCustomerPhone(selectedOrder) }}</p>
                </div>
                <div class="md:col-span-2">
                  <label class="text-sm font-medium text-gray-600">Địa chỉ:</label>
                  <p class="text-gray-900">{{ getCustomerAddress(selectedOrder) }}</p>
                </div>
                <div v-if="getCustomerNotes(selectedOrder)" class="md:col-span-2">
                  <label class="text-sm font-medium text-gray-600">Ghi chú:</label>
                  <p class="text-blue-600 bg-blue-50 p-2 rounded">{{ getCustomerNotes(selectedOrder) }}</p>
                </div>
              </div>
            </div>

            <!-- Order Information -->
            <div class="bg-gray-50 rounded-lg p-4">
              <h4 class="text-lg font-semibold text-gray-900 mb-3">Thông tin đơn hàng</h4>
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label class="text-sm font-medium text-gray-600">Ngày đặt hàng:</label>
                  <p class="text-gray-900">{{ formatDate(selectedOrder.createdAt || selectedOrder.date) }}</p>
                </div>
                <div>
                  <label class="text-sm font-medium text-gray-600">Trạng thái:</label>
                  <span class="px-2 py-1 text-xs rounded-full font-medium" :class="getStatusClass(getOrderStatus(selectedOrder))">
                    {{ getStatusText(getOrderStatus(selectedOrder)) }}
                  </span>
                </div>
                <div>
                  <label class="text-sm font-medium text-gray-600">Phương thức thanh toán:</label>
                  <p class="text-gray-900">{{ selectedOrder.paymentMethod || selectedOrder.payment }}</p>
                </div>
              </div>
            </div>

            <!-- Products List -->
            <div class="bg-gray-50 rounded-lg p-4">
              <h4 class="text-lg font-semibold text-gray-900 mb-3">Danh sách sản phẩm</h4>
              <div v-if="selectedOrder.items && selectedOrder.items.length > 0" class="space-y-3">
                <div v-for="(item, index) in selectedOrder.items" :key="index" class="bg-white rounded-lg p-4 border">
                  <div class="flex items-center space-x-4">
                    <div class="flex-shrink-0 relative">
                      <img 
                        :src="getProductImage(item)" 
                        :alt="item.name" 
                        class="w-16 h-16 object-cover rounded-lg cursor-pointer hover:opacity-80 transition-opacity"
                        @error="handleImageError($event, item)"
                        @click="openImageModal(getProductImage(item), getProductName(item))"
                      >
                      <div class="w-16 h-16 bg-gray-200 rounded-lg flex items-center justify-center absolute top-0 left-0" style="display: none;">
                        <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                        </svg>
                      </div>
                    </div>
                    <div class="flex-1">
                      <h5 class="font-semibold text-gray-900 text-lg">{{ getProductName(item) }}</h5>
                      <p v-if="getProductDescription(item)" class="text-sm text-gray-600 mt-1">{{ getProductDescription(item) }}</p>
                      <div class="flex items-center space-x-4 mt-2">
                        <span class="text-sm text-gray-600">Số lượng: <span class="font-medium">{{ item.quantity }}</span></span>
                        <span class="text-sm text-gray-600">Đơn giá: <span class="font-medium">{{ formatPrice(item.price) }}</span></span>
                        <span class="text-sm font-semibold text-green-600">Thành tiền: {{ formatPrice(item.price * item.quantity) }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="text-center py-8 text-gray-500">
                <svg class="w-12 h-12 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"></path>
                </svg>
                <p>Không có thông tin sản phẩm</p>
              </div>
            </div>

            <!-- Order Summary -->
            <div class="bg-green-50 rounded-lg p-4">
              <h4 class="text-lg font-semibold text-gray-900 mb-3">Tổng kết đơn hàng</h4>
              <div class="flex justify-between items-center">
                <span class="text-lg font-medium text-gray-900">Tổng tiền:</span>
                <span class="text-2xl font-bold text-green-600">{{ formatPrice(selectedOrder.total) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>

  <!-- Image Zoom Modal -->
  <div v-if="showImageModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-75" @click="closeImageModal">
    <div class="relative max-w-4xl max-h-full p-4" @click.stop>
      <!-- Close button -->
      <button 
        @click="closeImageModal"
        class="absolute top-2 right-2 z-10 bg-white bg-opacity-80 hover:bg-opacity-100 rounded-full p-2 transition-all duration-200"
      >
        <svg class="w-6 h-6 text-gray-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
        </svg>
      </button>
      
      <!-- Zoomed image -->
      <img 
        :src="zoomedImageSrc" 
        :alt="zoomedImageAlt"
        class="max-w-full max-h-full object-contain rounded-lg shadow-2xl"
        style="animation: zoomIn 0.3s ease-out;"
      >
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
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import AdminLayout from './AdminLayout.vue'
import { orderAPI } from '../utils/api'
import { 
  ORDER_STATUS, 
  getStatusText as getStatusTextUtil, 
  getStatusClass as getStatusClassUtil,
  getStatusSelectClass as getStatusSelectClassUtil 
} from '../constants/orderStatus.js'

const orders = ref([])
const loading = ref(true)
const error = ref(null)
const filters = ref({ orderId: '', status: '', paymentMethod: '', dateFrom: '', dateTo: '' })
const sortBy = ref('date') // 'date', 'status', 'total', 'id'
const sortOrder = ref('desc') // 'asc' hoặc 'desc'
const page = ref(1)
const pageSize = 10

// Confirmation modal state
const showConfirmModal = ref(false)
const pendingUpdate = ref({
  orderId: '',
  currentStatus: '',
  newStatus: '',
  orderIndex: -1
})

// Order detail modal state
const showOrderDetailModal = ref(false)
const selectedOrder = ref(null)

// Image zoom modal state
const showImageModal = ref(false)
const zoomedImageSrc = ref('')
const zoomedImageAlt = ref('')

// Popup state
const showPopup = ref(false)
const popupMessage = ref('')
const popupType = ref('success') // 'success' or 'error'

// Popup timeout reference
let popupTimeout = null

// Show popup
function showSuccessPopup(message) {
  popupType.value = 'success'
  popupMessage.value = message
  showPopup.value = true
  
  // Auto close after 3 seconds
  if (popupTimeout) clearTimeout(popupTimeout)
  popupTimeout = setTimeout(() => {
    closePopup()
  }, 3000)
}

function showErrorPopup(message) {
  popupType.value = 'error'
  popupMessage.value = message
  showPopup.value = true
  
  // Auto close after 4 seconds (errors need more time to read)
  if (popupTimeout) clearTimeout(popupTimeout)
  popupTimeout = setTimeout(() => {
    closePopup()
  }, 4000)
}

// Close popup
function closePopup() {
  if (popupTimeout) {
    clearTimeout(popupTimeout)
    popupTimeout = null
  }
  showPopup.value = false
  popupMessage.value = ''
}

// Load orders from API
async function loadOrders() {
  loading.value = true
  error.value = null
  
  try {
    const response = await orderAPI.getOrders()
    
    // Handle ApiResponse format: {success: true, data: [...]}
    // ServiceApiAdapter should extract data, so response should be array or {data: [...]}
    let ordersData = []
    if (Array.isArray(response)) {
      // Direct array (after ServiceApiAdapter processing)
      ordersData = response
    } else if (response && response.data && Array.isArray(response.data)) {
      // Nested in data
      ordersData = response.data
    } else if (response && response.orders && Array.isArray(response.orders)) {
      // Fallback format: {orders: [...]}
      ordersData = response.orders
    } else {
      console.error('Unexpected response format:', response)
      throw new Error('Invalid response format from API')
    }
    
    // Ensure status is properly set (UPPERCASE) and matches ORDER_STATUS constants
    // CRITICAL: Normalize status and ensure exact match with ORDER_STATUS constants
    ordersData = ordersData.map(order => {
      // Get original status
      let originalStatus = order.status
      
      // Normalize status to UPPERCASE if it exists
      let normalizedStatus = originalStatus
      if (normalizedStatus) {
        normalizedStatus = String(normalizedStatus).toUpperCase().trim()
      } else {
        normalizedStatus = ORDER_STATUS.ORDER_STATUS_PENDING
      }
      
      // Ensure status matches one of the valid ORDER_STATUS values
      const validStatuses = Object.values(ORDER_STATUS)
      if (!validStatuses.includes(normalizedStatus)) {
        console.warn(`Order ${getOrderId(order)}: Invalid status "${normalizedStatus}", defaulting to PENDING`)
        normalizedStatus = ORDER_STATUS.ORDER_STATUS_PENDING
      }
      
      // Create new object with normalized status
      // IMPORTANT: Use exact ORDER_STATUS constant value (e.g., ORDER_STATUS.ORDER_STATUS_CONFIRMED = 'CONFIRMED')
      const normalizedOrder = {
        ...order,
        status: normalizedStatus // Use normalized status directly (already UPPERCASE and validated)
      }
      
      return normalizedOrder
    })
    
    orders.value = ordersData
    
    // Force update select elements after data is loaded
    await nextTick()
    await nextTick() // Double nextTick to ensure DOM is fully rendered
    
    orders.value.forEach((order, index) => {
      const selectElement = document.querySelector(`select[data-order-id="${getOrderId(order)}"]`)
      if (selectElement) {
        const statusValue = order.status
        
        // Get all option values
        const options = Array.from(selectElement.options)
        
        // Set value
        selectElement.value = statusValue
        
        // If still not matching, use selectedIndex
        if (selectElement.value !== statusValue) {
          const optionIndex = options.findIndex(opt => String(opt.value) === String(statusValue))
          if (optionIndex >= 0) {
            selectElement.selectedIndex = optionIndex
          }
        }
      }
    })
    
  } catch (err) {
    console.error('Error loading orders:', err)
    error.value = err.message || 'Không thể tải dữ liệu đơn hàng'
    
    // Fallback to mock data if API fails
    orders.value = [
      { 
        id: '2507240002', 
        customerName: 'TRAN XUAN NGHIA', 
        createdAt: '2025-07-24 10:59:19', 
        address: 'C16 Khu đấu giá tân triều, thanh trì, hà nội Xã Tân Triều, Huyện Thanh Trì, Thành phố Hà Nội', 
        status: ORDER_STATUS.ORDER_STATUS_CONFIRMED, 
        paymentMethod: 'COD', 
        total: 618000, 
        type: 'THI_YEN',
        customerInfo: {
          name: 'TRAN XUAN NGHIA',
          phone: '0987654321',
          address: 'C16 Khu đấu giá tân triều, thanh trì, hà nội Xã Tân Triều, Huyện Thanh Trì, Thành phố Hà Nội',
          notes: 'Giao hàng vào buổi chiều, gọi trước khi giao'
        },
        items: [
          {
            productId: 1, // Test case: API trả về productId = 1 → "BỘT NGŨ HẮC MÈ ĐEN"
            name: '', // Test case: name rỗng, sẽ fallback to defaultProducts
            description: '', // Test case: description rỗng, sẽ fallback to defaultProducts
            price: 299000,
            quantity: 2,
            image: '', // Test case: image rỗng, sẽ fallback to defaultProducts
            imageUrl: '' // Test case: imageUrl rỗng
          },
          {
            productId: 2, // Test case: API trả về productId = 2 → "COMBO 2 LON BỘT NGŨ HẮC MÈ ĐEN"
            name: '', // Test case: name rỗng
            description: '', // Test case: description rỗng
            price: 499000,
            quantity: 1,
            image: '', // Test case: image rỗng
            imageUrl: '' // Test case: imageUrl rỗng
          },
          {
            productId: 99, // Test case: productId không có trong defaultProducts → "Sản phẩm #99"
            name: '', // Test case: name rỗng
            description: '', // Test case: description rỗng
            price: 199000,
            quantity: 1,
            image: '', // Test case: image rỗng
            imageUrl: '' // Test case: imageUrl rỗng
          }
        ]
      },
      { 
        id: '2507240001', 
        customerName: 'Phuong Thao Vu', 
        createdAt: '2025-07-24 07:19:45', 
        address: '72, nguyễn trãi, r5 royal city Phường Thượng Đình, Quận Thanh Xuân, Thành phố Hà Nội', 
        status: ORDER_STATUS.ORDER_STATUS_CONFIRMED, 
        paymentMethod: 'COD', 
        total: 598000, 
        type: 'THI_YEN',
        customerInfo: {
          name: 'Phuong Thao Vu',
          phone: '0912345678',
          address: '72, nguyễn trãi, r5 royal city Phường Thượng Đình, Quận Thanh Xuân, Thành phố Hà Nội',
          notes: 'Không có ghi chú'
        },
        items: [
          {
            id: 2, // Test case: API trả về id (không phải productId)
            productName: '', // Test case: productName rỗng, sẽ fallback to defaultProducts
            desc: '', // Test case: desc rỗng, sẽ fallback to defaultProducts
            price: 499000,
            quantity: 2,
            image: '', // Test case: image rỗng, sẽ fallback to defaultProducts
            imageUrl: 'https://invalid-url.com/image.jpg' // Test case: API trả về imageUrl lỗi
          }
        ]
      },
      { 
        id: '2507230003', 
        customerName: 'Đoàn Hải Nam', 
        createdAt: '2025-07-23 23:09:00', 
        address: '4 Phạm Sư Mạnh Phường Phan Chu Trinh, Quận Hoàn Kiếm, Thành phố Hà Nội', 
        status: ORDER_STATUS.ORDER_STATUS_SHIPPING, 
        paymentMethod: 'COD', 
        total: 618000, 
        type: 'THI_YEN',
        customerInfo: {
          name: 'Đoàn Hải Nam',
          phone: '0901234567',
          address: '4 Phạm Sư Mạnh Phường Phan Chu Trinh, Quận Hoàn Kiếm, Thành phố Hà Nội',
          notes: 'Giao hàng nhanh, khách hàng VIP'
        },
        items: [
          {
            id: 3, // Sẽ lấy từ defaultProducts: "BỘT NGŨ SẮC HỒNG ĐẬU"
            name: '', // Test case: name rỗng, sẽ fallback to defaultProducts
            description: '', // Test case: description rỗng, sẽ fallback to defaultProducts
            price: 299000,
            quantity: 2,
            image: '', // Test case: image rỗng, sẽ fallback to defaultProducts
            imageUrl: null // Test case: API không trả về imageUrl
          }
        ]
      },
      { 
        id: '2507230002', 
        customerName: 'Vĩ Bùi', 
        createdAt: '2025-07-23 12:43:35', 
        address: '444 Cách Mạng Tháng 8 Phường 11, Quận 3, Thành phố Hồ Chí Minh', 
        status: ORDER_STATUS.ORDER_STATUS_DELIVERED, 
        paymentMethod: 'COD', 
        total: 598000, 
        type: 'THI_YEN',
        customerInfo: {
          name: 'Vĩ Bùi',
          phone: '0923456789',
          address: '444 Cách Mạng Tháng 8 Phường 11, Quận 3, Thành phố Hồ Chí Minh',
          notes: null
        },
        items: [
          {
            id: 4, // Sẽ lấy từ defaultProducts: "COMBO 2 LON BỘT NGŨ SẮC HỒNG ĐẬU"
            name: '', // Test case: name rỗng, sẽ fallback to defaultProducts
            description: '', // Test case: description rỗng, sẽ fallback to defaultProducts
            price: 499000,
            quantity: 2,
            image: '', // Test case: image rỗng, sẽ fallback to defaultProducts
            imageUrl: undefined // Test case: API không có field imageUrl
          }
        ]
      },
      { 
        id: '2507230001', 
        customerName: 'Nguyen thanh vu', 
        createdAt: '2025-07-23 10:22:22', 
        address: '103/23 Hồ Thị Kỉ Phường 01, Quận 10, Thành phố Hồ Chí Minh', 
        status: ORDER_STATUS.ORDER_STATUS_CONFIRMED, 
        paymentMethod: 'COD', 
        total: 618000, 
        type: 'THI_YEN',
        customerInfo: {
          name: 'Nguyen thanh vu',
          phone: '0934567890',
          address: '103/23 Hồ Thị Kỉ Phường 01, Quận 10, Thành phố Hồ Chí Minh',
          notes: 'Đổi địa chỉ giao hàng, liên hệ trước'
        },
        items: [
          {
            id: 5, // Sẽ lấy từ defaultProducts: "COMBO 2 (1 BỘT NGŨ HẮC MÈ ĐEN + 1 BỘT NGŨ SẮC HỒNG ĐẬU)"
            name: '', // Test case: name rỗng, sẽ fallback to defaultProducts
            description: '', // Test case: description rỗng, sẽ fallback to defaultProducts
            price: 499000,
            quantity: 2,
            image: '', // Test case: image rỗng, sẽ fallback to defaultProducts
            imageUrl: '' // Test case: imageUrl rỗng
          }
        ]
      },
    ]
  } finally {
    loading.value = false
  }
}

// Format date
function formatDate(dateString) {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Format price
function formatPrice(price) {
  if (!price) return '0₫'
  return price.toLocaleString('vi-VN') + '₫'
}

// Helper functions to get customer info
// Helper function to get orderId (prefer orderId field, fallback to id)
function getOrderId(order) {
  return order?.orderId || order?.id || 'N/A'
}

function getCustomerName(order) {
  if (order.customerInfo && order.customerInfo.name) {
    return order.customerInfo.name
  }
  return order.customerName || order.customer || 'N/A'
}

function getCustomerPhone(order) {
  if (order.customerInfo && order.customerInfo.phone) {
    return order.customerInfo.phone
  }
  return order.phone || 'N/A'
}

function getCustomerAddress(order) {
  if (order.customerInfo && order.customerInfo.address) {
    return order.customerInfo.address
  }
  return order.address || 'N/A'
}

function getCustomerNotes(order) {
  if (order.customerInfo && order.customerInfo.notes) {
    return order.customerInfo.notes
  }
  return order.notes || null
}

// Product image helper functions - sử dụng utility từ productImage.js
import { getProductImage as getProductImageUtil } from '../utils/productImage'
import { getImageUrlFromApi } from '../utils/imageUtils.js'

function getProductImage(item) {
  // Nếu item có mainImage hoặc image, sử dụng utility function
  if (item.mainImage || item.image) {
    return getProductImageUtil(item)
  }
  
  // Nếu có imageUrl từ API và hợp lệ
  if (item.imageUrl && item.imageUrl.trim() !== '') {
    return item.imageUrl
  }
  
  // Fallback: sử dụng utility function với ID
  const productId = item.id || item.productId
  return getProductImageUtil(productId)
}

function getDefaultProductImage(productId) {
  // Đảm bảo productId là number
  const numericProductId = Number(productId)
  const product = defaultProducts.find(p => p.id === numericProductId)
  return product ? product.image : getImageUrlFromApi('/images/products/Combo-mix.png') // Ảnh mặc định chung
}

function handleImageError(event, item) {
  // Khi ảnh lỗi, thử ảnh mặc định theo ID (thử cả id và productId)
  const productId = item.id || item.productId
  const defaultImage = getDefaultProductImage(productId)
  
  // Nếu ảnh hiện tại không phải là ảnh mặc định, thử ảnh mặc định
  if (event.target.src !== defaultImage) {
    event.target.src = defaultImage
  } else {
    // Nếu ảnh mặc định cũng lỗi, hiển thị placeholder
    event.target.style.display = 'none'
    event.target.nextElementSibling.style.display = 'flex'
  }
}

// Product name and description helper functions
function getProductName(item) {
  // Thử các field có thể có tên sản phẩm
  if (item.name && item.name.trim() !== '') {
    return item.name
  }
  if (item.productName && item.productName.trim() !== '') {
    return item.productName
  }
  if (item.title && item.title.trim() !== '') {
    return item.title
  }
  if (item.productTitle && item.productTitle.trim() !== '') {
    return item.productTitle
  }
  
  // Fallback: tên mặc định theo ID (thử cả id và productId)
  const productId = item.id || item.productId
  return getDefaultProductName(productId)
}

function getProductDescription(item) {
  // Thử các field có thể có mô tả sản phẩm
  if (item.description && item.description.trim() !== '') {
    return item.description
  }
  if (item.desc && item.desc.trim() !== '') {
    return item.desc
  }
  if (item.productDescription && item.productDescription.trim() !== '') {
    return item.productDescription
  }
  
  // Fallback: mô tả mặc định theo ID (thử cả id và productId)
  const productId = item.id || item.productId
  return getDefaultProductDescription(productId)
}

// Danh sách sản phẩm mặc định - đồng nhất với Home.vue
const defaultProducts = [
  { 
    id: 1, 
    name: 'BỘT NGŨ HẮC MÈ ĐEN', 
    description: 'Bột Ngũ Hắc Mè Đen là bữa ăn thay thế tiện lợi, bổ dưỡng từ 5 loại hạt đen nguyên bản', 
    image: getImageUrlFromApi('/images/products/me-den.jpg'),
    category: 'me-den',
    price: 299000,
    oldPrice: 390000
  },
  { 
    id: 2, 
    name: 'COMBO 2 LON BỘT NGŨ HẮC MÈ ĐEN', 
    description: 'Combo tiết kiệm cho gia đình với 2 lon bột ngũ hắc mè đen', 
    image: getImageUrlFromApi('/images/products/combo-black.png'),
    category: 'combo',
    price: 499000,
    oldPrice: 780000
  },
  { 
    id: 3, 
    name: 'BỘT NGŨ SẮC HỒNG ĐẬU', 
    description: 'Bột Ngũ Sắc Hồng Đậu là bữa ăn thay thế tiện lợi, bổ dưỡng từ 5 loại hạt hồng đậu', 
    image: getImageUrlFromApi('/images/products/hong-dau.jpg'),
    category: 'hong-dau',
    price: 299000,
    oldPrice: 390000
  },
  { 
    id: 4, 
    name: 'COMBO 2 LON BỘT NGŨ SẮC HỒNG ĐẬU', 
    description: 'Combo tiết kiệm cho gia đình với 2 lon bột ngũ sắc hồng đậu', 
    image: getImageUrlFromApi('/images/products/combo-pink.png'),
    category: 'combo',
    price: 499000,
    oldPrice: 780000
  },
  { 
    id: 5, 
    name: 'COMBO 2 (1 BỘT NGŨ HẮC MÈ ĐEN + 1 BỘT NGŨ SẮC HỒNG ĐẬU)', 
    description: 'Combo tiết kiệm cho gia đình với 1 lon mè đen và 1 lon hồng đậu', 
    image: getImageUrlFromApi('/images/products/Combo-mix.png'),
    category: 'combo',
    price: 499000,
    oldPrice: 780000
  }
]

function getDefaultProductName(productId) {
  // Đảm bảo productId là number
  const numericProductId = Number(productId)
  const product = defaultProducts.find(p => p.id === numericProductId)
  return product ? product.name : `Sản phẩm #${productId}`
}

function getDefaultProductDescription(productId) {
  const product = defaultProducts.find(p => p.id === productId)
  return product ? product.description : null
}

// Get order status with normalization (for display)
function getOrderStatus(order) {
  if (!order) {
    return ORDER_STATUS.ORDER_STATUS_PENDING
  }
  
  let status = order.status
  if (status) {
    status = String(status).toUpperCase().trim()
  } else {
    status = ORDER_STATUS.ORDER_STATUS_PENDING
  }
  
  const validStatuses = Object.values(ORDER_STATUS)
  if (!validStatuses.includes(status)) {
    status = ORDER_STATUS.ORDER_STATUS_PENDING
  }
  
  return status
}

// Get order status value for select binding (must return exact string match)
// Note: This function is no longer used in template, but kept for force update logic
function getOrderStatusValue(order) {
  if (!order) {
    return ORDER_STATUS.ORDER_STATUS_PENDING
  }
  
  let status = order.status
  if (status) {
    status = String(status).toUpperCase().trim()
  } else {
    status = ORDER_STATUS.ORDER_STATUS_PENDING
  }
  
  // Ensure exact match with ORDER_STATUS constants
  const validStatuses = Object.values(ORDER_STATUS)
  if (!validStatuses.includes(status)) {
    status = ORDER_STATUS.ORDER_STATUS_PENDING
  }
  
  return String(status)
}

// Get select status value - wrapper for template binding
function getSelectStatusValue(order) {
  return getOrderStatusValue(order)
}

// Get status text - using constants
function getStatusText(status) {
  return getStatusTextUtil(status)
}

// Get status class - using constants
function getStatusClass(status) {
  return getStatusClassUtil(status)
}

// Get status select class - using constants
function getStatusSelectClass(status) {
  return getStatusSelectClassUtil(status)
}

// Handle status change from select dropdown
function handleStatusChange(order, event, orderIndex) {
  // Get the selected value from the select element
  const selectedValue = event.target.value
  
  // CRITICAL: Normalize to UPPERCASE immediately
  let normalizedStatus = selectedValue
  if (normalizedStatus) {
    // Convert to string, uppercase, and trim whitespace
    normalizedStatus = String(normalizedStatus).toUpperCase().trim()
  } else {
    normalizedStatus = ORDER_STATUS.ORDER_STATUS_PENDING
  }
  
      // Ensure it matches one of the valid ORDER_STATUS values
      const validStatuses = Object.values(ORDER_STATUS)
      if (!validStatuses.includes(normalizedStatus)) {
        console.warn(`handleStatusChange: Invalid status "${normalizedStatus}", defaulting to PENDING`)
        normalizedStatus = ORDER_STATUS.ORDER_STATUS_PENDING
      }
      
      // Call confirmUpdateStatus with normalized value
      confirmUpdateStatus(order, normalizedStatus, orderIndex)
}

// Confirmation modal functions
function confirmUpdateStatus(order, newStatus, orderIndex) {
  // newStatus should already be normalized to UPPERCASE from handleStatusChange
  // But we normalize again to be safe
  let normalizedStatus = newStatus
  if (normalizedStatus) {
    normalizedStatus = String(normalizedStatus).toUpperCase().trim()
  } else {
    normalizedStatus = ORDER_STATUS.ORDER_STATUS_PENDING
  }
  
  // Ensure it matches one of the valid ORDER_STATUS values
  const validStatuses = Object.values(ORDER_STATUS)
  if (!validStatuses.includes(normalizedStatus)) {
    normalizedStatus = ORDER_STATUS.ORDER_STATUS_PENDING
  }
  
  pendingUpdate.value = {
    orderId: getOrderId(order),
    currentStatus: getOrderStatus(order),
    newStatus: normalizedStatus, // Use normalized uppercase status
    orderIndex: orderIndex
  }
  showConfirmModal.value = true
}

async function cancelUpdateStatus() {
  showConfirmModal.value = false
  // Reset the dropdown to original value
  if (pendingUpdate.value.orderIndex !== -1) {
    const orderIndex = pendingUpdate.value.orderIndex
    // Reset status to currentStatus (already normalized to UPPERCASE)
    orders.value[orderIndex].status = pendingUpdate.value.currentStatus
    
    // Force update select element to show correct value
    await nextTick()
    const selectElement = document.querySelector(`select[data-order-id="${orders.value[orderIndex].id}"]`)
    if (selectElement) {
      selectElement.value = pendingUpdate.value.currentStatus
    }
  }
}

async function confirmUpdateStatusAction() {
  try {
    // CRITICAL: Normalize status to UPPERCASE before sending to API
    // Get the status from pendingUpdate (already normalized in confirmUpdateStatus)
    let statusToUpdate = pendingUpdate.value.newStatus
    
    // Double-check: Normalize again to ensure it's UPPERCASE
    if (statusToUpdate) {
      statusToUpdate = String(statusToUpdate).toUpperCase().trim()
    } else {
      statusToUpdate = ORDER_STATUS.ORDER_STATUS_PENDING
    }
    
    // Ensure it matches one of the valid ORDER_STATUS values
    const validStatuses = Object.values(ORDER_STATUS)
    if (!validStatuses.includes(statusToUpdate)) {
      console.warn(`confirmUpdateStatusAction: Invalid status "${statusToUpdate}", defaulting to PENDING`)
      statusToUpdate = ORDER_STATUS.ORDER_STATUS_PENDING
    }
    
    const response = await orderAPI.updateOrderStatus(pendingUpdate.value.orderId, statusToUpdate)
    
    // Handle ApiResponse format: {success: true, data: OrderResponse, message: "..."}
    // ServiceApiAdapter should extract data, so response should be OrderResponse or {success, data, message}
    let isSuccess = false
    let updatedStatus = statusToUpdate // Use normalized status
    
    if (response && (response.id || response.status)) {
      // OrderResponse object (after ServiceApiAdapter processing)
      isSuccess = true
      // Use status from response if available (should be UPPERCASE)
      if (response.status) {
        updatedStatus = String(response.status).toUpperCase().trim()
      }
    } else if (response && response.success) {
      // Format: {success: true, message: "..."}
      isSuccess = true
      // Check if response has data with status
      if (response.data && response.data.status) {
        updatedStatus = String(response.data.status).toUpperCase().trim()
      }
    } else if (response && response.data) {
      // Format: {data: OrderResponse}
      isSuccess = true
      if (response.data.status) {
        updatedStatus = String(response.data.status).toUpperCase().trim()
      }
    } else if (response && response.message) {
      // Format: {message: "..."}
      isSuccess = true
    } else {
      console.error('Unexpected update status response format:', response)
    }
    
    if (isSuccess) {
      // Update local state immediately for better UX
      if (pendingUpdate.value.orderIndex !== -1) {
        const orderIndex = pendingUpdate.value.orderIndex
        // Normalize status to UPPERCASE
        updatedStatus = String(updatedStatus).toUpperCase().trim()
        // Ensure it matches one of the valid ORDER_STATUS values
        const validStatuses = Object.values(ORDER_STATUS)
        if (!validStatuses.includes(updatedStatus)) {
          console.warn(`Invalid status "${updatedStatus}", defaulting to PENDING`)
          updatedStatus = ORDER_STATUS.ORDER_STATUS_PENDING
        }
        // Update status with normalized value
        orders.value[orderIndex].status = updatedStatus
        
        // Force update select element
        await nextTick()
        const selectElement = document.querySelector(`select[data-order-id="${orders.value[orderIndex].id}"]`)
        if (selectElement) {
          selectElement.value = updatedStatus
        }
      }
      
      showSuccessPopup('Cập nhật trạng thái đơn hàng thành công!')
      // Reload orders to reflect the change from server
      await loadOrders()
    } else {
      showErrorPopup('Cập nhật trạng thái đơn hàng thất bại: ' + (response && response.message || 'Lỗi không xác định'))
    }
  } catch (err) {
    console.error('Error updating order status:', err)
    showErrorPopup('Có lỗi xảy ra khi cập nhật trạng thái đơn hàng: ' + err.message)
  } finally {
    showConfirmModal.value = false
  }
}

const filteredOrders = computed(() => {
  let result = orders.value
  if (filters.value.orderId) {
    result = result.filter(o => {
      const orderId = getOrderId(o)
      return orderId.includes(filters.value.orderId)
    })
  }
  if (filters.value.status) {
    result = result.filter(o => getOrderStatus(o) === filters.value.status)
  }
  if (filters.value.paymentMethod) {
    result = result.filter(o => (o.paymentMethod || o.payment) === filters.value.paymentMethod)
  }
  if (filters.value.dateFrom) {
    result = result.filter(o => {
      const orderDate = new Date(o.createdAt || o.date)
      const filterDate = new Date(filters.value.dateFrom)
      return orderDate >= filterDate
    })
  }
  if (filters.value.dateTo) {
    result = result.filter(o => {
      const orderDate = new Date(o.createdAt || o.date)
      const filterDate = new Date(filters.value.dateTo)
      return orderDate <= filterDate
    })
  }
  
  // Sắp xếp theo tiêu chí được chọn
  result.sort((a, b) => {
    let valueA, valueB
    
    switch (sortBy.value) {
      case 'date':
        valueA = new Date(a.createdAt || a.date)
        valueB = new Date(b.createdAt || b.date)
        break
      case 'status':
        // Sắp xếp theo thứ tự ưu tiên trạng thái
        const statusOrder = { 
          [ORDER_STATUS.ORDER_STATUS_PENDING]: 1, 
          [ORDER_STATUS.ORDER_STATUS_CONFIRMED]: 2, 
          [ORDER_STATUS.ORDER_STATUS_SHIPPING]: 3, 
          [ORDER_STATUS.ORDER_STATUS_DELIVERED]: 4, 
          [ORDER_STATUS.ORDER_STATUS_CANCELLED]: 5 
        }
        valueA = statusOrder[getOrderStatus(a)] || 1
        valueB = statusOrder[getOrderStatus(b)] || 1
        break
      case 'total':
        valueA = a.total || 0
        valueB = b.total || 0
        break
      case 'id':
        valueA = a.id
        valueB = b.id
        break
      default:
        valueA = new Date(a.createdAt || a.date)
        valueB = new Date(b.createdAt || b.date)
    }
    
    if (sortOrder.value === 'desc') {
      return valueB > valueA ? 1 : valueB < valueA ? -1 : 0
    } else {
      return valueA > valueB ? 1 : valueA < valueB ? -1 : 0
    }
  })
  
  return result
})

const totalPages = computed(() => Math.ceil(filteredOrders.value.length / pageSize))
const startIdx = computed(() => (page.value - 1) * pageSize)
const endIdx = computed(() => Math.min(page.value * pageSize, filteredOrders.value.length))
const pagedOrders = computed(() => filteredOrders.value.slice(startIdx.value, endIdx.value))

function goToPage(p) {
  if (p < 1 || p > totalPages.value) return
  page.value = p
}

function applyFilter() {
  page.value = 1
}

// Order detail modal functions
function showOrderDetail(order) {
  selectedOrder.value = order
  showOrderDetailModal.value = true
}

function closeOrderDetail() {
  showOrderDetailModal.value = false
  selectedOrder.value = null
}

// Image zoom modal functions
function openImageModal(imageSrc, imageAlt) {
  zoomedImageSrc.value = imageSrc
  zoomedImageAlt.value = imageAlt
  showImageModal.value = true
  document.body.style.overflow = 'hidden'
}

function closeImageModal() {
  showImageModal.value = false
  zoomedImageSrc.value = ''
  zoomedImageAlt.value = ''
  document.body.style.overflow = 'auto'
}

function handleKeydown(event) {
  if (event.key === 'Escape') {
    closeImageModal()
  }
}

// Load orders on component mount
onMounted(() => {
  loadOrders()
  
  // Add keyboard event listener
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  // Remove keyboard event listener
  document.removeEventListener('keydown', handleKeydown)
  
  // Cleanup popup timeout
  if (popupTimeout) {
    clearTimeout(popupTimeout)
    popupTimeout = null
  }
})
</script>

<style scoped>
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

/* Hover effect for product images */
.cursor-pointer:hover {
  transform: scale(1.05);
  transition: transform 0.2s ease-in-out;
}

/* Fix horizontal scrollbar issue on table hover */
.overflow-x-auto {
  overflow-x: auto;
  scrollbar-width: thin;
  scrollbar-color: #d1d5db #f9fafb;
}

.overflow-x-auto::-webkit-scrollbar {
  height: 6px;
}

.overflow-x-auto::-webkit-scrollbar-track {
  background: #f9fafb;
  border-radius: 3px;
}

.overflow-x-auto::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.overflow-x-auto::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}

/* Ensure table doesn't cause horizontal overflow */
table {
  width: 100%;
  table-layout: fixed;
  min-width: 800px; /* Minimum width to prevent cramping */
}

/* Prevent content from causing horizontal scroll */
td, th {
  word-wrap: break-word;
  overflow-wrap: break-word;
  hyphens: auto;
  max-width: 0; /* Force text wrapping */
}

/* Responsive table adjustments */
@media (max-width: 1024px) {
  table {
    min-width: 900px;
  }
  
  .w-64 {
    width: 12rem; /* Reduce address column width on smaller screens */
  }
  
  .w-48 {
    width: 10rem; /* Reduce customer info column width */
  }
}

@media (max-width: 768px) {
  table {
    min-width: 1000px;
  }
  
  .w-64 {
    width: 10rem;
  }
  
  .w-48 {
    width: 8rem;
  }
  
  .w-40 {
    width: 7rem;
  }
  
  .w-32 {
    width: 6rem;
  }
  
  .w-28 {
    width: 5rem;
  }
  
  .w-24 {
    width: 4rem;
  }
}
</style> 