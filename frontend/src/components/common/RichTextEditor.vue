<template>
  <div class="rich-text-editor">
    <div class="editor-toolbar">
      <el-button-group class="toolbar-group">
        <el-button size="small" @click="execCmd('bold')" :type="isActive('bold') ? 'primary' : 'default'" :plain="!isActive('bold')">
          <b>B</b>
        </el-button>
        <el-button size="small" @click="execCmd('italic')" :type="isActive('italic') ? 'primary' : 'default'" :plain="!isActive('italic')">
          <i>I</i>
        </el-button>
        <el-button size="small" @click="execCmd('underline')" :type="isActive('underline') ? 'primary' : 'default'" :plain="!isActive('underline')">
          <u>U</u>
        </el-button>
        <el-button size="small" @click="execCmd('strikeThrough')" :type="isActive('strikeThrough') ? 'primary' : 'default'" :plain="!isActive('strikeThrough')">
          <s>S</s>
        </el-button>
      </el-button-group>

      <el-divider direction="vertical" />

      <el-button size="small" @click="execHeading" plain>
        H&nbsp;<el-icon><ArrowDown /></el-icon>
      </el-button>

      <el-divider direction="vertical" />

      <el-button size="small" @click="triggerFileUpload" plain type="primary">
        <el-icon><Upload /></el-icon>&nbsp;上传文件
      </el-button>

      <el-divider direction="vertical" />

      <el-button size="small" @click="execCmd('insertUnorderedList')" plain>
        <el-icon><List /></el-icon>&nbsp;无序列表
      </el-button>
      <el-button size="small" @click="execCmd('insertOrderedList')" plain style="margin-left: 4px;">
        <el-icon><List /></el-icon>&nbsp;有序列表
      </el-button>

      <el-divider direction="vertical" />

      <el-button size="small" @click="clearFormat" plain>
        清除格式
      </el-button>
    </div>

    <div
      ref="editorRef"
      class="editor-content"
      contenteditable="true"
      @input="onInput"
      @keydown.tab.prevent="execCmd('indent')"
      @paste="onPaste"
      @mouseup="updateToolbar"
      @keyup="updateToolbar"
    ></div>

    <input ref="fileInput" type="file" style="display:none" @change="onFileSelected" />
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ArrowDown, Link, Upload, List } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const props = defineProps({
  modelValue: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const editorRef = ref(null)
const fileInput = ref(null)
const savedRange = ref(null)

function execCmd(command, value) {
  document.execCommand(command, false, value || null)
  editorRef.value?.focus()
  updateToolbar()
  emitContent()
}

function isActive(command) { return document.queryCommandState(command) }

function execHeading() {
  const formatBlock = document.queryCommandValue('formatBlock')
  if (formatBlock === 'h2' || formatBlock === 'heading 2') { execCmd('formatBlock', '<p>') }
  else { execCmd('formatBlock', '<h2>') }
}

function saveSelection() {
  const selection = window.getSelection()
  if (selection.rangeCount > 0) savedRange.value = selection.getRangeAt(0).cloneRange()
}

function restoreSelection() {
  if (savedRange.value) {
    const selection = window.getSelection()
    selection.removeAllRanges()
    selection.addRange(savedRange.value)
  }
}

function triggerFileUpload() {
  saveSelection()
  fileInput.value?.click()
}

async function onFileSelected(e) {
  const file = e.target.files?.[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)

  try {
    const res = await request.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    const data = res.data || res
    const fileId = data.id || data.fileId
    const fileName = data.fileName || file.name
    if (fileId) {
      restoreSelection()
      const ext = (file.name.split('.').pop() || '').toLowerCase()
      const icon = ['jpg','jpeg','png','gif','bmp','webp'].includes(ext) ? '&#128247;' : '&#128196;'
      const cardHtml = `<div contenteditable="false" style="display:inline-block;vertical-align:middle;padding:8px 14px;margin:4px;background:#f0f7ff;border:1px solid #d0e3f7;border-radius:6px;font-size:13px;cursor:default;">
        <span style="font-size:18px;margin-right:6px;">${icon}</span>
        <a href="/api/files/${fileId}/download" target="_blank" style="color:#409eff;text-decoration:none;font-weight:500;">${fileName}</a>
        <span style="color:#909399;font-size:11px;margin-left:8px;">${(file.size / 1024).toFixed(1)} KB</span>
      </div>&nbsp;`
      execCmd('insertHTML', cardHtml)
      ElMessage.success(`文件 "${fileName}" 已上传`)
    }
  } catch {
    ElMessage.error('文件上传失败')
  }
  fileInput.value.value = ''
}

function clearFormat() { execCmd('removeFormat') }
function onInput() { emitContent() }

function onPaste(e) {
  e.preventDefault()
  const text = e.clipboardData?.getData('text/plain') || ''
  execCmd('insertText', text)
}

function updateToolbar() {}
function emitContent() {
  if (editorRef.value) emit('update:modelValue', editorRef.value.innerHTML)
}
function setContent(html) {
  if (editorRef.value && html !== editorRef.value.innerHTML) editorRef.value.innerHTML = html || ''
}
watch(() => props.modelValue, (val) => setContent(val))
onMounted(() => setContent(props.modelValue))
</script>

<style lang="scss" scoped>
.rich-text-editor {
  border: 1px solid #dcdfe6; border-radius: 6px; overflow: hidden; background: #fff;
  .editor-toolbar {
    display: flex; align-items: center; flex-wrap: wrap; gap: 4px; padding: 8px 10px;
    background: #f5f7fa; border-bottom: 1px solid #e4e7ed;
    .toolbar-group .el-button { padding: 5px 8px; min-width: 32px; s { text-decoration: line-through; font-size: 13px; } }
  }
  .editor-content {
    min-height: 200px; max-height: 500px; overflow-y: auto; padding: 12px 16px;
    font-size: 14px; line-height: 1.8; color: #303133; outline: none;
    &:focus { box-shadow: 0 0 0 2px rgba(64,158,255,0.2) inset; }
    :deep(a) { color: #409eff; text-decoration: underline; }
    :deep(.file-attachment) { display: inline-block; padding: 2px 8px; background: #ecf5ff; border-radius: 4px; margin: 2px 4px; color: #409eff; }
    :deep(img) { max-width: 100%; height: auto; border-radius: 4px; margin: 8px 0; }
    :deep(h2) { font-size: 18px; font-weight: 600; margin: 12px 0 8px; color: #303133; }
    :deep(ul), :deep(ol) { padding-left: 24px; margin: 8px 0; }
    :deep(li) { margin: 4px 0; }
    :deep(b), :deep(strong) { font-weight: 600; }
    :deep(i), :deep(em) { font-style: italic; }
    :deep(u) { text-decoration: underline; }
    :deep(s), :deep(strike) { text-decoration: line-through; }
  }
}
</style>
