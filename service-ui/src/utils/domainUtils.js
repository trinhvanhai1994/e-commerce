/**
 * Utility functions for domain switching based on product ID
 */

/**
 * Check if a product ID should trigger domain change
 * @param {number} productId - The product ID to check
 * @returns {boolean} - True if domain should be changed
 */
export function shouldChangeDomain(productId) {
  return productId === 1 || productId === 2 || productId === 3 || productId === 4
}

/**
 * Get the subdomain for specific products
 * @param {number} productId - The product ID (optional, for product-specific subdomain)
 * @returns {string} - The subdomain prefix
 */
export function getSubdomainPrefix(productId = null) {
  // ProductId 1,2 → botnguhacmeden
  if (productId === 1 || productId === 2) {
    return 'botnguhacmeden'
  }
  // ProductId 3,4 → botngusachongdau
  if (productId === 3 || productId === 4) {
    return 'botngusachongdau'
  }
  // Default (for backward compatibility)
  return 'botnguhacmeden'
}

/**
 * Change domain to subdomain for specific products
 * @param {number} productId - The product ID
 * @param {string} path - The path to navigate to
 */
export function navigateWithDomainChange(productId, path) {
  if (shouldChangeDomain(productId)) {
    const currentHost = window.location.hostname
    const subdomainPrefix = getSubdomainPrefix(productId)
    
    // Extract the main domain (remove any existing subdomain)
    const domainParts = currentHost.split('.')
    let mainDomain = currentHost
    
    // If already on a subdomain, get the main domain
    if (domainParts.length > 2) {
      mainDomain = domainParts.slice(-2).join('.')
    }
    
    // Create new subdomain URL
    const newHost = `${subdomainPrefix}.${mainDomain}`
    const newUrl = `${window.location.protocol}//${newHost}${path}`
    
    // Navigate to the new domain
    window.location.href = newUrl
    return true
  }
  
  return false
}

/**
 * Check if current domain is the subdomain for specific products
 * @param {number} productId - The product ID (optional)
 * @returns {boolean} - True if on the correct subdomain
 */
export function isOnProductSubdomain(productId = null) {
  const currentHost = window.location.hostname
  const subdomainPrefix = getSubdomainPrefix(productId)
  
  return currentHost.startsWith(`${subdomainPrefix}.`)
}

/**
 * Get the main domain from current subdomain
 * @returns {string} - The main domain
 */
export function getMainDomain() {
  const currentHost = window.location.hostname
  
  // Check both subdomains
  const subdomainPrefixes = ['botnguhacmeden', 'botngusachongdau']
  for (const prefix of subdomainPrefixes) {
    if (currentHost.startsWith(`${prefix}.`)) {
      return currentHost.substring(prefix.length + 1)
    }
  }
  
  return currentHost
}

/**
 * Navigate to main domain for non-product pages
 * @param {string} path - The path to navigate to
 */
export function navigateToMainDomain(path) {
  const currentHost = window.location.hostname
  
  // Check if currently on any product subdomain
  const subdomainPrefixes = ['botnguhacmeden', 'botngusachongdau']
  for (const prefix of subdomainPrefixes) {
    if (currentHost.startsWith(`${prefix}.`)) {
      const mainDomain = getMainDomain()
      const newUrl = `${window.location.protocol}//${mainDomain}${path}`
      window.location.href = newUrl
      return true
    }
  }
  
  return false
}

/**
 * Navigate to subdomain for special products
 * @param {string} path - The path to navigate to
 * @param {number} productId - The product ID (optional, extracted from path if not provided)
 */
export function navigateToSubdomain(path, productId = null) {
  // Extract productId from path if not provided
  if (!productId) {
    const productMatch = path.match(/^\/products\/(\d+)/)
    if (productMatch) {
      productId = parseInt(productMatch[1], 10)
    }
  }
  
  // Only navigate if productId requires subdomain
  if (!shouldChangeDomain(productId)) {
    return false
  }
  
  const currentHost = window.location.hostname
  const subdomainPrefix = getSubdomainPrefix(productId)
  
  // If not on correct subdomain, redirect to subdomain
  if (!currentHost.startsWith(`${subdomainPrefix}.`)) {
    const mainDomain = getMainDomain()
    const newUrl = `${window.location.protocol}//${subdomainPrefix}.${mainDomain}${path}`
    window.location.href = newUrl
    return true
  }
  
  return false
}

/**
 * Check if current page is a product page with special domain
 * @param {string} path - The current path
 * @returns {boolean} - True if should stay on subdomain
 */
export function shouldStayOnSubdomain(path) {
  // Check if path is a product page with ID 1, 2, 3, or 4
  const productMatch = path.match(/^\/products\/([1234])/)
  return productMatch !== null
}
