<template>
  <div class="max-w-7xl mx-auto px-4 py-12">
    <div class="grid grid-cols-1 md:grid-cols-2 gap-12 mb-16">
      <!-- Thông tin liên hệ -->
      <div>
        <h2 class="text-2xl font-bold text-green-600 dark:text-green-400 mb-6 transition-colors duration-300">Thông tin liên hệ</h2>
        <div class="text-gray-700 dark:text-gray-300 space-y-2 transition-colors duration-300">
          <div>Sản phẩm được phân phối bởi: <b>CÔNG TY CỔ PHẦN ĐẦU TƯ THƯƠNG MẠI VÀ DỊCH VỤ THÁI LINH</b></div>
          <div>Địa chỉ: Số 69, Ngõ 68 Đường Ngọc Thụy, Phường Ngọc Thụy, Quận Long Biên, Thành phố Hà Nội, Việt Nam</div>
          <div>Hotline: <a href="tel:0339886990" class="text-green-600 dark:text-green-400 font-semibold hover:underline transition-colors duration-300">033 988 6990</a></div>
          <div>Email: <a href="mailto:contact@tinee.vn" class="text-green-600 dark:text-green-400 font-semibold hover:underline transition-colors duration-300">contact@tinee.vn</a></div>
        </div>
      </div>
      <!-- Form đăng ký -->
      <div>
        <h2 class="text-2xl font-bold text-green-600 dark:text-green-400 mb-6 transition-colors duration-300">Đăng ký nhận thông tin khuyến mãi</h2>
        <form class="space-y-4">
          <input type="text" placeholder="Họ tên (*)" class="w-full rounded-full border border-gray-300 dark:border-gray-600 px-5 py-2 bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 placeholder-gray-500 dark:placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-200 dark:focus:ring-green-600 transition-colors duration-300" />
          <input type="text" placeholder="Số điện thoại (*)" class="w-full rounded-full border border-gray-300 dark:border-gray-600 px-5 py-2 bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 placeholder-gray-500 dark:placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-200 dark:focus:ring-green-600 transition-colors duration-300" />
          <button type="submit" class="bg-green-500 dark:bg-green-600 text-white font-bold rounded-full px-8 py-2 mt-2 hover:bg-green-600 dark:hover:bg-green-700 transition-colors duration-300">GỬI</button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
// Không cần logic JS cho form demo này
</script> 