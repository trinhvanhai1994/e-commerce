// Mock Data Service - Centralized mock data for development/testing

import { apiConfig } from '../http/config.js'
import { getImageUrlWithCacheBusting } from '../../utils/imageUtils.js'
import { ORDER_STATUS } from '../../constants/orderStatus.js'

/**
 * Check if mock data should be used
 * @returns {boolean}
 */
export function shouldUseMock() {
  return apiConfig.enableMock || import.meta.env.MODE === 'development'
}

/**
 * Get mock products list
 * @returns {Array} Mock products
 */
export function getMockProducts() {
  const baseUrl = apiConfig.baseURL
  return [
    {
      id: 1,
      name: 'BỘT NGŨ HẮC MÈ ĐEN',
      price: 299000,
      oldPrice: 390000,
      image: getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/black/1.png`),
      shortDesc: 'Bột Ngũ Hắc Mè Đen là bữa ăn thay thế tiện lợi...',
    },
    {
      id: 2,
      name: 'COMBO 2 LON BỘT NGŨ HẮC MÈ ĐEN',
      price: 499000,
      oldPrice: 780000,
      image: getImageUrlWithCacheBusting(`${baseUrl}/images/products/combo-black.png`),
      shortDesc: 'Combo tiết kiệm cho gia đình...',
    },
    {
      id: 3,
      name: 'BỘT NGŨ SẮC HỒNG ĐẬU',
      price: 299000,
      oldPrice: 390000,
      image: getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/pink/1.png`),
      shortDesc: 'Bột Ngũ Sắc Hồng Đậu là bữa ăn thay thế tiện lợi...',
    },
    {
      id: 4,
      name: 'COMBO 2 LON BỘT NGŨ SẮC HỒNG ĐẬU',
      price: 499000,
      oldPrice: 780000,
      image: getImageUrlWithCacheBusting(`${baseUrl}/images/products/combo-pink.png`),
      shortDesc: 'Combo tiết kiệm cho gia đình...',
    },
    {
      id: 5,
      name: 'COMBO 2 (1 BỘT NGŨ HẮC MÈ ĐEN + 1 BỘT NGŨ SẮC HỒNG ĐẬU)',
      price: 499000,
      oldPrice: 780000,
      image: getImageUrlWithCacheBusting(`${baseUrl}/images/products/Combo-mix.png`),
      shortDesc: 'Combo tiết kiệm cho gia đình...',
    },
  ]
}

/**
 * Get mock product by ID
 * @param {number|string} id - Product ID
 * @returns {Object} Mock product
 */
