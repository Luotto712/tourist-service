import DOMPurify from 'dompurify'

/**
 * Sanitize HTML to prevent XSS attacks.
 * Removes dangerous tags (script, iframe, etc.) and event handlers.
 */
export function sanitizeHtml(html) {
  if (!html) return ''
  return DOMPurify.sanitize(html, {
    ALLOWED_TAGS: [
      'p', 'br', 'b', 'i', 'u', 'em', 'strong', 'a', 'ul', 'ol', 'li',
      'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'blockquote', 'pre', 'code',
      'img', 'span', 'div', 'table', 'thead', 'tbody', 'tr', 'th', 'td',
      'hr', 'sub', 'sup', 'del'
    ],
    ALLOWED_ATTR: ['href', 'src', 'alt', 'title', 'target', 'class', 'style', 'width', 'height'],
    ALLOW_DATA_ATTR: false
  })
}
