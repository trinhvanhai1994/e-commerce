/**
 * Utility functions for domain switching based on product ID
 */

/**
 * Check if a product ID should trigger domain change
 * @param {number} productId - The product ID to check
 * @returns {boolean} - True if domain should be changed
 */
export function shouldChangeDomain(productId) {
  return productId === 1 || productId === 2
}

/**
 * Get the subdomain for specific products
 * @returns {string} - The subdomain prefix
 */
export function getSubdomainPrefix() {
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
    const subdomainPrefix = getSubdomainPrefix()
    
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
 * @returns {boolean} - True if on the correct subdomain
 */
export function isOnProductSubdomain() {
  const currentHost = window.location.hostname
  const subdomainPrefix = getSubdomainPrefix()
  
  return currentHost.startsWith(`${subdomainPrefix}.`)
}

/**
 * Get the main domain from current subdomain
 * @returns {string} - The main domain
 */
export function getMainDomain() {
  const currentHost = window.location.hostname
  const subdomainPrefix = getSubdomainPrefix()
  
  if (currentHost.startsWith(`${subdomainPrefix}.`)) {
    return currentHost.substring(subdomainPrefix.length + 1)
  }
  
  return currentHost
}

/**
 * Navigate to main domain for non-product pages
 * @param {string} path - The path to navigate to
 */
export function navigateToMainDomain(path) {
  const currentHost = window.location.hostname
  const subdomainPrefix = getSubdomainPrefix()
  
  // If currently on subdomain, redirect to main domain
  if (currentHost.startsWith(`${subdomainPrefix}.`)) {
    const mainDomain = getMainDomain()
    const newUrl = `${window.location.protocol}//${mainDomain}${path}`
    window.location.href = newUrl
    return true
  }
  
  return false
}

/**
 * Navigate to subdomain for special products
 * @param {string} path - The path to navigate to
 */
export function navigateToSubdomain(path) {
  const currentHost = window.location.hostname
  const subdomainPrefix = getSubdomainPrefix()
  
  // If not on subdomain, redirect to subdomain
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
  // Check if path is a product page with ID 1 or 2
  const productMatch = path.match(/^\/products\/([12])/)
  return productMatch !== null
}
