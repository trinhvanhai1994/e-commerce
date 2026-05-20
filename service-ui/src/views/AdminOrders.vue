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
      <div v-else class="bg-white rounded-xl shadow p-3 md:p-6 mb-6">
        <form class="flex flex-wrap gap-3 md:gap-4 items-end justify-center mb-4">
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
                <th class="px-3 py-2 text-left font-bold w-24">Nguồn</th>
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
                <th class="px-3 py-2 text-left font-bold w-40">Hóa Đơn MISA</th>
                <th class="px-3 py-2 text-left font-bold w-28">Option Misa</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(order, index) in pagedOrders" :key="getOrderId(order)" class="border-b hover:bg-green-50 cursor-pointer transition-colors duration-150" @click="showOrderDetail(order)">
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
                <td class="px-3 py-2 align-top">
                  <span class="inline-flex px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-800">
                    {{ formatOrderSource(order) }}
                  </span>
                </td>
                <td class="px-3 py-2 font-semibold align-top whitespace-nowrap">{{ formatPrice(order.total) }}</td>
                <td class="px-3 py-2 align-top" @click.stop>
                  <span
                    v-if="isMeinvoiceDraftDeleted(order)"
                    class="inline-flex items-center px-2 py-1 rounded border text-xs font-semibold bg-amber-50 border-amber-300 text-amber-900"
                    :title="TITLE_DRAFT_DELETED_ON_MISA"
                  >
                    {{ LABEL_DRAFT_DELETED_ON_MISA }}
                  </span>
                  <span
                    v-else-if="isMeinvoiceCreated(order)"
                    class="inline-flex items-center px-2 py-1 rounded border text-xs font-semibold bg-green-50 border-green-300 text-green-800"
                    :title="getMisaInvoiceRef(order) || undefined"
                  >
                    Đã tạo hóa đơn
                  </span>
                  <button
                    v-else
                    type="button"
                    class="px-2 py-1 text-xs font-semibold rounded-lg border border-green-600 text-green-700 bg-white hover:bg-green-50 disabled:opacity-50 disabled:cursor-not-allowed transition whitespace-nowrap"
                    :disabled="draftInvoiceLoadingKey === getDraftInvoiceLoadingKey(order)"
                    @click="handleCreateDraftInvoice(order)"
                  >
                    {{ draftInvoiceLoadingKey === getDraftInvoiceLoadingKey(order) ? 'Đang tạo...' : 'Tạo HĐ nháp' }}
                  </button>
                </td>
                <td class="px-3 py-2 align-top" @click.stop>
                  <div
                    v-if="getMisaInvoiceRef(order)"
                    class="flex items-center justify-center gap-0.5"
                  >
                    <button
                      type="button"
                      class="p-1.5 rounded-lg text-gray-600 hover:text-green-700 hover:bg-green-50 border border-transparent hover:border-green-200 transition disabled:opacity-50"
                      title="Xem PDF hóa đơn nháp"
                      :disabled="invoicePdfLoading && invoicePdfRefLabel === getMisaInvoiceRef(order)"
                      @click="handleViewInvoicePdf(order)"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                      </svg>
                    </button>
                    <button
                      type="button"
                      class="p-1.5 rounded-lg text-blue-600 hover:text-blue-800 hover:bg-blue-50 border border-transparent hover:border-blue-200 transition disabled:opacity-50"
                      title="Tải PDF hóa đơn về máy"
                      :disabled="draftInvoicePdfDownloadRefId === getMisaInvoiceRef(order) || draftInvoiceDeleteRefId === getMisaInvoiceRef(order)"
                      @click="handleDownloadInvoicePdf(order)"
                    >
                      <svg
                        v-if="draftInvoicePdfDownloadRefId !== getMisaInvoiceRef(order)"
                        class="w-5 h-5"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                        aria-hidden="true"
                      >
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                      </svg>
                      <span
                        v-else
                        class="w-5 h-5 block border-2 border-blue-400 border-t-transparent rounded-full animate-spin"
                      />
                    </button>
                    <button
                      v-if="!isMeinvoiceDraftDeleted(order)"
                      type="button"
                      class="p-1.5 rounded-lg text-red-600 bg-red-50 border border-red-200 hover:text-red-800 hover:bg-red-100 hover:border-red-300 transition disabled:opacity-50"
                      title="Xóa hóa đơn nháp trên MeInvoice"
                      :disabled="draftInvoiceDeleteRefId === getMisaInvoiceRef(order)"
                      @click="openDeleteDraftInvoiceConfirm(order)"
                    >
                      <svg
                        v-if="draftInvoiceDeleteRefId !== getMisaInvoiceRef(order)"
                        class="w-5 h-5"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                        aria-hidden="true"
                      >
                        <path
                          stroke-linecap="round"
                          stroke-linejoin="round"
                          stroke-width="2"
                          d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                        />
                      </svg>
                      <span
                        v-else
                        class="w-5 h-5 block border-2 border-red-400 border-t-transparent rounded-full animate-spin"
                      />
                    </button>
                  </div>
                  <span v-else class="text-gray-300 text-xs">—</span>
                </td>
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

      <!-- MeInvoice PDF preview -->
      <div
        v-if="showInvoicePdfModal"
        class="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-[60] p-4"
        @click.self="closeInvoicePdfModal"
      >
        <div class="bg-white rounded-xl shadow-xl w-full max-w-5xl h-[90vh] flex flex-col overflow-hidden">
          <div class="flex items-center justify-between px-4 py-3 border-b bg-green-50">
            <div>
              <h3 class="text-lg font-bold text-gray-900">Xem trước hóa đơn nháp</h3>
              <p v-if="invoicePdfRefLabel" class="text-xs font-mono text-gray-600 mt-0.5">Ref: {{ invoicePdfRefLabel }}</p>
            </div>
            <button type="button" class="text-gray-500 hover:text-gray-800 p-1" @click="closeInvoicePdfModal" aria-label="Đóng">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          <div class="flex-1 min-h-0 relative bg-gray-100">
            <div v-if="invoicePdfLoading" class="absolute inset-0 flex flex-col items-center justify-center text-gray-600">
              <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-green-600 mb-3"></div>
              <p>Đang tạo xem trước PDF (MeInvoice preview)...</p>
            </div>
            <div v-else-if="invoicePdfError" class="absolute inset-0 flex items-center justify-center p-6">
              <p class="text-red-600 text-center">{{ invoicePdfError }}</p>
            </div>
            <MeinvoicePdfViewer
              v-else-if="invoicePdfData"
              :pdf-data="invoicePdfData"
              class="absolute inset-0"
              @error="onInvoicePdfViewerError"
            />
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

      <!-- Delete draft invoice confirmation -->
      <div
        v-if="showDeleteInvoiceModal"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[70]"
        @click.self="cancelDeleteDraftInvoice"
      >
        <div class="bg-white rounded-lg p-6 max-w-md w-full mx-4 shadow-xl" role="dialog" aria-modal="true" aria-labelledby="delete-invoice-modal-title">
          <div class="flex items-center mb-4">
            <div class="flex-shrink-0">
              <svg class="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
              </svg>
            </div>
            <div class="ml-3">
              <h3 id="delete-invoice-modal-title" class="text-lg font-medium text-gray-900">{{ MSG_DELETE_MODAL_TITLE }}</h3>
            </div>
          </div>

          <div class="mb-6 space-y-3">
            <p class="text-sm text-gray-600">
              {{ MSG_DELETE_MODAL_BODY }}
            </p>
            <div class="bg-gray-50 rounded-lg p-3 space-y-2 text-sm">
              <div class="flex justify-between gap-2">
                <span class="text-gray-600 shrink-0">Mã đơn:</span>
                <strong class="text-gray-900 text-right">{{ pendingDeleteInvoice.orderId }}</strong>
              </div>
              <div>
                <span class="text-gray-600">Ref MeInvoice:</span>
                <p class="mt-1 font-mono text-xs text-gray-800 break-all leading-snug">{{ pendingDeleteInvoice.refId }}</p>
              </div>
            </div>
          </div>

          <div class="flex justify-end gap-3">
            <button
              type="button"
              class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 border border-gray-300 rounded-md hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 disabled:opacity-50"
              :disabled="!!draftInvoiceDeleteRefId"
              @click="cancelDeleteDraftInvoice"
            >
              Hủy
            </button>
            <button
              type="button"
              class="px-4 py-2 text-sm font-medium text-white bg-red-600 border border-transparent rounded-md hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 disabled:opacity-50 inline-flex items-center gap-2"
              :disabled="!!draftInvoiceDeleteRefId"
              @click="confirmDeleteDraftInvoice"
            >
              <span
                v-if="draftInvoiceDeleteRefId === pendingDeleteInvoice.refId"
                class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"
              />
              {{ draftInvoiceDeleteRefId === pendingDeleteInvoice.refId ? 'Đang xóa...' : 'Xóa hóa đơn nháp' }}
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
                <div>
                  <label class="text-sm font-medium text-gray-600">Nguồn:</label>
                  <p class="text-gray-900">{{ formatOrderSource(selectedOrder) }}</p>
                </div>
                <div>
                  <label class="text-sm font-medium text-gray-600">Hóa đơn MISA:</label>
                  <div v-if="isMeinvoiceDraftDeleted(selectedOrder)" class="mt-1 space-y-1">
                    <span
                      class="inline-flex items-center px-2 py-1 rounded border text-xs font-semibold bg-amber-50 border-amber-300 text-amber-900"
                    >
                      {{ LABEL_DRAFT_DELETED_ON_MISA }}
                    </span>
                    <p class="text-xs text-amber-800">{{ TITLE_DRAFT_DELETED_ON_MISA }}</p>
                    <p v-if="getMisaInvoiceRef(selectedOrder)" class="text-xs font-mono text-gray-700 break-all">
                      Ref: {{ getMisaInvoiceRef(selectedOrder) }}
                    </p>
                  </div>
                  <div v-else-if="isMeinvoiceCreated(selectedOrder)" class="mt-1 space-y-1">
                      <span
                        class="inline-flex items-center px-2 py-1 rounded border text-xs font-semibold bg-green-50 border-green-300 text-green-800"
                      >
                        Đã tạo hóa đơn
                      </span>
                      <p v-if="getMisaInvoiceRef(selectedOrder)" class="text-xs font-mono text-gray-700 break-all">
                        Ref: {{ getMisaInvoiceRef(selectedOrder) }}
                      </p>
                    </div>
                  <button
                    v-else
                    type="button"
                    class="mt-1 px-4 py-2 text-sm font-semibold rounded-lg border border-green-600 text-green-700 bg-white hover:bg-green-50 disabled:opacity-50"
                    :disabled="draftInvoiceLoadingKey === getDraftInvoiceLoadingKey(selectedOrder)"
                    @click="handleCreateDraftInvoice(selectedOrder)"
                  >
                    {{ draftInvoiceLoadingKey === getDraftInvoiceLoadingKey(selectedOrder) ? 'Đang tạo...' : 'Tạo HĐ nháp MeInvoice' }}
                  </button>
                </div>
                <div v-if="hasMisaInvoiceArchive(selectedOrder)">
                  <label class="text-sm font-medium text-gray-600">Option Misa:</label>
                  <div class="mt-1 flex items-center gap-1">
                    <button
                      type="button"
                      class="p-2 rounded-lg text-gray-600 hover:text-green-700 hover:bg-green-50"
                      title="Xem PDF hóa đơn nháp"
                      @click="handleViewInvoicePdf(selectedOrder)"
                    >
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                      </svg>
                    </button>
                    <button
                      type="button"
                      class="p-2 rounded-lg text-blue-600 hover:text-blue-800 hover:bg-blue-50 transition disabled:opacity-50"
                      title="Tải PDF hóa đơn về máy"
                      :disabled="draftInvoicePdfDownloadRefId === getMisaInvoiceRef(selectedOrder) || draftInvoiceDeleteRefId === getMisaInvoiceRef(selectedOrder)"
                      @click="handleDownloadInvoicePdf(selectedOrder)"
                    >
                      <svg
                        v-if="draftInvoicePdfDownloadRefId !== getMisaInvoiceRef(selectedOrder)"
                        class="w-5 h-5"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                        aria-hidden="true"
                      >
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                      </svg>
                      <span
                        v-else
                        class="w-5 h-5 block border-2 border-blue-400 border-t-transparent rounded-full animate-spin"
                      />
                    </button>
                    <button
                      v-if="!isMeinvoiceDraftDeleted(selectedOrder)"
                      type="button"
                      class="p-2 rounded-lg text-red-600 bg-red-50 border border-red-200 hover:text-red-800 hover:bg-red-100 hover:border-red-300 transition disabled:opacity-50"
                      title="Xóa hóa đơn nháp trên MeInvoice"
                      :disabled="draftInvoiceDeleteRefId === getMisaInvoiceRef(selectedOrder)"
                      @click="openDeleteDraftInvoiceConfirm(selectedOrder)"
                    >
                      <svg
                        v-if="draftInvoiceDeleteRefId !== getMisaInvoiceRef(selectedOrder)"
                        class="w-5 h-5"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                        aria-hidden="true"
                      >
                        <path
                          stroke-linecap="round"
                          stroke-linejoin="round"
                          stroke-width="2"
                          d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                        />
                      </svg>
                      <span v-else class="w-5 h-5 block border-2 border-red-400 border-t-transparent rounded-full animate-spin" />
                    </button>
                  </div>
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
                        @error="handleImageError($event)"
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
  createDraftInvoice,
  deleteDraftInvoice,
  downloadInvoicePdf,
  previewInvoicePdfArrayBuffer,
} from '../services/meinvoice.service.js'
import MeinvoicePdfViewer from '../components/MeinvoicePdfViewer.vue'
import {
  MEINVOICE_LOOKUP_BY_ORDER,
  MEINVOICE_LOOKUP_BY_PANCAKE,
  MSG_DELETE_MODAL_BODY,
  MSG_DELETE_MODAL_TITLE,
  MSG_DELETE_FAILED,
  MSG_DELETE_SUCCESS,
  MSG_DRAFT_SUCCESS,
  MSG_DRAFT_SUCCESS_WITH_REF_FORMAT,
  MSG_ORDER_NO_MISA_REF,
  MSG_PDF_DOWNLOAD_FAILED,
  MSG_PDF_LOAD_FAILED,
  MSG_PDF_VIEWER_FAILED,
  MISA_REF_DISPLAY_HEAD_LENGTH,
  MISA_REF_DISPLAY_MAX_LENGTH,
  MISA_REF_DISPLAY_TAIL_LENGTH,
  ORDER_SOURCE_LABELS,
  ORDER_TYPE_PANCAKE,
  LABEL_DRAFT_DELETED_ON_MISA,
  MSG_DRAFT_ALREADY_DELETED,
  RESPONSE_FIELD_MEINVOICE_DRAFT_DELETED,
  RESPONSE_FIELD_MEINVOICE_INVOICED,
  RESPONSE_FIELD_MEINVOICE_REF_ID,
  TITLE_DRAFT_DELETED_ON_MISA,
  RESPONSE_FIELD_MISA_INVOICE_REF,
  RESPONSE_FIELD_RECORDED_SUCCESS,
  RESPONSE_FIELD_REF_ID,
} from '../constants/meinvoice.constants.js'
import { 
  ORDER_STATUS, 
  getStatusText as getStatusTextUtil, 
  getStatusClass as getStatusClassUtil,
  getStatusSelectClass as getStatusSelectClassUtil 
} from '../constants/orderStatus.js'
import { getProductImage as getProductImageUtil } from '../utils/productImage'
import { getImageUrlFromApi } from '../utils/imageUtils.js'

