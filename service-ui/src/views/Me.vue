<template>
  <div>
    <!-- Hero Section - Outside container -->
    <section class="hero-section relative w-full overflow-hidden">
      <!-- Background Image -->
      <img 
        :src="getImageUrlFromApi('/images/me/banner.jpg')" 
        alt="Thi Yên Banner" 
        class="w-full h-auto object-cover"
        @error="handleBannerError"
      />
      
      <!-- Content Overlay -->
      <div class="absolute inset-0 flex items-center justify-center">
        <div class="text-center" data-aos="fade-up">
            <!-- <div class="logo-container mb-3 md:mb-6 lg:mb-8">
              <div class="circle-logo bg-white rounded-full w-10 h-10 sm:w-12 sm:h-12 md:w-20 md:h-20 lg:w-24 lg:h-24 mx-auto flex items-center justify-center shadow-lg">
                <img :src="getImageUrlFromApi('/images/logo/logo.png')" alt="Logo" class="w-8 h-8 sm:w-10 sm:h-10 md:w-12 md:h-12 lg:w-14 lg:h-14 object-contain transition-transform hover:scale-105" />
            </div>
            </div> -->
          <h3 class="text-lg sm:text-xl md:text-3xl lg:text-4xl font-bold text-white mb-2 md:mb-4 lg:mb-4" style="text-shadow: 0 2px 4px rgba(0,0,0,0.1);">
            CÂU CHUYỆN THƯƠNG HIỆU
          </h3>
          <p class="text-xs sm:text-sm md:text-2xl lg:text-4xl text-teal-500 font-medium tracking-wider leading-relaxed transform hover:scale-105 transition-all duration-300 ease-in-out drop-shadow-sm px-4" style="font-family: 'Great Vibes', cursive;">
            <span class="text-white text-sm sm:text-base md:text-2xl lg:text-4xl">Từ một bữa ăn nhỏ – đến thói quen sống lành mỗi ngày</span>
          </p>
        </div>
      </div>
    </section>

    <div class="veyo-page" style="background-color: #fefce8;">

    <!-- Story Section -->
    <section class="story-section py-8">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto">
          <div class="text-center mb-8" data-aos="fade-up">
            <h2 class="text-3xl md:text-4xl font-bold text-green-600 mb-4">
              CÂU CHUYỆN THI YÊN
            </h2>
            <div class="w-24 h-1 bg-green-500 mx-auto"></div>
          </div>

          <div
            class="mx-auto text-gray-700 leading-relaxed text-base md:text-lg"
            data-aos="fade-up"
            data-aos-delay="200"
          >
            <div class="mb-6">
              Giữa nhịp sống hiện đại, không khó để bắt gặp những bữa sáng qua loa,
              bữa trưa ăn vội, hay những ngày dài khép lại trong cảm giác mệt mỏi
              không tên. Người trẻ cuốn vào công việc, người lớn tuổi thì ngày càng
              cần chọn lọc kỹ hơn, vì cơ thể dần trở nên nhạy cảm với những thực
              phẩm kém lành. Nhưng ở mọi độ tuổi, cơ thể đều cần được nuôi dưỡng một
              cách nhẹ nhàng, đều đặn và tự nhiên.
            </div>

            <div class="mb-6">
              Thi Yên được tạo nên từ chính những điều giản dị ấy, như một nhịp thở
              chậm giữa guồng quay vội, một cách dịu dàng để cơ thể được yêu thương
              mỗi ngày.
            </div>

            <div class="text-gray-700 mb-4">
              Lấy cảm hứng từ triết lý dưỡng sinh thuần thực vật kết hợp cùng dinh
              dưỡng hiện đại, Thi Yên mang đến những bữa ăn nhỏ, đủ chất, tiện lợi,
              giúp bạn bắt đầu hành trình sống lành từ những điều đơn giản nhất.
            </div>

            <!-- Block với ảnh bên phải -->
            <div class="flex flex-col md:flex-row items-start gap-6">
              <!-- Nội dung bên trái -->
              <div class="flex-1">
                <h4 class="text-xl md:text-2xl font-bold text-gray-800 mt-4 mb-4">
                  Trọn vị tự nhiên - Cân bằng dưỡng chất
                </h4>
                <div class="text-gray-700 mb-4 text-base md:text-lg">
                  Với Thi Yên, giá trị của một sản phẩm tốt không chỉ nằm ở công
                  thức, mà bắt đầu từ sự chắt lọc cẩn thận từng nguyên liệu, giữ lại
                  những gì nguyên bản, trọn vị và thật sự nuôi dưỡng cơ thể.
                </div>
                <div class="mb-6 text-base md:text-lg">
                  Mỗi loại hạt được chọn vì giá trị tinh túy vốn có, chế biến nhẹ
                  nhàng để giữ nguyên dưỡng chất và hương vị tự nhiên, đặc biệt khi
                  kết hợp cùng nhau, có thể tạo nên những bữa ăn bổ dưỡng, thơm ngon
                  và tiện lợi.
                </div>
              </div>

              <!-- Ảnh bên phải -->
              <div class="w-full md:w-64 shrink-0">
                <img
                  :src="getImageUrlFromApi('/images/me/COMBO-MIX.png')"
                  alt="Hình minh họa"
                  class="rounded-lg shadow-lg w-full cursor-pointer hover:opacity-90 transition-opacity duration-300"
                  @click="openImageModal(getImageUrlFromApi('/images/me/COMBO-MIX.png'), 'Hình minh họa - Trọn vị tự nhiên')"
                />
              </div>
            </div>

            <!-- Block với ảnh bên trái desktop, dưới mobile -->
            <div class="flex flex-col md:flex-row items-start gap-6">
              <!-- Nội dung bên phải (desktop) / trên (mobile) -->
              <div class="flex-1 order-1 md:order-2">
                <h4 class="text-xl md:text-2xl font-bold text-gray-800 mt-4 mb-4">
              Khác biệt trong cách làm
                </h4>
            <div class="mb-6 text-base md:text-lg">
              Giữa thị trường ngày càng bão hòa bởi thực phẩm siêu chế biến, Thi Yên
              chọn một hướng đi chậm hơn: không ngũ cốc công nghiệp, không thành
              phần pha sẵn, không phụ gia đánh lừa vị giác.
            </div>
            <div class="text-base md:text-lg">
              Chúng tôi tin vào hương vị thực sự đến từ tự nhiên và sự kết hợp tối
              giản mà vẫn đủ đầy cho nhu cầu cơ thể, bởi lẽ "Chăm sóc bản thân không
              cần phức tạp, chỉ cần đều đặn và đủ lành."
            </div>
              </div>

              <!-- Ảnh bên trái (desktop) / dưới (mobile) -->
              <div class="w-full md:w-64 shrink-0 order-2 md:order-1">
                <img
                  :src="getImageUrlFromApi('/images/me/item4.png')"
                  alt="Sản phẩm Thi Yên"
                  class="rounded-lg shadow-lg w-full cursor-pointer hover:opacity-90 transition-opacity duration-300"
                  @click="openImageModal(getImageUrlFromApi('/images/me/item4.png'), 'Sản phẩm Thi Yên')"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Vision Section -->
    <section class="vision-section py-4">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto">
          <div class="text-center mb-8" data-aos="fade-up">
            <h2 class="text-3xl md:text-4xl font-bold text-green-600 mb-2">TẦM NHÌN</h2>
            <div class="w-24 h-1 bg-green-500 mx-auto"></div>
          </div>
          
          <div class="bg-gray-50 rounded-lg shadow-lg p-8" data-aos="fade-up" data-aos-delay="200">
            <div class="mx-auto text-gray-700 leading-relaxed text-base md:text-lg">
              <div class="mb-6">
                Thi Yên hướng đến trở thành thương hiệu thực phẩm dưỡng sinh đồng hành cùng người Việt trong hành trình sống khỏe, từ người trẻ bận rộn đến người trung niên cần lựa chọn nhẹ nhàng mà bồi bổ.
              </div>
              <div>
                Dù là bữa sáng nhanh, bữa phụ sau giờ làm, hay khoảnh khắc thư giãn mỗi chiều, chúng tôi mong mỗi sản phẩm đều có thể góp phần nuôi dưỡng một lối sống đủ lành, đủ chậm và đủ yêu thương.
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Mission Section -->
    <section class="mission-section py-4">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto">
          <div class="text-center mb-8" data-aos="fade-up">
            <h2 class="text-3xl md:text-4xl font-bold text-green-600 mb-2">SỨ MỆNH</h2>
            <div class="w-24 h-1 bg-green-500 mx-auto"></div>
          </div>
          
          <div class="bg-gray-50 rounded-lg shadow-lg p-8" data-aos="fade-up" data-aos-delay="200">
            <div class="mx-auto text-gray-700 leading-relaxed text-base md:text-lg">
              <div class="mb-6">
                Chúng tôi mong muốn đưa dinh dưỡng thuần khiết trở lại nhịp sống hiện đại bằng những sản phẩm tiện lợi, dễ tiếp cận, nhưng vẫn giữ trọn giá trị tự nhiên.
              </div>
              <h3 class="text-xl md:text-2xl font-bold text-gray-800 mb-2">Thi Yên cam kết:</h3>
              <ul class="space-y-2 text-gray-700 text-base md:text-lg">
                <li>• Thành phần rõ ràng, tối giản, dễ dùng mỗi ngày</li>
                <li>• Không phụ gia, không đường tinh luyện, không thành phần nhân tạo</li>
                <li>• Giữ lại trọn vẹn dưỡng chất và hương vị nguyên bản trong từng bước chế biến</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Core Values Section -->
    <section class="core-values-section py-4">
      <div class="container mx-auto px-4">
        <div class="text-center mb-8" data-aos="fade-up">
          <h2 class="text-3xl md:text-4xl font-bold text-green-600 mb-4">GIÁ TRỊ CỐT LÕI</h2>
          <div class="w-24 h-1 bg-green-500 mx-auto"></div>
        </div>
        
        <div class="grid md:grid-cols-3 gap-8">
          <!-- Value 1 -->
          <div class="value-card bg-white rounded-lg shadow-lg p-8" data-aos="fade-up" data-aos-delay="100">
            <div class="text-center mb-6">
              <div class="relative w-32 h-32 mx-auto mb-4 flex items-center justify-center">
                <div class="w-24 h-24 bg-green-50 rounded-full absolute"></div>
                <img :src="getImageUrlFromApi('/images/icons/tu-nhien.png')" alt="Thuần tự nhiên" class="w-32 h-32 object-contain relative z-10" />
                </div>
              <h3 class="text-xl font-bold text-gray-800 mb-4">Thuần tự nhiên</h3>
              </div>
            <div class="text-gray-700 leading-relaxed text-base md:text-lg">
                  Nguyên liệu được chọn lọc kỹ lưỡng, không phụ gia tổng hợp, không đường tinh luyện, không bổ sung dưỡng chất nhân tạo. Mỗi thành phần đều được lựa chọn với sự tử tế và minh bạch.
            </div>
          </div>

          <!-- Value 2 -->
          <div class="value-card bg-white rounded-lg shadow-lg p-8" data-aos="fade-up" data-aos-delay="200">
            <div class="text-center mb-6">
              <div class="relative w-32 h-32 mx-auto mb-4 flex items-center justify-center">
                <div class="w-24 h-24 bg-green-50 rounded-full absolute"></div>
                <img :src="getImageUrlFromApi('/images/icons/can-bang.png')" alt="Cân bằng & Bền vững" class="w-32 h-32 object-contain relative z-10" />
                </div>
              <h3 class="text-xl font-bold text-gray-800 mb-4">Cân bằng & Bền vững</h3>
              </div>
            <div class="text-gray-700 leading-relaxed text-base md:text-lg">
               Kết hợp giữa dưỡng chất hiện đại và tinh thần dưỡng sinh, hài hòa giữa khoa học và truyền thống để phù hợp với thể trạng Á Đông.
             </div>
          </div>

          <!-- Value 3 -->
          <div class="value-card bg-white rounded-lg shadow-lg p-8" data-aos="fade-up" data-aos-delay="300">
            <div class="text-center mb-6">
              <div class="relative w-32 h-32 mx-auto mb-4 flex items-center justify-center">
                <div class="w-24 h-24 bg-green-50 rounded-full absolute"></div>
                <img :src="getImageUrlFromApi('/images/icons/tien-loi.png')" alt="Tiện lợi & Ngon miệng" class="w-32 h-32 object-contain relative z-10" />
                </div>
              <h3 class="text-xl font-bold text-gray-800 mb-4">Tiện lợi & Ngon miệng</h3>
              </div>
            <div class="text-gray-700 leading-relaxed text-base md:text-lg">
               Pha nhanh trong 1 phút nhưng đủ để no lâu, đủ chất, dễ trở thành một phần trong cuộc sống thường ngày.
             </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Nutritional Values Section -->
    <section class="nutrition-section py-4">
      <div class="container mx-auto px-4">
        <div class="text-center mb-8" data-aos="fade-up">
          <h2 class="text-3xl md:text-4xl font-bold text-green-600 mb-4">Dinh dưỡng vàng từ thực vật</h2>
          <div class="w-24 h-1 bg-green-500 mx-auto"></div>
        </div>
        
        <div class="grid md:grid-cols-5 gap-6">
          <!-- Value 1 -->
          <div class="nutrition-card text-center" data-aos="zoom-in" data-aos-delay="100">
            <div class="nutrition-icon w-20 h-20 bg-green-100 rounded-full mx-auto mb-4 flex items-center justify-center">
              <span class="text-3xl">❤️</span>
            </div>
            <h3 class="text-lg font-semibold text-gray-800 mb-2">Không cholesterol</h3>
              <p class="text-sm text-gray-600">Tốt cho tim mạch</p>
          </div>

          <!-- Value 2 -->
          <div class="nutrition-card text-center" data-aos="zoom-in" data-aos-delay="200">
            <div class="nutrition-icon w-20 h-20 bg-green-100 rounded-full mx-auto mb-4 flex items-center justify-center">
              <span class="text-3xl">🌱</span>
            </div>
            <h3 class="text-lg font-semibold text-gray-800 mb-2">100% đạm từ thực vật</h3>
              <p class="text-sm text-gray-600">Dinh dưỡng thuần chay</p>
          </div>

          <!-- Value 3 -->
          <div class="nutrition-card text-center" data-aos="zoom-in" data-aos-delay="300">
            <div class="nutrition-icon w-20 h-20 bg-green-100 rounded-full mx-auto mb-4 flex items-center justify-center">
              <span class="text-3xl">⚖️</span>
            </div>
            <h3 class="text-lg font-semibold text-gray-800 mb-2">Chất béo bão hòa thấp</h3>
              <p class="text-sm text-gray-600">Tốt cho sức khỏe</p>
          </div>

          <!-- Value 4 -->
          <div class="nutrition-card text-center" data-aos="zoom-in" data-aos-delay="400">
            <div class="nutrition-icon w-20 h-20 bg-green-100 rounded-full mx-auto mb-4 flex items-center justify-center">
              <span class="text-3xl">🧬</span>
            </div>
            <h3 class="text-lg font-semibold text-gray-800 mb-2">Không biến đổi gen</h3>
              <p class="text-sm text-gray-600">An toàn tự nhiên</p>
          </div>

          <!-- Value 5 -->
          <div class="nutrition-card text-center" data-aos="zoom-in" data-aos-delay="500">
            <div class="nutrition-icon w-20 h-20 bg-green-100 rounded-full mx-auto mb-4 flex items-center justify-center">
              <span class="text-3xl">🥛</span>
            </div>
            <h3 class="text-lg font-semibold text-gray-800 mb-2">Không Lactose</h3>
              <p class="text-sm text-gray-600">Phù hợp mọi người</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Ingredients Section -->
    <section class="ingredients-section py-2">
      <div class="container mx-auto px-4">
        <div class="max-w-6xl mx-auto">
          <div class="text-center mb-8" data-aos="fade-up">
            <h2 class="text-4xl md:text-5xl font-bold text-green-600 mb-6 gradient-text-green">Thành phần dinh dưỡng</h2>
            <div class="w-32 h-1 bg-gradient-to-r from-green-400 to-green-600 mx-auto rounded-full"></div>
            <div class="text-lg text-gray-600 mt-4 max-w-2xl mx-auto">
              Khám phá những thành phần tự nhiên và dinh dưỡng trong mỗi sản phẩm Thi Yên
            </div>
          </div>
          
          <div class="bg-gradient-to-br from-white to-green-50 rounded-3xl shadow-2xl p-8 md:p-12 border border-green-100" data-aos="fade-up" data-aos-delay="200">
            <div class="grid lg:grid-cols-4 gap-8">
              <!-- Tab Navigation -->
              <div class="lg:col-span-1">
                <div class="space-y-3">
                  <button 
                    v-for="(tab, index) in tabs" 
                    :key="index"
                    @click="activeTab = index"
                    :class="[
                      'w-full text-left px-6 py-4 rounded-2xl transition-all duration-300 font-semibold text-sm md:text-base',
                      activeTab === index 
                        ? 'bg-gradient-to-r from-green-500 to-green-600 text-white shadow-lg transform scale-105' 
                        : 'bg-white text-gray-700 hover:bg-green-50 hover:text-green-600 border border-gray-200 hover:border-green-200'
                    ]"
                  >
                    <div class="flex items-center space-x-3">
                      <div :class="[
                        'w-2 h-2 rounded-full transition-all duration-300',
                        activeTab === index ? 'bg-white' : 'bg-green-500'
                      ]"></div>
                      <span>{{ tab.title }}</span>
                    </div>
                  </button>
                </div>
              </div>
              
              <!-- Tab Content -->
              <div class="lg:col-span-3">
                <div class="tab-content bg-white rounded-2xl p-8 shadow-lg">
                  <div v-if="activeTab === 0" class="space-y-6">
                    <div class="flex items-center space-x-3 mb-6">
                      <div class="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center">
                        <span class="text-green-600 text-xl">🌿</span>
                      </div>
                      <h3 class="text-2xl font-bold text-gray-800">Thành phần tự nhiên</h3>
                    </div>
                    <div class="bg-gradient-to-r from-green-50 to-blue-50 p-6 rounded-xl border-l-4 border-green-500">
                      <div class="space-y-4">
                        <div class="bg-white p-4 rounded-lg border border-green-200">
                          <h4 class="font-bold text-green-800 mb-2">BỘT NGŨ HẮC MÈ ĐEN</h4>
                          <p class="text-gray-700 leading-relaxed text-sm">
                            Hỗn hợp bột hạt (Yến mạch, Đậu đen, Hạt vừng, Gạo lứt đen, Diêm mạch đen, Óc chó), Erythritol, Prebiotic (Fructo Oligosaccharides), Bột khoai mỡ, Dâu tằm đen, Muối NaCl.
                          </p>
                        </div>
                        <div class="bg-white p-4 rounded-lg border border-pink-200">
                          <h4 class="font-bold text-pink-800 mb-2">BỘT NGŨ SẮC HỒNG ĐẬU</h4>
                          <p class="text-gray-700 leading-relaxed text-sm">
                            Hỗn hợp bột hạt (Yến mạch, Đậu đỏ, Diêm mạch đỏ, Hạt sen), Táo đỏ, Isomalto oligosaccharide, Fructose, Bột sữa dừa, Chiết xuất củ dền, Konjac gum (INS 425), Muối NaCl.
                          </p>
                        </div>
                      </div>
                    </div>
                    <div class="bg-amber-50 p-4 rounded-lg border border-amber-200">
                      <p class="text-amber-800 font-medium">
                        <span class="font-bold">⚠️ Lưu ý:</span> Có chứa: gluten, các loại hạt (tree nuts).
                      </p>
                    </div>
                  </div>
                  
                  <div v-if="activeTab === 1" class="space-y-6">
                    <div class="flex items-center space-x-3 mb-6">
                      <div class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                        <span class="text-blue-600 text-xl">📋</span>
                      </div>
                      <h3 class="text-2xl font-bold text-gray-800">Thông tin sản phẩm</h3>
                    </div>
                    <div class="grid md:grid-cols-2 gap-6">
                      <div class="bg-gradient-to-br from-blue-50 to-indigo-50 p-6 rounded-xl">
                        <h4 class="font-bold text-gray-800 mb-3 flex items-center">
                          <span class="w-6 h-6 bg-blue-500 rounded-full flex items-center justify-center text-white text-sm mr-3">1</span>
                          Thông tin cơ bản
                        </h4>
                    <ul class="space-y-2 text-gray-700">
                          <li class="flex items-center"><span class="w-2 h-2 bg-blue-500 rounded-full mr-3"></span>Khối lượng tịnh: 600g</li>
                          <li class="flex items-center"><span class="w-2 h-2 bg-blue-500 rounded-full mr-3"></span>Xuất xứ: Việt Nam</li>
                    </ul>
                      </div>
                      <div class="bg-gradient-to-br from-green-50 to-emerald-50 p-6 rounded-xl">
                        <h4 class="font-bold text-gray-800 mb-3 flex items-center">
                          <span class="w-6 h-6 bg-green-500 rounded-full flex items-center justify-center text-white text-sm mr-3">2</span>
                          Bảo quản & Sử dụng
                        </h4>
                    <ul class="space-y-2 text-gray-700">
                          <li class="flex items-center"><span class="w-2 h-2 bg-green-500 rounded-full mr-3"></span>Bảo quản: Nơi khô ráo, thoáng mát</li>
                          <li class="flex items-center"><span class="w-2 h-2 bg-green-500 rounded-full mr-3"></span>Hạn sử dụng: Xem trên bao bì</li>
                    </ul>
                      </div>
                    </div>
                  </div>
                  
                  <div v-if="activeTab === 2" class="space-y-6">
                    <div class="flex items-center space-x-3 mb-6">
                      <div class="w-10 h-10 bg-purple-100 rounded-full flex items-center justify-center">
                        <span class="text-purple-600 text-xl">📊</span>
                      </div>
                      <h3 class="text-2xl font-bold text-gray-800">Bảng dinh dưỡng</h3>
                    </div>
                    <div class="bg-gradient-to-r from-purple-50 to-pink-50 p-6 rounded-xl border-l-4 border-purple-500">
                      <p class="text-gray-700 leading-relaxed mb-4">
                        <strong>(**)</strong> Hàm lượng các vitamin và khoáng chất không thấp hơn 80% giá trị ghi trên nhãn.
                      </p>
                    </div>
                    <div class="bg-gradient-to-br from-gray-50 to-gray-100 p-6 rounded-xl">
                      <div class="flex items-center space-x-3 mb-4">
                        <span class="text-2xl">📈</span>
                        <h4 class="text-lg font-bold text-gray-800">Giá trị dinh dưỡng</h4>
                      </div>
                      <p class="text-gray-600 font-medium">
                        Giá trị dinh dưỡng trung bình trong 100ml sản phẩm
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Product Showcase Section -->
    <section class="product-showcase py-2">
      <div class="container mx-auto px-4">
        <div class="max-w-7xl mx-auto">
          <div class="text-center mb-8" data-aos="fade-up">
            <h2 class="text-4xl md:text-5xl font-bold text-green-600 mb-6 gradient-text-green">Sản phẩm nổi bật</h2>
            <div class="w-32 h-1 bg-gradient-to-r from-green-400 to-green-600 mx-auto rounded-full"></div>
            <p class="text-lg text-gray-600 mt-4 max-w-2xl mx-auto">
              Khám phá những sản phẩm được yêu thích nhất từ Thi Yên
            </p>
          </div>
          
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
            <!-- Product 1 -->
            <div class="product-card bg-gradient-to-br from-white to-green-50 rounded-3xl p-8 shadow-2xl border border-green-100" data-aos="fade-right">
              <div class="relative overflow-hidden rounded-2xl mb-6">
                <img
                  :src="getImageUrlFromApi('/images/me/item1.png')"
                  alt="Bột Ngũ Hắc Mè Đen"
                  class="w-full h-80 object-contain bg-white rounded-2xl shadow-lg hover:shadow-xl transition-all duration-500 cursor-pointer hover:scale-105"
                  @click="openImageModal('/images/me/item1.png', 'Bột Ngũ Hắc Mè Đen - Sản phẩm dinh dưỡng từ 5 loại hạt đen')"
                />
                <div class="absolute inset-0 bg-gradient-to-t from-black/20 to-transparent opacity-0 hover:opacity-100 transition-opacity duration-300 rounded-2xl flex items-end justify-center pb-4">
                  <button 
                    @click="goToProductDetail(1)"
                    class="bg-white/90 text-green-600 px-4 py-2 rounded-full font-semibold text-sm"
                  >
                    Xem chi tiết
                  </button>
                </div>
              </div>
              <div class="text-center">
                <h3 class="text-2xl font-bold text-gray-800 mb-3">Bột Ngũ Hắc Mè Đen</h3>
                <p class="text-gray-600 leading-relaxed mb-4">
                  Kết hợp 5 loại hạt đen bổ dưỡng: mè đen, gạo lứt đen, đậu đen, diêm mạch đen và dâu tằm đen
                </p>
                <div class="flex justify-center space-x-4">
                  <span class="bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm font-medium">Thuần tự nhiên</span>
                  <span class="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm font-medium">Bổ dưỡng</span>
                </div>
              </div>
            </div>
            
            <!-- Product 2 -->
            <div class="product-card bg-gradient-to-br from-white to-pink-50 rounded-3xl p-8 shadow-2xl border border-pink-100" data-aos="fade-left">
              <div class="relative overflow-hidden rounded-2xl mb-6">
                <img
                  :src="getImageUrlFromApi('/images/me/item2.png')"
                  alt="Bột Ngũ Sắc Hồng Đậu"
                  class="w-full h-80 object-contain bg-white rounded-2xl shadow-lg hover:shadow-xl transition-all duration-500 cursor-pointer hover:scale-105"
                  @click="openImageModal(getImageUrlFromApi('/images/me/item2.png'), 'Bột Ngũ Sắc Hồng Đậu - Sản phẩm dinh dưỡng từ 5 loại hạt đỏ')"
                />
                <div class="absolute inset-0 bg-gradient-to-t from-black/20 to-transparent opacity-0 hover:opacity-100 transition-opacity duration-300 rounded-2xl flex items-end justify-center pb-4">
                  <button 
                    @click="goToProductDetail(3)"
                    class="bg-white/90 text-pink-600 px-4 py-2 rounded-full font-semibold text-sm"
                  >
                    Xem chi tiết
                  </button>
                </div>
              </div>
              <div class="text-center">
                <h3 class="text-2xl font-bold text-gray-800 mb-3">Bột Ngũ Sắc Hồng Đậu</h3>
                <p class="text-gray-600 leading-relaxed mb-4">
                  Kết hợp 5 nguyên liệu đỏ tự nhiên, giàu dưỡng chất: táo đỏ, củ dền, đậu đỏ, diêm mạch đỏ, hạt sen đỏ
                </p>
                <div class="flex justify-center space-x-4">
                  <span class="bg-pink-100 text-pink-800 px-3 py-1 rounded-full text-sm font-medium">Bổ khí huyết</span>
                  <span class="bg-purple-100 text-purple-800 px-3 py-1 rounded-full text-sm font-medium">Dưỡng nhan</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- Call to Action -->
          <div class="text-center mt-8" data-aos="fade-up" data-aos-delay="300">
            <div class="bg-gradient-to-r from-green-500 to-green-600 rounded-2xl p-6 text-white">
              <h3 class="text-2xl md:text-3xl font-bold mb-4">Sẵn sàng bắt đầu hành trình sống lành?</h3>
              <p class="text-lg mb-6 opacity-90">Khám phá thêm nhiều sản phẩm dinh dưỡng khác từ Thi Yên</p>
              <button 
                @click="goToProducts"
                class="bg-white text-green-600 px-8 py-3 rounded-full font-bold text-lg hover:bg-gray-100 transition-colors duration-300 shadow-lg"
              >
                Xem tất cả sản phẩm
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- BỮA ĂN THAY THẾ THƠM NGON, BỔ DƯỠNG -->
    <section class="max-w-8xl mx-auto px-4 py-2" data-aos="fade-up">
      <h2 class="text-2xl md:text-3xl font-bold text-green-600 mb-6 text-center gradient-text-green" data-aos="fade-up">BỮA ĂN THAY THẾ THƠM NGON, BỔ DƯỠNG</h2>
      <!-- Desktop: ảnh vuông to, ẩn trên mobile -->
      <div class="hidden md:grid md:grid-cols-4 md:gap-6">
        <img
          v-for="(img, index) in images"
          :key="'d' + index"
          :src="img.src"
          :alt="img.alt"
          class="w-full aspect-square object-contain bg-gray-50 rounded-xl shadow hover:shadow-lg transition-shadow duration-300 cursor-pointer hover:opacity-90 transition-opacity duration-300"
          @click="openImageModal(img.src, img.alt)"
        />
      </div>

      <!-- Mobile: swiper slider, ẩn trên desktop -->
      <div class="md:hidden">
        <swiper
          :modules="modules"
          :slides-per-view="2.5"
          space-between="12"
          :loop="shouldLoop"
          :autoplay="{
            delay: 3000,
            disableOnInteraction: false,
            pauseOnMouseEnter: true
          }"
          :speed="800"
          :grab-cursor="true"
          :centered-slides="false"
          :breakpoints="{
            320: {
              slidesPerView: 2.2,
              spaceBetween: 8
            },
            480: {
              slidesPerView: 2.5,
              spaceBetween: 12
            }
          }"
        >
          <swiper-slide v-for="(img, index) in loopImages" :key="'m' + index">
            <img
              :src="img.src"
              :alt="img.alt"
              class="w-full h-40 object-contain rounded-xl shadow hover:shadow-lg transition-shadow duration-300 cursor-pointer hover:opacity-90 transition-opacity duration-300"
              @click="openImageModal(img.src, img.alt)"
            />
          </swiper-slide>
        </swiper>
      </div>
    </section>
    </div>

    <!-- Image Zoom Modal -->
    <div
      v-if="showImageModal"
      class="fixed inset-0 z-[100] flex items-center justify-center bg-black bg-opacity-90 backdrop-blur-sm"
      @click="closeImageModal"
    >
      <div class="relative w-full h-full flex items-center justify-center p-4">
        <!-- Close Button -->
        <button
          @click="closeImageModal"
          class="absolute top-4 right-4 z-10 bg-white rounded-full p-3 shadow-lg hover:bg-gray-100 transition-colors duration-200"
        >
          <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
        
        <!-- Zoomed Image Container -->
        <div class="relative max-w-[90vw] max-h-[90vh] flex items-center justify-center">
          <img
            :src="zoomedImageSrc"
            :alt="zoomedImageAlt"
            class="max-w-full max-h-full object-contain rounded-lg shadow-2xl zoom-image"
            @click.stop
          />
        </div>
        
        <!-- Image Info -->
        <div v-if="zoomedImageAlt" class="absolute bottom-4 left-4 right-4 bg-black bg-opacity-70 text-white p-3 rounded-lg">
          <p class="text-sm md:text-base text-center">{{ zoomedImageAlt }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Swiper, SwiperSlide } from 'swiper/vue';
