<template>
  <el-card class="ai-page">
    <template #header>
      <div class="header-bar">
        <span>🤖 AI 问询助手</span>
        <el-button size="small" :disabled="messages.length === 0" @click="handleClear">
          清空对话
        </el-button>
      </div>
    </template>

    <div ref="listRef" class="msg-list">
      <el-empty
        v-if="messages.length === 0"
        description="问点什么吧，比如「景区开放时间是几点？」"
        :image-size="80"
      />

      <div v-for="(m, i) in messages" :key="i" class="msg" :class="m.role">
        <div class="bubble">{{ m.content }}</div>
      </div>

      <div v-if="loading" class="msg assistant">
        <div class="bubble">思考中…</div>
      </div>
    </div>

    <div class="input-bar">
      <el-input
        v-model="input"
        type="textarea"
        :rows="2"
        resize="none"
        placeholder="输入问题,Enter 发送 / Shift+Enter 换行"
        @keydown.enter.exact.prevent="handleSend"
      />
      <el-button type="primary" :loading="loading" @click="handleSend">发送</el-button>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { chat } from '@/api/ai'
import type { ChatMessage } from '@/api/ai'

const messages = ref<ChatMessage[]>([])
const input = ref('')
const loading = ref(false)
const listRef = ref<HTMLElement | null>(null)

async function scrollToBottom() {
  await nextTick()
  listRef.value?.scrollTo({ top: listRef.value.scrollHeight })
}

// ✏️ 核心逻辑，你自己写：
async function handleSend() {
  // ✏️ 自己写。目标五个：
  //   ① 空输入 → 提示 + 提前返回
  //   ② 用户消息进列表，并清空输入框
  //   ③ 开启 loading
  //   ④ 调 chat() 拿回复，回复进列表
  //   ⑤ 成功或失败都要关掉 loading
  //   ⑥ 每次都滚到底部

  // 要注意的点（不给答案，只说哪里容易错）：
  //   · input 是 ref，在 script 里读写都要 .value
  //   · messages 是 ref，推数组要 messages.value.push(...)
  //   · chat() 要的是【数组】，不是 ref 对象
  //   · loading 的关闭要放在【所有路径都会走到】的地方
  const inputValue = input.value.trim()
  if (!inputValue){
    ElMessage.warning('输入为空')
    return
  }
  messages.value.push({role:"user",content:inputValue})
  input.value=''
  loading.value=true
  scrollToBottom()
  try{
    const reply = await chat(messages.value)
    messages.value.push({role:"assistant",content:reply})
    scrollToBottom()
  }catch(error){
    const msg = error instanceof Error ? error.message : '请求失败'
    ElMessage.error(msg)
  }finally{
    loading.value=false
  }
}

function handleClear() {
  messages.value = []
}
</script>

<style scoped>
.ai-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 140px);
}

.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.msg-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 4px;
}

.msg {
  display: flex;
  margin-bottom: 12px;
}

.msg.user {
  justify-content: flex-end;
}

.bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 10px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg.user .bubble {
  background: #409eff;
  color: #fff;
}

.msg.assistant .bubble {
  background: #f4f4f5;
  color: #303133;
}

.input-bar {
  display: flex;
  gap: 10px;
  align-items: flex-end;
  margin-top: 12px;
}
</style>
