import { defineStore } from 'pinia'

export const useProductStore = defineStore('product', {
  state: () => ({
    mainProduct: {
      id: 1,
      name: 'Serum Dưỡng Da Vitamin C',
      description: 'Serum dưỡng da giúp làm sáng và đều màu da, giảm thâm nám với công thức Vitamin C tự nhiên.',
      price: 499000,
      images: ['/images/serum-vitamin-c.jpg'],
      features: [
        'Chống oxy hóa mạnh mẽ',
        'Giảm thâm nám, làm sáng da',
        'Thẩm thấu nhanh, không nhờn rít',
        'Phù hợp mọi loại da'
      ]
    }
  }),
  
  getters: {
    getMainProduct: (state) => state.mainProduct
  }
}) 