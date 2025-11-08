import { defineStore } from 'pinia'
import { productAPI } from '@/utils/api.js'
import { getProductImage } from '@/utils/productImage'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: []
  }),
  actions: {
    loadFromStorage() {
      const saved = localStorage.getItem('cartItems')
      if (saved) {
        try {
          this.items = JSON.parse(saved)
        } catch {
          this.items = []
        }
      }
    },
    saveToStorage() {
      localStorage.setItem('cartItems', JSON.stringify(this.items))
    },
    addToCart(product, quantity) {
      const existing = this.items.find(i => i.id === product.id)
      if (existing) {
        existing.quantity += quantity
      } else {
        // Đảm bảo sản phẩm có ảnh đúng
        const productWithImage = {
          ...product,
          quantity,
          image: getProductImage(product.id)
        }
        this.items.push(productWithImage)
      }
      this.saveToStorage()
    },
    updateQuantity(id, quantity) {
      const item = this.items.find(i => i.id === id)
      if (item) {
        item.quantity = quantity
        this.saveToStorage()
      }
    },
    removeItem(id) {
      this.items = this.items.filter(i => i.id !== id)
      this.saveToStorage()
    },
    clearCart() {
      this.items = []
      this.saveToStorage()
    },
    // Cập nhật giá sản phẩm từ API
    async updateProductPrices() {
      try {
        const data = await productAPI.getProducts()
        const products = Array.isArray(data) ? data : (data.data || [])
        
            // Cập nhật giá cho từng item trong giỏ hàng
    this.items.forEach(item => {
      const updatedProduct = products.find(p => p.id === item.id && !p.deleted)
      if (updatedProduct) {
        item.price = updatedProduct.price
        item.oldPrice = updatedProduct.oldPrice
        item.name = updatedProduct.name
        item.image = getProductImage(item.id) // Sử dụng ảnh có sẵn
        item.shortDesc = updatedProduct.shortDesc
        item.category = updatedProduct.category
        item.quantity = updatedProduct.quantity
      }
    })
        
        this.saveToStorage()
      } catch (error) {
        console.error('Không thể cập nhật giá sản phẩm:', error)
      }
    }
  }
})