import { getImageUrlFromApi } from '../utils/imageUtils.js'

/** Fallback when CMS API fails — mirrors seed / previous hardcode */
export const defaultHomepage = {
  sections: {
    header_logo: {
      sectionKey: 'header_logo',
      title: 'Logo website',
      imageDesktop: null,
      useDefaultDesktop: true,
      useDefaultMobile: true,
      enabled: true,
    },
    hero: {
      sectionKey: 'hero',
      imageDesktop: null,
      imageMobile: null,
      useDefaultDesktop: true,
      useDefaultMobile: true,
      enabled: true,
    },
    story: {
      sectionKey: 'story',
      title: 'GIỮ TRỌN DƯỠNG CHẤT\nVÀ HƯƠNG VỊ NGUYÊN BẢN',
      body: 'Một bữa ăn nhẹ, nhưng đủ đầy.\nMột cách chăm sóc bản thân, nhưng không cầu kỳ.\nMột thói quen nhỏ, nhưng nuôi dưỡng cơ thể mỗi ngày.\n\nVới Thi Yên, giá trị của một sản phẩm tốt không chỉ nằm ở công thức, mà bắt đầu từ sự chắt lọc cẩn thận từng nguyên liệu, giữ lại những gì nguyên bản,  trọn vị và thật sự nuôi dưỡng cơ thể.\n\nMỗi sản phẩm là sự giao thoa giữa công thức dưỡng sinh cổ truyền thuần thực vật và tư duy dinh dưỡng hiện đại tạo nên những bữa ăn tiện lợi, phù hợp với nhịp sống ngày nay.',
      ctaText: 'Khám phá câu chuyện Thi Yên',
      ctaLink: '/me',
      imageDesktop: null,
      useDefaultDesktop: true,
      useDefaultMobile: true,
      enabled: true,
    },
    quality: {
      sectionKey: 'quality',
      title: 'TIÊU CHUẨN CHẤT LƯỢNG',
      enabled: true,
      useDefaultDesktop: true,
      useDefaultMobile: true,
      extraJson: {
        items: [
          {
            icon: null,
            useDefaultIcon: true,
            title: 'TIÊU CHUẨN ISO 22000:2018',
            body: 'Nhà máy sản xuất có hệ thống quản lý tốt an toàn vệ sinh thực phẩm và đảm bảo cung cấp các sản phẩm thực phẩm an toàn, chất lượng cho người tiêu dùng.',
          },
          {
            icon: null,
            useDefaultIcon: true,
            title: 'Chứng nhận GMP Codex (TCVN 5603:2023)',
            body: 'Do TQC cấp theo lĩnh vực thực phẩm.\nNhà máy đảm bảo toàn bộ quy trình sản xuất tuân thủ Thực hành sản xuất tốt (Good Manufacturing Practice - GMP)',
          },
        ],
      },
    },
    meal_gallery: {
      sectionKey: 'meal_gallery',
      enabled: true,
      useDefaultDesktop: true,
      useDefaultMobile: true,
      extraJson: {
        banner: null,
        useDefaultBanner: true,
        title: 'BỮA ĂN THAY THẾ THƠM NGON, BỔ DƯỠNG',
        tiles: [
          { src: null, useDefault: true, alt: 'Thêm mô tả 1' },
          { src: null, useDefault: true, alt: 'Thêm mô tả 2' },
          { src: null, useDefault: true, alt: 'Thêm mô tả 3' },
          { src: null, useDefault: true, alt: 'Thêm mô tả 4' },
        ],
      },
    },
    floating_chat: {
      sectionKey: 'floating_chat',
      enabled: true,
      extraJson: {
        messengerUrl: 'https://m.me/yourpage',
        zaloUrl: 'https://zalo.me/yourzalo',
      },
    },
    footer: {
      sectionKey: 'footer',
      enabled: true,
      extraJson: {
        address: 'KĐT Khai Sơn City,\nPhường Bồ Đề, Thành phố Hà Nội, Việt Nam',
        phone: '0396860584',
        email: 'thiyen.vietnam@gmail.com',
        policyTitle: 'QUY ĐỊNH VÀ CHÍNH SÁCH',
        policyLinks: [
          { label: 'Điều khoản sử dụng', to: '/terms' },
          { label: 'Chính sách bảo mật', to: '/privacy' },
          { label: 'Chính sách đổi trả', to: '/returns' },
          { label: 'Chính sách thanh toán', to: '/payment' },
        ],
        contactTitle: 'THÔNG TIN LIÊN HỆ',
        hotline: '0396860584',
        hotlineHours: '(Thứ 2 - Thứ 7 (8h - 17h))',
        faqLabel: 'Các câu hỏi thường gặp',
        faqLink: '/faq',
        socialTitle: 'Kết nối với Thi Yên tại',
        socials: [
          { type: 'facebook', label: 'FACEBOOK', url: '#' },
          { type: 'tiktok', label: 'TIKTOK', url: '#' },
          { type: 'youtube', label: 'YOUTUBE', url: '#' },
        ],
        copyright: '© 2024 Thi Yên. Tất cả quyền được bảo lưu.',
      },
    },
  },
  featuredPosts: [
    {
      id: 1,
      title: 'Dinh Dưỡng Đen Nguyên Bản Từ 5 Loại Hạt Trong Bột Ngũ Hắc Mè Đen',
      excerpt:
        'Bột ngũ hắc mè đen Thi Yên là bữa ăn thay thế dinh dưỡng từ 5 loại hạt đen nguyên bản, phù hợp với lối sống lành mạnh và hiện đại. Dưỡng sinh dễ dàng cho cơ thể khỏe mạnh.',
      coverImage: '/images/blogs/blog1/avatar1.png',
      slug: 'blog1',
    },
    {
      id: 2,
      title: 'Hiểu Cơ Bản Về Dưỡng Sinh: Cân Bằng Âm Dương & Thuận Tự Nhiên',
      excerpt:
        'Khám phá triết lý dưỡng sinh của Thi Yên, kết hợp tinh hoa dưỡng sinh cổ truyền và dinh dưỡng hiện đại. Mỗi sản phẩm là bữa ăn tiện lợi, cân bằng âm dương, thuận theo ngũ hành, nuôi dưỡng cơ thể, giấc ngủ, dưỡng nhan và hỗ trợ sức khỏe toàn diện',
      coverImage: '/images/blogs/blog2/avatar2.png',
      slug: 'blog2',
    },
    {
      id: 3,
      title: 'Dưỡng Sinh Hiện Đại Là Gì? Vì Sao Người Trẻ Nên Nuôi Cơ Thể Từ Gốc Mỗi Ngày?',
      excerpt:
        'Khám phá dưỡng sinh hiện đại với Thi Yên: Bột ngũ hắc mè đen và bột ngũ sắc hồng đậu giúp nuôi dưỡng cơ thể từ bên trong, cải thiện giấc ngủ, dưỡng nhan, dinh dưỡng cho tóc. Sản phẩm thuần tự nhiên, tiện lợi, phù hợp cho người trẻ và lối sống lành mạnh.',
      coverImage: '/images/blogs/blog3/avatar3.png',
      slug: 'blog3',
    },
  ],
}

export function resolveCmsImage(path) {
  if (!path) return ''
  if (path.startsWith('http') || path.startsWith('data:')) return path
  return getImageUrlFromApi(path)
}

/**
 * Three-state CMS image resolve:
 * - useDefault true → packaged defaultPath
 * - useDefault false + path → override
 * - useDefault false + empty → blank (storefront shows empty)
 */
export function resolveSlotImage({ useDefault, path, defaultPath }) {
  if (useDefault !== false) {
    return resolveCmsImage(defaultPath || '')
  }
  if (!path) return ''
  return resolveCmsImage(path)
}

/** Packaged defaults by section (source of truth outside DB) */
export const DEFAULT_IMAGE_PATHS = {
  header_logo: { imageDesktop: '/images/logo/logo.png' },
  hero: {
    imageDesktop: '/images/banner.png',
    imageMobile: '/images/banner-mobile.png',
  },
  story: { imageDesktop: '/images/baner-second.png' },
  quality: {
    icons: ['/images/iso.png', '/images/gmp.png'],
  },
  meal_gallery: {
    banner: '/images/four-home.png',
    tiles: [
      '/images/struct/1.png',
      '/images/struct/2.png',
      '/images/struct/3.png',
      '/images/struct/4.png',
    ],
  },
}
