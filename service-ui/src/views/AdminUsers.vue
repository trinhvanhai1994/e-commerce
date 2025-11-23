<template>
  <AdminLayout>
    <div>
      <h2 class="text-xl md:text-2xl font-bold text-green-700 text-center mb-4 md:mb-8">Khách Hàng</h2>

      <!-- Loading State -->
      <div v-if="loading" class="flex justify-center items-center py-8">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-green-600"></div>
        <span class="ml-2 text-gray-600">Đang tải dữ liệu...</span>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
        <div class="flex items-center justify-between">
          <div class="flex items-center">
            <svg
              class="w-5 h-5 text-red-500 mr-2"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              ></path>
            </svg>
            <span class="text-red-700">{{ error }}</span>
          </div>
        </div>
      </div>

      <!-- Users Table -->
      <div v-else class="bg-white rounded-xl shadow p-3 md:p-6 mb-6 overflow-x-auto">
        <div class="mb-4 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
          <div>
            <h3 class="text-lg font-semibold text-gray-800">
              Danh sách khách hàng ({{ users.length }})
            </h3>
            <p class="text-sm text-gray-500 mt-1">
              Dữ liệu được lấy từ thông tin đơn hàng
            </p>
            <p class="text-xs text-blue-600 mt-1">
              💡 Khách hàng được gộp dựa vào số điện thoại
            </p>
          </div>
          <div class="flex gap-2">
            <button
              @click="loadUsers"
              class="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors duration-200 flex items-center"
            >
              <svg
                class="w-4 h-4 mr-2"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                ></path>
              </svg>
              Làm mới
            </button>
          </div>
        </div>

        <!-- Customer Statistics -->
        <div v-if="users.length > 0" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 md:gap-4 mb-4 md:mb-6">
          <div class="bg-blue-50 rounded-lg p-4">
            <div class="flex items-center">
              <div class="p-2 bg-blue-100 rounded-lg">
                <svg
                  class="w-6 h-6 text-blue-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                  ></path>
                </svg>
              </div>
              <div class="ml-3">
                <p class="text-sm font-medium text-blue-600">Tổng khách hàng</p>
                <p class="text-2xl font-bold text-blue-900">{{ users.length }}</p>
              </div>
            </div>
          </div>

          <div class="bg-green-50 rounded-lg p-4">
            <div class="flex items-center">
              <div class="p-2 bg-green-100 rounded-lg">
                <svg
                  class="w-6 h-6 text-green-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
                  ></path>
                </svg>
              </div>
              <div class="ml-3">
                <p class="text-sm font-medium text-green-600">Tổng đơn hàng</p>
                <p class="text-2xl font-bold text-green-900">{{ totalOrders }}</p>
              </div>
            </div>
          </div>

          <div class="bg-purple-50 rounded-lg p-4">
            <div class="flex items-center">
              <div class="p-2 bg-purple-100 rounded-lg">
                <svg
                  class="w-6 h-6 text-purple-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"
                  ></path>
                </svg>
              </div>
              <div class="ml-3">
                <p class="text-sm font-medium text-purple-600">Tổng giá trị</p>
                <p class="text-2xl font-bold text-purple-900">
                  {{ formatCurrency(totalValue) }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <div class="overflow-x-auto">
          <table class="min-w-full text-xs md:text-sm border rounded-xl">
            <thead class="bg-green-50">
              <tr>
                <th class="px-2 md:px-3 py-2 text-left font-bold">Tên khách hàng</th>
                <th class="px-2 md:px-3 py-2 text-left font-bold hidden md:table-cell">
                  Số điện thoại
                  <span
                    class="text-xs text-blue-600 ml-1"
                    title="Khách hàng được gộp dựa vào số điện thoại"
                    >🔗</span
                  >
                </th>
                <th class="px-2 md:px-3 py-2 text-left font-bold hidden md:table-cell">Địa chỉ</th>
                <th class="px-2 md:px-3 py-2 text-left font-bold">
                  Số đơn hàng
                  <span
                    class="text-xs text-green-600 ml-1"
                    title="Tổng số đơn hàng của khách hàng này"
                    >📊</span
                  >
                </th>
                <th class="px-2 md:px-3 py-2 text-left font-bold hidden lg:table-cell">Tổng giá trị</th>
                <th class="px-2 md:px-3 py-2 text-left font-bold hidden lg:table-cell">Đơn hàng gần nhất</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="user in users"
                :key="user.phone"
                class="border-b hover:bg-green-50 transition-colors duration-200"
              >
                <td class="px-2 md:px-3 py-2 font-semibold">
                  <div>{{ user.name }}</div>
                  <div class="text-xs text-gray-500 md:hidden">{{ user.phone }}</div>
                </td>
                <td class="px-2 md:px-3 py-2 hidden md:table-cell">{{ user.phone }}</td>
                <td class="px-2 md:px-3 py-2 max-w-xs truncate hidden md:table-cell" :title="user.address">
                  {{ user.address }}
                </td>
                <td class="px-2 md:px-3 py-2 text-center">
                  <span
                    class="bg-blue-100 text-blue-800 px-2 py-1 rounded-full text-xs font-medium"
                  >
                    {{ user.orders }}
                  </span>
                </td>
                <td class="px-2 md:px-3 py-2 font-semibold text-green-600 hidden lg:table-cell">
                  {{ formatCurrency(user.totalValue) }}
                </td>
                <td class="px-2 md:px-3 py-2 text-gray-600 hidden lg:table-cell">
                  {{ formatDate(user.lastOrderDate) }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Empty State -->
        <div v-if="users.length === 0" class="text-center py-8 text-gray-500">
          <svg
            class="w-12 h-12 mx-auto mb-4 text-gray-300"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
            ></path>
          </svg>
          <p>Chưa có dữ liệu khách hàng</p>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import AdminLayout from "./AdminLayout.vue";
import { orderAPI } from "../utils/api.js";

// Reactive data
const users = ref([]);
const loading = ref(false);
const error = ref("");

// Computed properties for statistics
const totalOrders = computed(() => {
  return users.value.reduce((sum, user) => sum + user.orders, 0);
});

const totalValue = computed(() => {
  return users.value.reduce((sum, user) => sum + user.totalValue, 0);
});

// Load users from orders API
const loadUsers = async () => {
  loading.value = true;
  error.value = "";

  try {
    const response = await orderAPI.getOrders();

    // Handle different response formats
    let orders = [];
    if (response && response.success && response.orders) {
      orders = response.orders;
    } else if (response && Array.isArray(response)) {
      orders = response;
    } else if (response && response.orders) {
      orders = response.orders;
    } else {
      throw new Error("Không tìm thấy dữ liệu đơn hàng trong response");
    }

    if (orders.length === 0) {
      users.value = [];
      return;
    }

    // Process orders to extract unique customers
    const customerMap = new Map();

    orders.forEach((order) => {
      try {
        // Ưu tiên customerInfo, fallback về các field khác
        let phone =
          order.customerInfo?.phone || order.customerPhone || order.phone || "N/A";
        const name =
          order.customerInfo?.name || order.customerName || order.name || "N/A";
        const address = order.customerInfo?.address || order.address || "N/A";

        // Normalize phone number (remove spaces, dashes, etc.)
        if (phone !== "N/A") {
          phone = phone
            .toString()
            .replace(/[\s\-\(\)]/g, "")
            .trim();
        }

        // Skip orders without valid phone number
        if (phone === "N/A" || !phone || phone.trim() === "" || phone.length < 3) {
          return;
        }

        if (customerMap.has(phone)) {
          // Update existing customer - GỘP DỮ LIỆU DỰA VÀO SỐ ĐIỆN THOẠI
          const existingCustomer = customerMap.get(phone);
          existingCustomer.orders += 1;
          existingCustomer.totalValue += order.total || 0;

          // Update customer info from most recent order
          const orderDate = new Date(order.createdAt || order.created_at || Date.now());
          const lastOrderDate = new Date(existingCustomer.lastOrderDate);

          if (orderDate >= lastOrderDate) {
            // This order is more recent or same date, update customer info
            existingCustomer.lastOrderDate = order.createdAt || order.created_at;

            // Update name if new order has better data
            if (
              name !== "N/A" &&
              (existingCustomer.name === "N/A" ||
                existingCustomer.name.length < name.length ||
                orderDate > lastOrderDate)
            ) {
              existingCustomer.name = name;
            }

            // Update address if new order has better data
            if (
              address !== "N/A" &&
              (existingCustomer.address === "N/A" ||
                existingCustomer.address.length < address.length ||
                orderDate > lastOrderDate)
            ) {
              existingCustomer.address = address;
            }
          }
        } else {
          // Create new customer
          customerMap.set(phone, {
            name: name,
            phone: phone,
            address: address,
            orders: 1,
            totalValue: order.total || 0,
            lastOrderDate:
              order.createdAt || order.created_at || new Date().toISOString(),
          });
        }
      } catch (orderError) {
        console.error(`Error processing order ${order.id}:`, orderError);
      }
    });

    // Convert map to array and sort by last order date (newest first)
    const customersArray = Array.from(customerMap.values());

    // Sort with error handling
    try {
      users.value = customersArray.sort((a, b) => {
        try {
          const dateA = new Date(a.lastOrderDate);
          const dateB = new Date(b.lastOrderDate);
          return dateB - dateA;
        } catch (dateError) {
          console.warn("Date sorting error:", dateError);
          return 0;
        }
      });
    } catch (sortError) {
      console.warn("Sorting error, using unsorted data:", sortError);
      users.value = customersArray;
    }
  } catch (err) {
    console.error("Error loading users:", err);
    error.value = err.message || "Có lỗi xảy ra khi tải dữ liệu khách hàng";
  } finally {
    loading.value = false;
  }
};

// Format currency
const formatCurrency = (amount) => {
  if (!amount) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(amount);
};

// Format date
const formatDate = (dateString) => {
  if (!dateString) return "N/A";
  try {
    const date = new Date(dateString);
    return date.toLocaleDateString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch (err) {
    return dateString;
  }
};


// Function để debug tìm kiếm số điện thoại cụ thể
const debugPhoneNumber = async (phoneToFind = "0123") => {
  try {
    const response = await orderAPI.getOrders();

    if (response && (response.success || response.orders)) {
      const orders = response.orders || response;

      let foundOrders = [];
      orders.forEach((order, index) => {
        const phone =
          order.customerInfo?.phone || order.customerPhone || order.phone || "N/A";
        const normalizedPhone =
          phone !== "N/A"
            ? phone
                .toString()
                .replace(/[\s\-\(\)]/g, "")
                .trim()
            : "N/A";

        if (normalizedPhone === phoneToFind || phone === phoneToFind) {
          foundOrders.push({
            orderId: order.id,
            originalPhone: phone,
            normalizedPhone: normalizedPhone,
            customerInfo: order.customerInfo,
            customerName: order.customerName,
            name: order.name,
            address: order.address,
            total: order.total,
            createdAt: order.createdAt,
          });
        }
      });

    }
  } catch (err) {
    console.error("Error debugging phone number:", err);
  }
};

// Load users on component mount
onMounted(() => {
  loadUsers();
});
</script>
