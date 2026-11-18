/**
 * CMS links can be internal paths ("/me") or external targets (https://, tel:, mailto:, "#").
 * router-link only handles internal paths: an external value would be turned into a broken relative route.
 * Usage: <component :is="isInternalLink(to) ? 'router-link' : 'a'" v-bind="linkAttrs(to)">
 */
export const isExternalLink = (to) => typeof to === 'string' && /^https?:\/\//i.test(to)

export const isInternalLink = (to) => typeof to === 'string' && to.startsWith('/') && !to.startsWith('//')

export const linkAttrs = (to) =>
  isInternalLink(to)
    ? { to }
    : {
        href: to || '#',
        target: isExternalLink(to) ? '_blank' : null,
        rel: isExternalLink(to) ? 'noopener' : null,
      }