export function getMockProduct(id) {
  const baseUrl = apiConfig.baseURL
  const productId = parseInt(id)
  
  // Gallery dựa trên loại sản phẩm
  let gallery = []
  if (productId === 1 || productId === 2) {
    // Sản phẩm mè đen
    gallery = [
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/black/1.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/black/2.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/black/3.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/black/4.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/black/5.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/black/6.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/black/7.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/black/8.png`),
    ]
  } else if (productId === 3 || productId === 4) {
    // Sản phẩm hồng đậu
    gallery = [
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/pink/1.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/pink/2.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/pink/3.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/pink/4.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/pink/5.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/pink/6.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/pink/7.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/pink/8.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/pink/9.png`),
    ]
  } else if (productId === 5) {
    gallery = [
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/mix/0.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/mix/1.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/mix/2.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/mix/3.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/mix/4.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/mix/5.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/mix/1.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/mix/6.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/mix/7.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/mix/8.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/mix/9.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/mix/10.png`),
      getImageUrlWithCacheBusting(`${baseUrl}/images/products/details/mix/11.png`),
    ]
  }

  const productNames = {
    1: 'BỘT NGŨ HẮC MÈ ĐEN',
    2: 'COMBO 2 LON BỘT NGŨ HẮC MÈ ĐEN',
    3: 'BỘT NGŨ SẮC HỒNG ĐẬU',
    4: 'COMBO 2 LON BỘT NGŨ SẮC HỒNG ĐẬU',
    5: 'COMBO 2 (1 BỘT NGŨ HẮC MÈ ĐEN + 1 BỘT NGŨ SẮC HỒNG ĐẬU)',
  }

  return {
    id: productId,
    name: productNames[productId] || `Sản phẩm ${productId}`,
    price: productId === 2 || productId === 4 ? 499000 : 299000,
    oldPrice: productId === 2 || productId === 4 ? 780000 : 390000,
    image: getImageUrlWithCacheBusting(
      productId <= 2
        ? `${baseUrl}/images/products/details/black/1.png`
        : `${baseUrl}/images/products/details/pink/1.png`
    ),
    shortDesc: 'Mô tả sản phẩm...',
    gallery: gallery,
    discount: productId === 2 || productId === 4 ? 36 : 23,
    rating: 5,
    reviewCount: 32,
    stock: 100,
    category: productId <= 2 ? 'me-den' : 'hong-dau',
    quantity: '500g/lon',
    benefits: 'Bổ sung dinh dưỡng, tăng cường sức khỏe, hỗ trợ tiêu hóa.',
    ingredients:
      productId <= 2
        ? 'Mè đen, đậu đen, đậu xanh, đậu đỏ, đậu nành, gạo lứt, hạt sen, hạt điều, hạt óc chó, hạt hạnh nhân.'
        : 'Đậu đỏ, đậu xanh, đậu nành, gạo lứt, hạt sen, hạt điều, hạt óc chó, hạt hạnh nhân, hạt macca.',
    specifications:
      'Sản phẩm được sản xuất tại nhà máy đạt tiêu chuẩn ISO 22000:2018 và GMP Codex (TCVN 5603:2023)',
    technology: 'Công nghệ sấy lạnh giữ nguyên dinh dưỡng.',
    storage: 'Bảo quản nơi khô ráo, thoáng mát, tránh ánh nắng trực tiếp.',
  }
}

/**
 * Get mock order creation response
 * @param {Object} orderData - Order data
 * @returns {Object} Mock order response
 */
export function getMockCreateOrderResponse(orderData) {
  return {
    success: true,
    orderId: 'ORD' + Date.now() + Math.random().toString(36).substr(2, 5).toUpperCase(),
    message: 'Đơn hàng đã được tạo thành công',
  }
}

/**
 * Get mock order by ID
 * @param {string} orderId - Order ID
 * @returns {Object} Mock order
 */
export function getMockOrderById(orderId) {
  return {
    success: true,
    order: {
      id: orderId,
      customerInfo: {
        name: 'Nguyễn Văn A',
        phone: '0123456789',
        address: '123 Đường ABC',
        province: '79',
        district: '760',
        ward: '26734',
      },
      items: [
        {
          id: 1,
          name: 'BỘT NGŨ HẮC MÈ ĐEN',
          price: 299000,
          quantity: 2,
        },
      ],
      subTotal: 598000,
      shippingFee: 20000,
      total: 618000,
      status: ORDER_STATUS.ORDER_STATUS_CONFIRMED,
      type: 'THI_YEN',
      createdAt: new Date().toISOString(),
    },
  }
}

/**
 * Get mock orders list
 * @returns {Object} Mock orders response
 */
export function getMockOrders() {
  return {
    success: true,
    orders: [
      {
        id: '2507240002',
        customerName: 'TRAN XUAN NGHIA',
        createdAt: '2025-07-24 10:59:19',
        address:
          'C16 Khu đấu giá tân triều, thanh trì, hà nội Xã Tân Triều, Huyện Thanh Trì, Thành phố Hà Nội',
        status: ORDER_STATUS.ORDER_STATUS_CONFIRMED,
        paymentMethod: 'COD',
        total: 618000,
        type: 'THI_YEN',
      },
      {
        id: '2507240001',
        customerName: 'Phuong Thao Vu',
        createdAt: '2025-07-24 07:19:45',
        address:
          '72, nguyễn trãi, r5 royal city Phường Thượng Đình, Quận Thanh Xuân, Thành phố Hà Nội',
        status: ORDER_STATUS.ORDER_STATUS_CONFIRMED,
        paymentMethod: 'COD',
        total: 598000,
        type: 'THI_YEN',
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
      },
      {
        id: '2507250001',
        customerName: 'Test Customer',
        customerPhone: '0123',
        createdAt: '2025-07-25 10:00:00',
        address: 'Test Address',
        status: ORDER_STATUS.ORDER_STATUS_CONFIRMED,
        paymentMethod: 'COD',
        total: 100000,
        type: 'THI_YEN',
      },
    ],
  }
}

/**
 * Get mock orders by customer phone
 * @param {string} phone - Customer phone
 * @returns {Object} Mock orders response
 */
export function getMockOrdersByCustomer(phone) {
  return {
    success: true,
    orders: [
      {
        id: 'ORD' + Date.now(),
        customerName: 'Nguyễn Văn A',
        phone: phone,
        createdAt: new Date().toISOString(),
        status: ORDER_STATUS.ORDER_STATUS_CONFIRMED,
        total: 618000,
        type: 'THI_YEN',
        items: [
          {
            name: 'BỘT NGŨ HẮC MÈ ĐEN',
            quantity: 2,
          },
        ],
      },
    ],
  }
}

/**
 * Get mock update order status response
 * @param {string} orderId - Order ID
 * @param {string} status - New status
 * @returns {Object} Mock response
 */
export function getMockUpdateOrderStatusResponse(orderId, status) {
  return {
    success: true,
    message: `Cập nhật trạng thái đơn hàng ${orderId} thành ${status} thành công`,
  }
}

