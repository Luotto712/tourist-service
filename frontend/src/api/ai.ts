/** 一轮对话消息（OpenAI 兼容格式） */
export interface ChatMessage {
  role: 'user' | 'assistant' | 'system'
  content: string
}

/** DeepSeek /chat/completions 的响应结构（只写用到的部分） */
interface ChatCompletionResponse {
  choices: {
    message: {
      role: string
      content: string
    }
  }[]
}

/**
 * 发一轮对话，返回 AI 的回复文本
 *
 * ⚠️ 这里【不用】utils/request.ts（那个 axios 实例），三个原因：
 *   1. 它的 baseURL 是 /api，会把请求打到 /api/ai/... 去
 *   2. 它的响应拦截器是为后端的 { code, message, data } 写的，结构对不上
 *   3. ★ 后面要做流式输出，fetch + ReadableStream 才是标准做法，
 *      从一开始就用 fetch，加流式时不用改底层
 */
export async function chat(messages: ChatMessage[]): Promise<string> {
  const res = await fetch('/ai/chat/completions', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      model: 'deepseek-chat',
      messages
    })
  })

  if (!res.ok) {
    // 尽量把服务端返回的错误带出来（比如余额不足、key 失效）
    const detail = await res.text().catch(() => '')
    throw new Error(`请求失败(${res.status})${detail ? ':' + detail : ''}`)
  }

  const data = (await res.json()) as ChatCompletionResponse
  // 用 ?. 兜底：万一 choices 是空数组
  return data.choices[0]?.message?.content ?? ''
}
