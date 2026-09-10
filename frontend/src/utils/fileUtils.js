/**
 * Extract file attachment links from HTML content.
 * Returns array of { fileId, fileName, downloadUrl }
 */
export function extractFileLinks(html) {
  if (!html) return []
  const links = []
  const regex = /<a[^>]*href="\/api\/files\/(\d+)\/download"[^>]*>([^<]*)<\/a>/gi
  let match
  while ((match = regex.exec(html)) !== null) {
    let name = match[2]
    // Clean HTML entities
    name = name.replace(/&#\d+;/g, '').replace(/&[a-z]+;/g, '')
    links.push({
      fileId: parseInt(match[1]),
      fileName: name.trim(),
      downloadUrl: `/api/files/${match[1]}/download`
    })
  }
  return links
}
