// Location Service - Handles location-related API calls (provinces, districts, wards)

import { httpClient } from './http/client.js'
import { shouldUseMock } from './mock/mockData.js'

/**
 * Location Service
 */
export const locationService = {
  /**
   * Get all provinces
   * @returns {Promise<Array>} Provinces list
   */
  async getProvinces() {
    try {
      if (shouldUseMock()) {
        // Return empty array for mock, can be extended with mock data if needed
        return []
      }
      
      return await httpClient.get('/provinces')
    } catch (error) {
      if (shouldUseMock()) {
        return []
      }
      throw error
    }
  },

  /**
   * Get districts by province code
   * @param {string} provinceCode - Province code
   * @returns {Promise<Array>} Districts list
   */
  async getDistricts(provinceCode) {
    try {
      if (!provinceCode) {
        return []
      }
      
      if (shouldUseMock()) {
        return []
      }
      
      return await httpClient.get(`/districts/${provinceCode}`)
    } catch (error) {
      if (shouldUseMock()) {
        return []
      }
      throw error
    }
  },

  /**
   * Get wards by district code
   * @param {string} districtCode - District code
   * @returns {Promise<Array>} Wards list
   */
  async getWards(districtCode) {
    try {
      if (!districtCode) {
        return []
      }
      
      if (shouldUseMock()) {
        return []
      }
      
      return await httpClient.get(`/wards/${districtCode}`)
    } catch (error) {
      if (shouldUseMock()) {
        return []
      }
      throw error
    }
  },
}

export default locationService

