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
          // Đảm bảo quantity luôn là number
          this.items.forEach(item => {
            if (item.quantity) {
              // Nếu quantity là string, cố gắng parse số từ đầu string
              if (typeof item.quantity === 'string') {
                const numMatch = item.quantity.match(/^\d+/)
                item.quantity = numMatch ? parseInt(numMatch[0], 10) : 1
              } else {
                item.quantity = Number(item.quantity) || 1
              }
            } else {
              item.quantity = 1
            }
          })
        } catch {
          this.items = []
        }
      }
    },
    saveToStorage() {
      localStorage.setItem('cartItems', JSON.stringify(this.items))
    },
    addToCart(product, quantity) {
      // Đảm bảo quantity là number
      const qty = Number(quantity) || 1
      const existing = this.items.find(i => i.id === product.id)
      if (existing) {
        // Đảm bảo existing.quantity là number
        const currentQty = Number(existing.quantity) || 1
        existing.quantity = currentQty + qty
      } else {
        // Đảm bảo sản phẩm có ảnh đúng
        const productWithImage = {
          ...product,
          quantity: qty, // Luôn là number
          image: getProductImage(product.id)
        }
        this.items.push(productWithImage)
      }
      this.saveToStorage()
    },
    updateQuantity(id, quantity) {
      const item = this.items.find(i => i.id === id)
      if (item) {
        // Đảm bảo quantity luôn là number
        item.quantity = Number(quantity) || 1
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
        // KHÔNG cập nhật quantity từ product (vì product.quantity là "500g/lon", không phải số lượng)
        // Giữ nguyên quantity hiện tại trong giỏ hàng, chỉ đảm bảo nó là number
        if (item.quantity) {
          if (typeof item.quantity === 'string') {
            const numMatch = item.quantity.match(/^\d+/)
            item.quantity = numMatch ? parseInt(numMatch[0], 10) : 1
          } else {
            item.quantity = Number(item.quantity) || 1
          }
        } else {
          item.quantity = 1
        }
      }
    })
        
        this.saveToStorage()
      } catch (error) {
        console.error('Không thể cập nhật giá sản phẩm:', error)
      }
    }
  }
})