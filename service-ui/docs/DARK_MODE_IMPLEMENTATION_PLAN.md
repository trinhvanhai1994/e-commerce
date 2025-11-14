# Kế Hoạch Triển Khai Light/Dark Mode

## 📋 Tổng Quan

Tài liệu này mô tả kế hoạch chi tiết để thêm tính năng chuyển đổi Light/Dark Mode cho ứng dụng e-commerce, đảm bảo trải nghiệm người dùng chuyên nghiệp và thân thiện.

## 🎯 Mục Tiêu

1. ✅ Cho phép người dùng chuyển đổi giữa Light Mode và Dark Mode
2. ✅ Lưu trữ preference của người dùng (localStorage)
3. ✅ Tự động phát hiện system preference (optional)
4. ✅ Smooth transition khi chuyển đổi
5. ✅ Áp dụng dark mode cho toàn bộ components và pages
6. ✅ UI toggle button đẹp và dễ sử dụng

## 🛠️ Công Nghệ Sử Dụng

- **VueUse** (`@vueuse/core`): Sử dụng composable `useDark` và `useToggle`
- **Tailwind CSS**: Dark mode classes (`dark:`) đã được tích hợp sẵn
- **Pinia**: Quản lý theme state
- **localStorage**: Lưu trữ user preference

## 📁 Cấu Trúc Files Cần Tạo/Chỉnh Sửa

### Files Mới Cần Tạo:
1. `src/stores/theme.js` - Pinia store quản lý theme
2. `src/components/ThemeToggle.vue` - Component nút chuyển đổi theme
3. `src/composables/useTheme.js` - Composable để sử dụng theme (optional, nếu cần)

### Files Cần Chỉnh Sửa:
1. `tailwind.config.js` - Bật dark mode strategy
2. `src/App.vue` - Thêm ThemeToggle vào header
3. `src/main.js` - Khởi tạo theme store
4. `src/style.css` - Thêm dark mode base styles (nếu cần)
5. Các component/view files - Thêm dark mode classes

## 🔧 Chi Tiết Implementation

### Bước 1: Cấu Hình Tailwind CSS

**File: `tailwind.config.js`**
- Thêm `darkMode: 'class'` vào config
- Đảm bảo dark mode hoạt động với class strategy

### Bước 2: Tạo Theme Store

**File: `src/stores/theme.js`**
- Sử dụng VueUse `useDark` composable
- Lưu preference vào localStorage
- Cung cấp methods để toggle theme
- Hỗ trợ system preference detection

### Bước 3: Tạo Theme Toggle Component

**File: `src/components/ThemeToggle.vue`**
- Button với icon sun/moon
- Smooth animation khi chuyển đổi
- Responsive design
- Accessible (ARIA labels)

### Bước 4: Tích Hợp Vào App

**File: `src/App.vue`**
- Thêm ThemeToggle vào header
- Đảm bảo theme được apply ngay khi app load
- Thêm dark class vào root element

### Bước 5: Cập Nhật Styles

**File: `src/style.css`**
- Thêm dark mode base styles
- Đảm bảo smooth transitions

### Bước 6: Cập Nhật Components

Cập nhật các components chính với dark mode classes:
- Header/Navigation
- Footer
- Product Cards
- Cart Popup
- Forms
- Buttons
- Modals

## 🎨 Design Guidelines

### Light Mode Colors:
- Background: `bg-yellow-50`, `bg-white`
- Text: `text-gray-900`, `text-black`
- Borders: `border-gray-200`, `border-gray-300`
- Cards: `bg-white`

### Dark Mode Colors:
- Background: `dark:bg-gray-900`, `dark:bg-gray-800`
- Text: `dark:text-gray-100`, `dark:text-white`
- Borders: `dark:border-gray-700`, `dark:border-gray-600`
- Cards: `dark:bg-gray-800`

### Transition:
- Sử dụng `transition-colors duration-300` cho smooth transitions

## 📝 Implementation Checklist

### Phase 1: Core Setup
- [ ] Cấu hình Tailwind dark mode
- [ ] Tạo theme store với Pinia
- [ ] Tạo ThemeToggle component
- [ ] Tích hợp vào App.vue
- [ ] Test basic toggle functionality

### Phase 2: UI Components
- [ ] Cập nhật Header với dark mode
- [ ] Cập nhật Footer với dark mode
- [ ] Cập nhật Navigation menu
- [ ] Cập nhật Cart Popup
- [ ] Cập nhật Search overlay

### Phase 3: Pages
- [ ] Home page
- [ ] Products page
- [ ] Product Detail page
- [ ] Blog page
- [ ] Contact page
- [ ] Checkout page
- [ ] Cart page
- [ ] Admin pages (optional)

### Phase 4: Polish
- [ ] Smooth transitions
- [ ] Icon animations
- [ ] Accessibility improvements
- [ ] Mobile responsiveness
- [ ] Testing trên các browsers

## 🚀 Best Practices

1. **Consistency**: Sử dụng cùng một bộ màu dark mode cho toàn bộ app
2. **Performance**: Tránh flash of wrong theme (FOIT) bằng cách load theme ngay từ đầu
3. **Accessibility**: Đảm bảo contrast ratio đạt WCAG standards
4. **User Experience**: 
   - Lưu preference của user
   - Smooth transitions
   - Clear visual feedback
5. **Testing**: Test trên nhiều devices và browsers

## 🔍 Testing Checklist

- [ ] Toggle hoạt động đúng
- [ ] Preference được lưu vào localStorage
- [ ] Theme persist sau khi reload
- [ ] Không có flash khi load page
- [ ] Tất cả components hiển thị đúng trong dark mode
- [ ] Responsive trên mobile
- [ ] Accessibility (keyboard navigation, screen readers)

## 📚 Tài Liệu Tham Khảo

- [Tailwind CSS Dark Mode](https://tailwindcss.com/docs/dark-mode)
- [VueUse useDark](https://vueuse.org/core/useDark/)
- [WCAG Contrast Guidelines](https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum.html)

## 🎯 Timeline Ước Tính

- **Phase 1 (Core Setup)**: 1-2 giờ
- **Phase 2 (UI Components)**: 2-3 giờ
- **Phase 3 (Pages)**: 3-4 giờ
- **Phase 4 (Polish)**: 1-2 giờ

**Tổng cộng**: ~7-11 giờ

## 💡 Notes

- Có thể bắt đầu với Phase 1 để có basic functionality
- Sau đó iterate qua các phases để hoàn thiện
- Nên test thường xuyên trong quá trình development
- Có thể thêm "auto" mode để follow system preference trong tương lai


