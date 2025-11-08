<template>
  <div class="min-h-screen flex flex-col bg-gray-50">
    <AdminHeader />
    <div class="flex flex-1">
      <AdminSidebar />
      <main class="flex-1 p-8 bg-gray-50">
        <slot />
      </main>
    </div>
    <AdminFooter />
  </div>
</template>

<script setup>
import AdminHeader from './AdminHeader.vue'
import AdminSidebar from './AdminSidebar.vue'
import AdminFooter from './AdminFooter.vue'
</script> 