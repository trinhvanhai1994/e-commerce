import { httpClient } from './http/client.js'

function unwrap(data) {
  if (data && typeof data === 'object' && 'success' in data) {
    if (!data.success) {
      throw new Error(data.message || 'Request failed')
    }
    return data.data
  }
  return data
}

export const contentService = {
  async getHomepage() {
    return unwrap(await httpClient.get('/api/dragun/content/homepage'))
  },

  async getPage(pageKey) {
    return unwrap(await httpClient.get(`/api/dragun/content/pages/${pageKey}`))
  },

  async getAdminPageSections(pageKey) {
    return unwrap(await httpClient.get(`/api/dragun/admin/pages/${pageKey}/sections`))
  },

  async updatePageSection(pageKey, sectionKey, payload) {
    return unwrap(await httpClient.put(`/api/dragun/admin/pages/${pageKey}/sections/${sectionKey}`, payload))
  },

  async getPosts() {
    return unwrap(await httpClient.get('/api/dragun/content/posts'))
  },

  async getPostBySlug(slug) {
    return unwrap(await httpClient.get(`/api/dragun/content/posts/${slug}`))
  },

  async getAdminSections() {
    return unwrap(await httpClient.get('/api/dragun/admin/homepage/sections'))
  },

  async updateSection(sectionKey, payload) {
    return unwrap(await httpClient.put(`/api/dragun/admin/homepage/sections/${sectionKey}`, payload))
  },

  async getAdminPosts(includeDeleted = false) {
    const q = includeDeleted ? '?includeDeleted=true' : ''
    return unwrap(await httpClient.get(`/api/dragun/admin/posts${q}`))
  },

  async getAdminPost(id) {
    return unwrap(await httpClient.get(`/api/dragun/admin/posts/${id}`))
  },

  async createPost(payload) {
    return unwrap(await httpClient.post('/api/dragun/admin/posts', payload))
  },

  async updatePost(id, payload) {
    return unwrap(await httpClient.put(`/api/dragun/admin/posts/${id}`, payload))
  },

  async updatePostStatus(id, status) {
    return unwrap(await httpClient.patch(`/api/dragun/admin/posts/${id}/status`, { status }))
  },

  async softDeletePost(id) {
    return unwrap(await httpClient.post(`/api/dragun/admin/posts/${id}/soft-delete`, {}))
  },

  async uploadCmsImage(file, slot, entityType = 'cms', entityId = 'common') {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('slot', slot)
    formData.append('entityType', entityType)
    formData.append('entityId', String(entityId))

    const authToken = localStorage.getItem('authToken')
    const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5678'
    const response = await fetch(`${baseURL}/api/dragun/admin/files/upload-cms`, {
      method: 'POST',
      headers: {
        Authorization: authToken ? `Bearer ${authToken}` : '',
      },
      body: formData,
    })

    const data = await response.json().catch(() => ({}))
    if (!response.ok || data.success === false) {
      throw new Error(data.message || `Upload failed: ${response.statusText}`)
    }
    return data.data
  },
}

export default contentService
