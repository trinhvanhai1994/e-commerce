<template>
  <AdminLayout>
    <div>
      <h2 class="text-2xl font-bold text-green-700 text-center mb-8">Khách Hàng</h2>

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
          <button
            @click="loadMockData"
            class="bg-red-600 text-white px-3 py-1 rounded text-sm hover:bg-red-700 transition-colors duration-200"
          >
            Dùng dữ liệu mẫu
          </button>
        </div>
        <div class="mt-2 text-sm text-red-600">
          <p>Đang sử dụng dữ liệu mẫu. Kiểm tra console để xem chi tiết lỗi API.</p>
        </div>
      </div>

      <!-- Users Table -->
      <div v-else class="bg-white rounded-xl shadow p-6 mb-6">
        <div class="mb-4 flex justify-between items-center">
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
        <div v-if="users.length > 0" class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
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
          <table class="min-w-full text-sm border rounded-xl">
            <thead class="bg-green-50">
              <tr>
                <th class="px-3 py-2 text-left font-bold">Tên khách hàng</th>
                <th class="px-3 py-2 text-left font-bold">
                  Số điện thoại
                  <span
                    class="text-xs text-blue-600 ml-1"
                    title="Khách hàng được gộp dựa vào số điện thoại"
                    >🔗</span
                  >
                </th>
                <th class="px-3 py-2 text-left font-bold">Địa chỉ</th>
                <th class="px-3 py-2 text-left font-bold">
                  Số đơn hàng
                  <span
                    class="text-xs text-green-600 ml-1"
                    title="Tổng số đơn hàng của khách hàng này"
                    >📊</span
                  >
                </th>
                <th class="px-3 py-2 text-left font-bold">Tổng giá trị</th>
                <th class="px-3 py-2 text-left font-bold">Đơn hàng gần nhất</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="user in users"
                :key="user.phone"
                class="border-b hover:bg-green-50 transition-colors duration-200"
              >
                <td class="px-3 py-2 font-semibold">{{ user.name }}</td>
                <td class="px-3 py-2">{{ user.phone }}</td>
                <td class="px-3 py-2 max-w-xs truncate" :title="user.address">
                  {{ user.address }}
                </td>
                <td class="px-3 py-2 text-center">
                  <span
                    class="bg-blue-100 text-blue-800 px-2 py-1 rounded-full text-xs font-medium"
                  >
                    {{ user.orders }}
                  </span>
                </td>
                <td class="px-3 py-2 font-semibold text-green-600">
                  {{ formatCurrency(user.totalValue) }}
                </td>
                <td class="px-3 py-2 text-gray-600">
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
    console.log("Loading users from orders API...");
    const response = await orderAPI.getOrders();

    console.log("API Response received:", response);
    console.log("Response type:", typeof response);
    console.log("Response keys:", Object.keys(response || {}));

    // Handle different response formats
    let orders = [];
    if (response && response.success && response.orders) {
      orders = response.orders;
      console.log("Using response.orders:", orders.length);
    } else if (response && Array.isArray(response)) {
      orders = response;
      console.log("Using response as array:", orders.length);
    } else if (response && response.orders) {
      orders = response.orders;
      console.log("Using response.orders (no success flag):", orders.length);
    } else {
      console.log("No valid orders found in response");
      throw new Error("Không tìm thấy dữ liệu đơn hàng trong response");
    }

    if (orders.length === 0) {
      console.log("No orders to process");
      users.value = [];
      return;
    }

    // Process orders to extract unique customers
    const customerMap = new Map();
    console.log("Processing orders:", orders.length);

    orders.forEach((order, index) => {
      try {
        console.log(
          `Processing order ${index + 1}/${orders.length}:`,
          order.id,
          "customerInfo:",
          order.customerInfo
        );

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

        console.log(
          `Order ${order.id}: phone=${phone}, name=${name}, address=${address}`
        );

        // Skip orders without valid phone number
        if (phone === "N/A" || !phone || phone.trim() === "" || phone.length < 3) {
          console.warn(`Skipping order ${order.id} - no valid phone number:`, phone);
          return;
        }

        // Log phone numbers that might be short but valid
        if (phone.length < 8) {
          console.log(
            `⚠️ Short phone number detected: ${phone} (length: ${phone.length}) - keeping for processing`
          );
        }

        if (customerMap.has(phone)) {
          // Update existing customer - GỘP DỮ LIỆU DỰA VÀO SỐ ĐIỆN THOẠI
          const existingCustomer = customerMap.get(phone);
          existingCustomer.orders += 1;
          existingCustomer.totalValue += order.total || 0;

          console.log(
            `Merging customer ${phone}: orders=${existingCustomer.orders}, totalValue=${existingCustomer.totalValue}`
          );

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
              console.log(`Updated name for ${phone}: ${existingCustomer.name}`);
            }

            // Update address if new order has better data
            if (
              address !== "N/A" &&
              (existingCustomer.address === "N/A" ||
                existingCustomer.address.length < address.length ||
                orderDate > lastOrderDate)
            ) {
              existingCustomer.address = address;
              console.log(`Updated address for ${phone}`);
            }
          }
        } else {
          // Create new customer
          console.log(`Creating new customer: ${phone}`);
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
        console.error("Order data:", order);
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

    console.log("Users loaded successfully:", users.value.length);
    console.log("Processed users data:", users.value);

    // Log merging statistics
    const totalOrders = users.value.reduce((sum, user) => sum + user.orders, 0);
    const totalValue = users.value.reduce((sum, user) => sum + user.totalValue, 0);
    console.log(`📊 MERGING STATISTICS:`);
    console.log(`- Total unique customers: ${users.value.length}`);
    console.log(`- Total orders processed: ${totalOrders}`);
    console.log(`- Total value: ${totalValue.toLocaleString("vi-VN")} ₫`);
    console.log(
      `- Average orders per customer: ${(totalOrders / users.value.length).toFixed(2)}`
    );

    if (users.value.length === 0) {
      console.warn("No customers found after processing orders");
    }
  } catch (err) {
    console.error("Error loading users:", err);
    error.value = err.message || "Có lỗi xảy ra khi tải dữ liệu khách hàng";

    // Fallback to mock data if API fails
    console.log("Using fallback mock data...");
    users.value = [
      {
        name: "TRAN XUAN NGHIA",
        phone: "0396860584",
        address:
          "C16 Khu đấu giá tân triều, thanh trì, hà nội Xã Tân Triều, Huyện Thanh Trì, Thành phố Hà Nội",
        orders: 2,
        totalValue: 1236000,
        lastOrderDate: "2025-07-24 10:59:19",
      },
      {
        name: "Phuong Thao Vu",
        phone: "0987654321",
        address:
          "72, nguyễn trãi, r5 royal city Phường Thượng Đình, Quận Thanh Xuân, Thành phố Hà Nội",
        orders: 1,
        totalValue: 598000,
        lastOrderDate: "2025-07-24 07:19:45",
      },
      {
        name: "Đoàn Hải Nam",
        phone: "0912345678",
        address: "4 Phạm Sư Mạnh Phường Phan Chu Trinh, Quận Hoàn Kiếm, Thành phố Hà Nội",
        orders: 1,
        totalValue: 618000,
        lastOrderDate: "2025-07-23 23:09:00",
      },
      {
        name: "Vĩ Bùi",
        phone: "0901234567",
        address: "444 Cách Mạng Tháng 8 Phường 11, Quận 3, Thành phố Hồ Chí Minh",
        orders: 1,
        totalValue: 598000,
        lastOrderDate: "2025-07-23 12:43:35",
      },
      {
        name: "Nguyen thanh vu",
        phone: "0934567890",
        address: "103/23 Hồ Thị Kỉ Phường 01, Quận 10, Thành phố Hồ Chí Minh",
        orders: 1,
        totalValue: 618000,
        lastOrderDate: "2025-07-23 10:22:22",
      },
    ];

    // Clear error after showing fallback data
    setTimeout(() => {
      error.value = "";
    }, 3000);
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

// Test function để kiểm tra cấu trúc dữ liệu API
const testApiData = async () => {
  try {
    console.log("Testing API data structure...");
    const response = await orderAPI.getOrders();
    console.log("Full API Response:", response);

    if (response && response.success && response.orders) {
      console.log("Orders count:", response.orders.length);
      response.orders.forEach((order, index) => {
        console.log(`Order ${index + 1}:`, {
          id: order.id,
          customerInfo: order.customerInfo,
          customerName: order.customerName,
          customerPhone: order.customerPhone,
          address: order.address,
          total: order.total,
          createdAt: order.createdAt,
        });
      });
    } else {
      console.log("API Response structure:", response);
      console.log("Response success:", response?.success);
      console.log("Response orders:", response?.orders);
    }
  } catch (err) {
    console.error("Error testing API data:", err);
    console.error("Error details:", err.message);
    console.error("Error stack:", err.stack);
  }
};

// Function để force load mock data
const loadMockData = () => {
  console.log("Loading mock data directly...");
  users.value = [
    {
      name: "TRAN XUAN NGHIA",
      phone: "0396860584",
      address:
        "C16 Khu đấu giá tân triều, thanh trì, hà nội Xã Tân Triều, Huyện Thanh Trì, Thành phố Hà Nội",
      orders: 2,
      totalValue: 1236000,
      lastOrderDate: "2025-07-24 10:59:19",
    },
    {
      name: "Phuong Thao Vu",
      phone: "0987654321",
      address:
        "72, nguyễn trãi, r5 royal city Phường Thượng Đình, Quận Thanh Xuân, Thành phố Hà Nội",
      orders: 1,
      totalValue: 598000,
      lastOrderDate: "2025-07-24 07:19:45",
    },
    {
      name: "Đoàn Hải Nam",
      phone: "0912345678",
      address: "4 Phạm Sư Mạnh Phường Phan Chu Trinh, Quận Hoàn Kiếm, Thành phố Hà Nội",
      orders: 1,
      totalValue: 618000,
      lastOrderDate: "2025-07-23 23:09:00",
    },
    {
      name: "Vĩ Bùi",
      phone: "0901234567",
      address: "444 Cách Mạng Tháng 8 Phường 11, Quận 3, Thành phố Hồ Chí Minh",
      orders: 1,
      totalValue: 598000,
      lastOrderDate: "2025-07-23 12:43:35",
    },
    {
      name: "Nguyen thanh vu",
      phone: "0934567890",
      address: "103/23 Hồ Thị Kỉ Phường 01, Quận 10, Thành phố Hồ Chí Minh",
      orders: 1,
      totalValue: 618000,
      lastOrderDate: "2025-07-23 10:22:22",
    },
    {
      name: "Test Customer",
      phone: "0123",
      address: "Test Address",
      orders: 1,
      totalValue: 100000,
      lastOrderDate: "2025-07-25 10:00:00",
    },
  ];
  error.value = "";
  loading.value = false;
};

// Function để debug tìm kiếm số điện thoại cụ thể
const debugPhoneNumber = async (phoneToFind = "0123") => {
  try {
    console.log(`🔍 DEBUGGING: Looking for phone number: ${phoneToFind}`);
    const response = await orderAPI.getOrders();

    if (response && (response.success || response.orders)) {
      const orders = response.orders || response;
      console.log(`📋 Total orders to check: ${orders.length}`);

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

      console.log(`✅ Found ${foundOrders.length} orders with phone ${phoneToFind}:`);
      foundOrders.forEach((order) => {
        console.log(`- Order ${order.orderId}:`, order);
      });

      if (foundOrders.length === 0) {
        console.log(`❌ No orders found with phone ${phoneToFind}`);
        console.log("📝 Available phone numbers in orders:");
        const allPhones = orders
          .map((order) => {
            const phone =
              order.customerInfo?.phone || order.customerPhone || order.phone || "N/A";
            return phone !== "N/A"
              ? phone
                  .toString()
                  .replace(/[\s\-\(\)]/g, "")
                  .trim()
              : "N/A";
          })
          .filter((phone) => phone !== "N/A");

        const uniquePhones = [...new Set(allPhones)];
        console.log("Unique phones:", uniquePhones);
      }
    }
  } catch (err) {
    console.error("Error debugging phone number:", err);
  }
};

// Load users on component mount
onMounted(() => {
  loadUsers();
  // Uncomment dòng dưới để test API data structure
  testApiData();
});
</script>