import { Autoplay } from 'swiper/modules';
import 'swiper/css';
import { useRouter } from 'vue-router';
import { navigateToMainDomain, navigateToSubdomain } from '../utils/domainUtils';
import { getImageUrlFromApi } from '../utils/imageUtils.js';

export default {
  name: 'Me',
  components: {
    Swiper,
    SwiperSlide
  },
  setup() {
    const router = useRouter();
    
    const goToProducts = () => {
      const domainChanged = navigateToMainDomain('/products');
      if (!domainChanged) {
        router.push('/products');
      }
    };
    
    const goToProductDetail = (productId) => {
      const path = `/products/${productId}`;
      
      // Check if this is a special product (ID 1 or 2)
      if (productId === 1 || productId === 2) {
        const domainChanged = navigateToSubdomain(path);
        if (!domainChanged) {
          router.push(path);
        }
      } else {
        // Regular product, use main domain
        const domainChanged = navigateToMainDomain(path);
        if (!domainChanged) {
          router.push(path);
        }
      }
    };
    
    // Swiper modules
    const modules = [Autoplay];
    
    return {
      goToProducts,
      goToProductDetail,
      modules,
      getImageUrlFromApi
    };
  },
  data() {
    return {
      activeTab: 0,
      tabs: [
        { title: 'THÀNH PHẦN' },
        { title: 'THÔNG TIN CHUNG' },
        { title: 'BẢNG CÔNG BỐ' }
      ],
      showImageModal: false,
      zoomedImageSrc: '',
      zoomedImageAlt: '',
      imagePaths: [
        { src: '/images/struct/1.png', alt: 'Thêm mô tả 1' },
        { src: '/images/struct/2.png', alt: 'Thêm mô tả 2' },
        { src: '/images/struct/3.png', alt: 'Thêm mô tả 3' },
        { src: '/images/struct/4.png', alt: 'Thêm mô tả 4' },
      ]
    }
  },
  methods: {
    handleBannerError(event) {
      // Hide the image and show fallback background
      event.target.style.display = 'none'
    }
  },
  computed: {
    // Computed property để map images với getImageUrlFromApi
    images() {
      return this.imagePaths.map(img => ({
        ...img,
        src: this.getImageUrlFromApi(img.src)
      }))
    },
    // Computed property để duplicate images nếu cần cho loop
    // Loop cần ít nhất 2 * slidesPerView slides để hoạt động đúng
    loopImages() {
      const minSlidesForLoop = Math.ceil(2 * 2.5) // 2 * slidesPerView, làm tròn lên
      if (this.images.length >= minSlidesForLoop) {
        return this.images
      }
      // Duplicate images để đủ cho loop
      const duplicated = []
      while (duplicated.length < minSlidesForLoop) {
        duplicated.push(...this.images)
      }
      return duplicated.slice(0, minSlidesForLoop)
    },
    // Luôn bật loop khi đã duplicate đủ slides
    shouldLoop() {
      return true
    }
  },
  methods: {
    handleBannerError(event) {
      // Hide the image and show fallback background
      event.target.style.display = 'none'
    },
    openImageModal(src, alt = '') {
      // Đảm bảo ảnh được load trước khi hiển thị
      const img = new Image()
      img.onload = () => {
        this.zoomedImageSrc = src
        this.zoomedImageAlt = alt
        this.showImageModal = true
        // Prevent body scroll when modal is open
        document.body.style.overflow = 'hidden'
      }
      img.onerror = () => {
        // Fallback nếu ảnh không load được
        this.zoomedImageSrc = src
        this.zoomedImageAlt = alt
        this.showImageModal = true
        document.body.style.overflow = 'hidden'
      }
      img.src = src
    },
    closeImageModal() {
      this.showImageModal = false
      this.zoomedImageSrc = ""
      this.zoomedImageAlt = ""
      // Restore body scroll
      document.body.style.overflow = 'auto'
    }
  },
  mounted() {
    // Initialize AOS if available
    if (typeof AOS !== 'undefined') {
      AOS.refresh()
    }
  }
}
</script>

