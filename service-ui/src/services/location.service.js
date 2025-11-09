// Location Service - Handles location-related API calls (provinces, districts, wards)

import { httpClient } from './http/client.js'

/**
 * Location Service
 */
export const locationService = {
  /**
   * Get all provinces
   * @returns {Promise<Array>} Provinces list
   */
  async getProvinces() {
    return await httpClient.get('/provinces')
  },

  /**
   * Get districts by province code
   * @param {string} provinceCode - Province code
   * @returns {Promise<Array>} Districts list
   */
  async getDistricts(provinceCode) {
    if (!provinceCode) {
      return []
    }
    
    return await httpClient.get(`/districts/${provinceCode}`)
  },

  /**
   * Get wards by district code
   * @param {string} districtCode - District code
   * @returns {Promise<Array>} Wards list
   */
  async getWards(districtCode) {
    if (!districtCode) {
      return []
    }
    
    return await httpClient.get(`/wards/${districtCode}`)
  },
}

export default locationService