const PLACEHOLDER_PRODUCT_IMAGE = getImageUrlFromApi('/images/products/Combo-mix.png')

const orders = ref([])
const draftInvoiceLoadingKey = ref(null)
const draftInvoiceDeleteRefId = ref(null)
const draftInvoicePdfDownloadRefId = ref(null)
const showInvoicePdfModal = ref(false)
const invoicePdfLoading = ref(false)
const invoicePdfData = ref(null)
const invoicePdfError = ref('')
const invoicePdfRefLabel = ref('')
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

const showDeleteInvoiceModal = ref(false)
const pendingDeleteInvoice = ref({
  orderId: '',
  refId: '',
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

function formatOrderSource(order) {
  const type = order?.orderType || order?.type || ''
  if (!type) return '—'
  return ORDER_SOURCE_LABELS[type] || type
}

function getMisaInvoiceRef(order) {
  return order?.misaInvoiceRef || order?.meinvoiceRefId || null
}

function isMeinvoiceDraftDeleted(order) {
  return order?.[RESPONSE_FIELD_MEINVOICE_DRAFT_DELETED] === true
}

function isMeinvoiceCreated(order) {
  if (isMeinvoiceDraftDeleted(order)) {
    return false
  }
  return order?.[RESPONSE_FIELD_MEINVOICE_INVOICED] === true
}

function hasMisaInvoiceArchive(order) {
  return !!getMisaInvoiceRef(order)
}

function truncateMisaRef(ref) {
  if (!ref || ref.length <= MISA_REF_DISPLAY_MAX_LENGTH) return ref
  return `${ref.slice(0, MISA_REF_DISPLAY_HEAD_LENGTH)}…${ref.slice(-MISA_REF_DISPLAY_TAIL_LENGTH)}`
}

function getDraftInvoiceBy(order) {
  const type = order?.orderType || order?.type
  if (type === ORDER_TYPE_PANCAKE && order?.pancakeOrderId) {
    return MEINVOICE_LOOKUP_BY_PANCAKE
  }
  return MEINVOICE_LOOKUP_BY_ORDER
}

function getDraftInvoiceOrderKey(order) {
  if (getDraftInvoiceBy(order) === MEINVOICE_LOOKUP_BY_PANCAKE) {
    return order.pancakeOrderId || getOrderId(order)
  }
  return getOrderId(order)
}

function getDraftInvoiceLoadingKey(order) {
  return `${getDraftInvoiceBy(order)}:${getDraftInvoiceOrderKey(order)}`
}

function closeInvoicePdfModal() {
  showInvoicePdfModal.value = false
  invoicePdfLoading.value = false
  invoicePdfData.value = null
  invoicePdfError.value = ''
  invoicePdfRefLabel.value = ''
}

function onInvoicePdfViewerError(message) {
  invoicePdfError.value = message || MSG_PDF_VIEWER_FAILED
  invoicePdfData.value = null
}

function applyMeinvoiceDraftDeletedToOrder(orderId, refId) {
  const idx = orders.value.findIndex((o) => getOrderId(o) === orderId)
  if (idx < 0) return
  const updated = {
    ...orders.value[idx],
    [RESPONSE_FIELD_MEINVOICE_DRAFT_DELETED]: true,
    [RESPONSE_FIELD_MEINVOICE_INVOICED]: false,
    [RESPONSE_FIELD_MISA_INVOICE_REF]: refId,
    [RESPONSE_FIELD_MEINVOICE_REF_ID]: refId,
  }
  orders.value[idx] = updated
  if (selectedOrder.value && getOrderId(selectedOrder.value) === orderId) {
    selectedOrder.value = updated
  }
}

function openDeleteDraftInvoiceConfirm(order) {
  if (isMeinvoiceDraftDeleted(order)) {
    showErrorPopup(MSG_DRAFT_ALREADY_DELETED)
    return
  }
  const refId = getMisaInvoiceRef(order)
  if (!refId) {
    showErrorPopup(MSG_ORDER_NO_MISA_REF)
    return
  }
  pendingDeleteInvoice.value = {
    orderId: getOrderId(order),
    refId,
  }
  showDeleteInvoiceModal.value = true
}

function cancelDeleteDraftInvoice() {
  if (draftInvoiceDeleteRefId.value) {
    return
  }
  showDeleteInvoiceModal.value = false
  pendingDeleteInvoice.value = { orderId: '', refId: '' }
}

async function confirmDeleteDraftInvoice() {
  const { orderId, refId } = pendingDeleteInvoice.value
  if (!refId || !orderId) {
    return
  }
  draftInvoiceDeleteRefId.value = refId
  try {
    await deleteDraftInvoice(refId, orderId)
    applyMeinvoiceDraftDeletedToOrder(orderId, refId)
    if (showInvoicePdfModal.value && invoicePdfRefLabel.value === refId) {
      closeInvoicePdfModal()
    }
    showDeleteInvoiceModal.value = false
    pendingDeleteInvoice.value = { orderId: '', refId: '' }
    showSuccessPopup(MSG_DELETE_SUCCESS)
    await loadOrders()
  } catch (err) {
    const msg =
      (err instanceof Error ? err.message : null) || err?.message || MSG_DELETE_FAILED
    showErrorPopup(msg)
  } finally {
    if (draftInvoiceDeleteRefId.value === refId) {
      draftInvoiceDeleteRefId.value = null
    }
  }
}

async function handleDownloadInvoicePdf(order) {
  const refId = getMisaInvoiceRef(order)
  if (!refId) {
    showErrorPopup(MSG_ORDER_NO_MISA_REF)
    return
  }
  draftInvoicePdfDownloadRefId.value = refId
  try {
    await downloadInvoicePdf(refId)
  } catch (err) {
    const msg =
      (err instanceof Error ? err.message : null) || err?.message || MSG_PDF_DOWNLOAD_FAILED
    showErrorPopup(msg)
  } finally {
    if (draftInvoicePdfDownloadRefId.value === refId) {
      draftInvoicePdfDownloadRefId.value = null
    }
  }
}

async function handleViewInvoicePdf(order) {
  const refId = getMisaInvoiceRef(order)
  if (!refId) {
    showErrorPopup(MSG_ORDER_NO_MISA_REF)
    return
  }
  showInvoicePdfModal.value = true
  invoicePdfLoading.value = true
  invoicePdfData.value = null
  invoicePdfError.value = ''
  invoicePdfRefLabel.value = refId
  try {
    invoicePdfData.value = await previewInvoicePdfArrayBuffer(
      getDraftInvoiceOrderKey(order),
      refId,
      getDraftInvoiceBy(order)
    )
  } catch (err) {
    invoicePdfError.value =
      (err instanceof Error ? err.message : null) || err?.message || MSG_PDF_LOAD_FAILED
  } finally {
    invoicePdfLoading.value = false
  }
}

async function handleCreateDraftInvoice(order) {
  const loadingKey = getDraftInvoiceLoadingKey(order)
  draftInvoiceLoadingKey.value = loadingKey
  try {
    const data = await createDraftInvoice(getDraftInvoiceOrderKey(order), getDraftInvoiceBy(order))
    const recorded = data?.[RESPONSE_FIELD_RECORDED_SUCCESS] === true
    const refId = data?.[RESPONSE_FIELD_MEINVOICE_REF_ID] || data?.[RESPONSE_FIELD_REF_ID]
    const orderId = getOrderId(order)
    const idx = orders.value.findIndex((o) => getOrderId(o) === orderId)
    if (idx >= 0) {
      const updated = {
        ...orders.value[idx],
        [RESPONSE_FIELD_MEINVOICE_INVOICED]: recorded,
        [RESPONSE_FIELD_MEINVOICE_DRAFT_DELETED]: recorded ? false : orders.value[idx][RESPONSE_FIELD_MEINVOICE_DRAFT_DELETED],
        [RESPONSE_FIELD_MISA_INVOICE_REF]: recorded ? refId : orders.value[idx][RESPONSE_FIELD_MISA_INVOICE_REF],
        [RESPONSE_FIELD_MEINVOICE_REF_ID]: recorded ? refId : orders.value[idx][RESPONSE_FIELD_MEINVOICE_REF_ID],
      }
      orders.value[idx] = updated
      if (selectedOrder.value && getOrderId(selectedOrder.value) === orderId) {
        selectedOrder.value = updated
      }
    }
    if (recorded) {
      showSuccessPopup(
        refId ? MSG_DRAFT_SUCCESS_WITH_REF_FORMAT.replace('%s', refId) : MSG_DRAFT_SUCCESS
      )
    } else {
      showErrorPopup(
        data?.message || data?.meinvoiceResponse || 'MeInvoice trả về lỗi khi tạo hóa đơn nháp.'
      )
    }
  } catch (err) {
    const msg =
      (err instanceof Error ? err.message : null) ||
      err?.message ||
      err?.data?.message ||
      'Không thể tạo hóa đơn nháp. Kiểm tra đăng nhập admin và cấu hình MeInvoice.'
    showErrorPopup(msg)
  } finally {
    if (draftInvoiceLoadingKey.value === loadingKey) {
      draftInvoiceLoadingKey.value = null
    }
  }
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
    orders.value = []
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

function getProductImage(item) {
  if (item.mainImage || item.image) {
    return getProductImageUtil(item)
  }
  if (item.imageUrl && item.imageUrl.trim() !== '') {
    return item.imageUrl
  }
  return PLACEHOLDER_PRODUCT_IMAGE
}

function handleImageError(event) {
  if (event.target.src !== PLACEHOLDER_PRODUCT_IMAGE) {
    event.target.src = PLACEHOLDER_PRODUCT_IMAGE
  } else {
    event.target.style.display = 'none'
    if (event.target.nextElementSibling) {
      event.target.nextElementSibling.style.display = 'flex'
    }
  }
}

function getProductName(item) {
  const name = item.name || item.productName || item.title || item.productTitle
  if (name && String(name).trim() !== '') {
    return String(name).trim()
  }
  const productId = item.id || item.productId
  return productId ? `Sản phẩm #${productId}` : 'Sản phẩm'
}

function getProductDescription(item) {
  const desc = item.description || item.desc || item.productDescription
  return desc && String(desc).trim() !== '' ? String(desc).trim() : null
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

  invoicePdfData.value = null
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

/* Hover effect for product images - removed zoom */

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