<style scoped>
.veyo-page {
  overflow-x: hidden;
  position: relative;
}

/* Hero section improvements */
.hero-section {
  position: relative;
  width: 100vw;
  left: 50%;
  right: 50%;
  margin-left: -50vw;
  margin-right: -50vw;
  background-color: #fefce8; /* Fallback background */
}

/* Logo ở góc - ảnh bên trong nhỏ */
.absolute.top-4.left-4 .logo-circle img {
  width: 1.5rem !important;
  height: 1.5rem !important;
}

@media (min-width: 640px) {
  .absolute.top-4.left-4 .logo-circle img {
    width: 1.75rem !important;
    height: 1.75rem !important;
  }
}

/* Ensure text is always readable */
.hero-section h1,
.hero-section p {
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

/* Smooth transitions for responsive elements */
.logo-circle,
.hero-section h1,
.hero-section p {
  transition: all 0.3s ease;
}

.product-card, .value-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.product-card:hover, .value-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.nutrition-card {
  transition: transform 0.3s ease;
}

.nutrition-card:hover {
  transform: scale(1.05);
}

.nutrition-icon {
  transition: all 0.3s ease;
}

.nutrition-card:hover .nutrition-icon {
  background-color: #f97316;
  transform: rotate(5deg);
}

.logo-circle {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

/* Responsive adjustments for hero section */
@media (max-width: 640px) {
  .logo-container {
    margin-bottom: 0.5rem !important;
  }
  
  .logo-circle {
    width: 3rem !important;
    height: 3rem !important;
  }
  
  .logo-circle img {
    width: 1.5rem !important;
    height: 1.5rem !important;
  }
  
  h1 {
    font-size: 1.125rem !important;
    margin-bottom: 0.375rem !important;
    line-height: 1.2 !important;
  }
  
  p {
    font-size: 0.6875rem !important;
    line-height: 1.3 !important;
    padding: 0 0.75rem !important;
  }
}

@media (min-width: 641px) and (max-width: 768px) {
  .logo-container {
    margin-bottom: 0.75rem !important;
  }
  
  .logo-circle {
    width: 3.5rem !important;
    height: 3.5rem !important;
  }
  
  .logo-circle img {
    width: 2rem !important;
    height: 2rem !important;
  }
  
  h1 {
    font-size: 1.375rem !important;
    margin-bottom: 0.5rem !important;
  }
  
  p {
    font-size: 0.8125rem !important;
    line-height: 1.4 !important;
  }
}

@media (min-width: 769px) and (max-width: 1023px) {
  .logo-container {
    margin-bottom: 2rem !important;
  }
  
  .logo-circle {
    width: 8rem !important;
    height: 8rem !important;
  }
  
  .logo-circle img {
    width: 7rem !important;
    height: 7rem !important;
  }
  
  h1 {
    font-size: 3.5rem !important;
    margin-bottom: 1.5rem !important;
  }
  
  p {
    font-size: 1.75rem !important;
    line-height: 1.6 !important;
  }
}

@media (min-width: 1024px) {
  /* Desktop styles if needed */
}

.tab-content {
  min-height: 200px;
}



/* Hide scrollbar */
::-webkit-scrollbar {
  display: none;
}

/* For Firefox */
* {
  scrollbar-width: none;
}

/* For IE and Edge */
* {
  -ms-overflow-style: none;
}

/* Gradient text effect */
.gradient-text-green {
  background: linear-gradient(45deg, #059669, #10b981);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Enhanced Tab Styling */
.tab-content {
  min-height: 300px;
  transition: all 0.3s ease;
}

/* Product Card Enhancements */
.product-card {
  transition: all 0.4s ease;
  position: relative;
  overflow: hidden;
}

.product-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.05) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.product-card:hover::before {
  opacity: 1;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15);
}

/* Tab Button Enhancements */
.tab-button {
  position: relative;
  overflow: hidden;
}

.tab-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.5s ease;
}

.tab-button:hover::before {
  left: 100%;
}

/* Enhanced Image Hover Effects */
.product-card img {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.product-card:hover img {
  transform: scale(1.05);
}

/* Call to Action Button */
.cta-button {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #10b981, #059669);
  transition: all 0.3s ease;
}

.cta-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.6s ease;
}

.cta-button:hover::before {
  left: 100%;
}

.cta-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(16, 185, 129, 0.3);
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .product-card {
    margin-bottom: 1rem;
  }
  
  .tab-content {
    min-height: 250px;
  }
  
  .product-card img {
    height: 60vh;
    max-height: 300px;
  }
  
  /* Reduce spacing on mobile */
  .story-section,
  .vision-section,
  .mission-section,
  .core-values-section,
  .nutrition-section,
  .ingredients-section,
  .product-showcase {
    padding-top: 1rem !important;
    padding-bottom: 1rem !important;
  }
  
  .text-center.mb-8 {
    margin-bottom: 1.5rem !important;
  }
  
  .text-center.mb-16 {
    margin-bottom: 2rem !important;
  }
}

@media (max-width: 640px) {
  .product-card {
    padding: 1.5rem;
  }
  
  .product-card img {
    height: 50vh;
    max-height: 250px;
  }
  
  .tab-content {
    padding: 1.5rem;
  }
  
  /* Further reduce spacing on small mobile */
  .story-section,
  .vision-section,
  .mission-section,
  .core-values-section,
  .nutrition-section,
  .ingredients-section,
  .product-showcase {
    padding-top: 0.5rem !important;
    padding-bottom: 0.5rem !important;
  }
  
  .text-center.mb-8 {
    margin-bottom: 1rem !important;
  }
  
  .text-center.mb-16 {
    margin-bottom: 1.5rem !important;
  }
}

/* Image Zoom Modal Styles */
.zoom-image {
  animation: zoomIn 0.3s ease-out;
  transform-origin: center;
  width: auto;
  height: auto;
  max-width: 90vw;
  max-height: 90vh;
  object-fit: contain;
  object-position: center;
  border-radius: 0.5rem;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

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

/* Modal backdrop animation */
.fixed.inset-0 {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* Close button hover effect */
.absolute.top-4.right-4 button:hover {
  transform: scale(1.1);
  background-color: #f3f4f6;
}

/* Image hover effects for clickable images */
img[class*="cursor-pointer"]:hover {
  transform: scale(1.02);
  filter: brightness(1.05);
}

/* Smooth transitions for all clickable images */
img[class*="cursor-pointer"] {
  transition: all 0.3s ease;
}

/* Responsive adjustments for modal */
@media (max-width: 768px) {
  .zoom-image {
    max-width: 95vw;
    max-height: 85vh;
  }
  
  .absolute.top-4.right-4 {
    top: 1rem;
    right: 1rem;
  }
  
  .absolute.top-4.right-4 button {
    padding: 0.75rem;
  }
  
  .absolute.top-4.right-4 svg {
    width: 1.25rem;
    height: 1.25rem;
  }
  
  .absolute.bottom-4 {
    bottom: 1rem;
    left: 1rem;
    right: 1rem;
  }
}

/* Swiper mobile optimizations */
@media (max-width: 767px) {
  .swiper {
    overflow: visible;
  }
  
  .swiper-slide {
    transition: transform 0.3s ease;
  }
  
  .swiper-slide:hover {
    transform: scale(1.02);
  }
  
  /* Ensure smooth autoplay */
  .swiper-wrapper {
    transition-timing-function: ease-in-out;
  }
}

@media (min-width: 768px) {
  .swiper-container {
    display: none !important;
  }
}

/* Enhanced image styling for better visual appeal */
.image-container {
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.image-container:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

/* Aspect ratio container */
.aspect-4-3 {
  aspect-ratio: 4 / 3;
  position: relative;
  overflow: hidden;
}

/* Image styling */
.image-container img {
  transition: all 0.3s ease;
  object-fit: contain;
  object-position: center;
}

.image-container:hover img {
  transform: scale(1.02);
}

/* Enhanced shadow effects */
.shadow-xl {
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.shadow-2xl {
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

/* Content card enhancements */
.content-card {
  background: linear-gradient(135deg, #f0fdf4 0%, #ffffff 100%);
  border: 1px solid rgba(16, 185, 129, 0.2);
  transition: all 0.3s ease;
}

.content-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  border-color: rgba(16, 185, 129, 0.3);
}

/* Quote styling */
.quote-box {
  position: relative;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.8) 0%, rgba(240, 253, 244, 0.6) 100%);
  border-left: 4px solid #10b981;
  transition: all 0.3s ease;
}

.quote-box::before {
  content: '"';
  position: absolute;
  top: -10px;
  left: 15px;
  font-size: 3rem;
  color: #10b981;
  opacity: 0.3;
  font-family: serif;
}

.quote-box:hover {
  transform: translateX(5px);
  box-shadow: 0 10px 25px rgba(16, 185, 129, 0.1);
}

/* Responsive image adjustments */
@media (max-width: 1024px) {
  .image-gallery {
    margin-bottom: 2rem;
  }
  
  .content-card {
    padding: 1.5rem;
  }
}

/* Single image design */
.image-container {
  position: relative;
}

/* Responsive adjustments for single image layout */
@media (max-width: 768px) {
  .bg-gradient-to-br {
    padding: 1rem !important;
  }
  
  .space-y-6 > * + * {
    margin-top: 1.5rem !important;
  }
  
  .bg-gray-50 {
    padding: 1rem !important;
  }
  
  .grid.grid-cols-1.sm\\:grid-cols-2 {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }
}

@media (max-width: 480px) {
  .bg-gradient-to-br {
    padding: 0.75rem !important;
  }
  
  .space-y-6 > * + * {
    margin-top: 1rem !important;
  }
  
  .bg-gray-50 {
    padding: 0.75rem !important;
  }
  
  .text-2xl {
    font-size: 1.5rem !important;
  }
}

</style>
  
  
  
  
  