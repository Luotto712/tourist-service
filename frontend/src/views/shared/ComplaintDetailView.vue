<template>
  <el-card v-loading="loading">
    <template #header>
      <div class="header-bar">
        <span>投诉详情</span>
        <el-button link @click="$router.back()">返回</el-button>
      </div>
    </template>

    <template v-if="detail">
      <el-alert v-if="detail.status === 'REJECTED'" title="投诉未通过审批" type="error" :closable="false" style="margin-bottom: 16px" />

      <el-steps :active="stepIndex" finish-status="success" align-center style="margin-bottom: 24px">
        <el-step v-for="s in steps" :key="s" :title="s" />
      </el-steps>

      <el-descriptions :column="1" border>
        <el-descriptions-item label="提交人">{{ detail.userName }}</el-descriptions-item>
        <el-descriptions-item label="投诉内容">
          {{ detail.content }}
          <div v-if="thumbnails.length" class="thumb-grid">
            <div v-for="f in thumbnails" :key="f.id" class="thumb-item" @click="download(f)" title="点击下载">
              <img v-if="isImage(f.fileExt)" :src="previewUrl(f.id)" class="thumb-img" alt="附件" />
              <video v-else-if="isVideo(f.fileExt)" :src="previewUrl(f.id)" class="thumb-video" controls @click.stop />
              <div v-else class="thumb-file"><el-icon><Document /></el-icon><span class="thumb-name">{{ f.fileName }}</span></div>
            </div>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(detail.status)">{{ statusText[detail.status] || detail.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理人员">{{ detail.handlerName || '待分派' }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.result" label="处理结果">{{ detail.result }}</el-descriptions-item>
        <el-descriptions-item label="评分">
          <el-rate :model-value="detail.rating" disabled v-if="detail.rating" />
          <span v-else>未评分</span>
        </el-descriptions-item>
      </el-descriptions>

      <div class="section">
        <h4>回复记录</h4>
        <el-timeline>
          <el-timeline-item v-for="(rec, i) in mergedRecords" :key="i" :timestamp="formatTime(rec.time)" placement="top">
            <el-tag :type="rec.tag" size="small">{{ rec.role }}</el-tag>
            <span class="rec-name">{{ rec.name }}</span>
            <p class="rec-text">{{ rec.text }}</p>
            <div v-if="rec.files && rec.files.length" class="thumb-grid">
              <div v-for="f in rec.files" :key="f.id" class="thumb-item" @click="download(f)" title="点击下载">
                <img v-if="isImage(f.fileExt)" :src="previewUrl(f.id)" class="thumb-img" alt="附件" />
                <video v-else-if="isVideo(f.fileExt)" :src="previewUrl(f.id)" class="thumb-video" controls @click.stop />
                <div v-else class="thumb-file"><el-icon><Document /></el-icon><span class="thumb-name">{{ f.fileName }}</span></div>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-if="!mergedRecords.length" description="暂无记录" :image-size="60" />
      </div>

      <div class="actions">
        <el-button v-if="isOwner && detail.status !== 'CLOSED' && detail.status !== 'PENDING'" type="primary" plain @click="openReply">回复</el-button>
        <el-button v-if="isOwner && detail.status === 'RESOLVED'" type="primary" :loading="acting" @click="handleConfirm">确认处理意见</el-button>
        <div class="action-row" v-if="isOwner && detail.status === 'CLOSED' && !detail.rating">
          <span>请评价：</span>
          <el-rate v-model="rating" />
          <el-button type="primary" :loading="acting" @click="handleRate">提交评分</el-button>
        </div>
      </div>
    </template>

    <el-dialog v-model="replyVisible" title="追加回复" width="560px" :close-on-click-modal="false">
      <el-input v-model="replyContent" type="textarea" :rows="4" placeholder="请输入回复内容" />
      <div class="reply-upload">
        <el-upload
          multiple
          :auto-upload="false"
          accept="image/*,video/*"
          :on-change="onReplyFileChange"
          :on-remove="onReplyFileRemove"
          :file-list="replyFileList"
          list-type="picture-card"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
      </div>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="acting" @click="handleReply">发送</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Document } from '@element-plus/icons-vue'
import { getComplaint, replyComplaint, confirmComplaint, rateComplaint } from '@/api/complaint'
import { uploadFile } from '@/api/file'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()
const complaintId = computed(() => Number(route.params.id))

const loading = ref(false)
const acting = ref(false)
const detail = ref(null)
const rating = ref(0)
const replyVisible = ref(false)
const replyContent = ref('')
const replyFileList = ref([])
const replySelected = ref([])

const steps = ['待审批', '已通过', '处理中', '处理完成', '已确认', '已结案']
const stepMap = { PENDING: 0, APPROVED: 1, PROCESSING: 2, RESOLVED: 3, CONFIRMED: 4, CLOSED: 5 }
const statusText = { PROCESSING: '处理中', RESOLVED: '处理完成', CONFIRMED: '已确认', CLOSED: '已结案' }
const statusTypeMap = { REJECTED: 'danger', APPROVED: 'success', RESOLVED: 'success', CONFIRMED: 'success', CLOSED: 'info', PENDING: 'warning', PROCESSING: 'primary' }

const stepIndex = computed(() => stepMap[detail.value?.status] ?? 0)
function statusType(s) { return statusTypeMap[s] || 'info' }
const isOwner = computed(() => !!detail.value && detail.value.userId === userStore.userId)
const thumbnails = computed(() => (detail.value && detail.value.attachments) || [])

// 合并「回复记录」：提交 → 审批(通过/驳回) → 回复 → 处理 → 确认 → 结案 → 评分，按时间排序
const mergedRecords = computed(() => {
  if (!detail.value) return []
  const d = detail.value
  const recs = []
  recs.push({ role: '游客', name: d.userName, text: d.content, time: d.createTime, tag: 'primary' })
  ;(d.timeline || []).forEach(r => {
    const ok = r.action === 'APPROVE'
    const txt = (r.nodeName ? r.nodeName + '：' : '') + (ok ? '审批通过' : '审批驳回') + (r.comment ? '；意见：' + r.comment : '')
    recs.push({ role: '审批人员', name: r.approverName, text: txt, time: r.createTime, tag: 'success' })
  })
  ;(d.replies || []).forEach(r => {
    const owner = r.userId === d.userId
    const roleText = owner ? '游客' : (r.userId === d.handlerId ? '投诉处理人员' : '其他')
    recs.push({ role: roleText, name: r.userName, text: r.content, time: r.createTime, tag: owner ? 'primary' : 'warning', files: r.attachments })
  })
  if (d.confirmTime) recs.push({ role: '游客', name: d.userName, text: '已确认处理意见', time: d.confirmTime, tag: 'primary' })
  if (d.closeTime) recs.push({ role: '平台管理员', name: '平台管理员', text: '已结案', time: d.closeTime, tag: 'info' })
  if (d.rating) recs.push({ role: '游客', name: d.userName, text: '评价 ' + d.rating + ' 星', time: d.closeTime, tag: 'primary' })
  return recs.filter(r => r.text && r.text !== '').slice().sort((a, b) => {
    const at = a.time || '', bt = b.time || ''
    if (!at && !bt) return 0
    if (!at) return 1
    if (!bt) return -1
    return String(at).localeCompare(String(bt))
  })
})

onMounted(fetchDetail)

async function fetchDetail() {
  loading.value = true
  try {
    const res = await getComplaint(complaintId.value)
    detail.value = res.data || res
  } finally {
    loading.value = false
  }
}

function previewUrl(id) { return `/api/files/${id}/download` }
function isImage(ext) { return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes((ext || '').toLowerCase()) }
function isVideo(ext) { return ['mp4', 'avi', 'mov', 'mkv'].includes((ext || '').toLowerCase()) }
function download(f) { window.open(previewUrl(f.id), '_blank') }

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function openReply() {
  replyContent.value = ''
  replyFileList.value = []
  replySelected.value = []
  replyVisible.value = true
}
function onReplyFileChange(file, files) {
  if (file.raw) replySelected.value.push(file.raw)
  replyFileList.value = files
}
function onReplyFileRemove(file, files) {
  replySelected.value = replySelected.value.filter(r => r !== file.raw)
  replyFileList.value = files
}

async function handleReply() {
  if (!replyContent.value.trim()) { ElMessage.warning('请输入回复内容'); return }
  acting.value = true
  try {
    const res = await replyComplaint(complaintId.value, { content: replyContent.value })
    const reply = res.data || res
    for (const raw of replySelected.value) {
      const fd = new FormData()
      fd.append('file', raw)
      fd.append('relatedType', 'COMPLAINT_REPLY')
      fd.append('relatedId', reply.id)
      await uploadFile(fd)
    }
    ElMessage.success('回复成功')
    replyVisible.value = false
    fetchDetail()
  } finally {
    acting.value = false
  }
}

async function handleConfirm() {
  acting.value = true
  try {
    await confirmComplaint(complaintId.value)
    ElMessage.success('已确认处理意见')
    fetchDetail()
  } finally {
    acting.value = false
  }
}

async function handleRate() {
  if (!rating.value) { ElMessage.warning('请选择评分'); return }
  acting.value = true
  try {
    await rateComplaint(complaintId.value, { rating: rating.value })
    ElMessage.success('评价成功')
    fetchDetail()
  } finally {
    acting.value = false
  }
}
</script>

<style lang="scss" scoped>
.header-bar { display: flex; justify-content: space-between; align-items: center; }
.section { margin-top: 24px; h4 { margin: 0 0 10px; } }
.actions { margin-top: 24px; display: flex; flex-direction: column; gap: 16px; }
.action-row { display: flex; align-items: center; gap: 12px; }
.rec-name { margin-left: 8px; font-weight: 500; color: #303133; }
.rec-text { margin: 6px 0 0; color: #606266; line-height: 1.6; white-space: pre-wrap; }
.thumb-grid { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 12px; }
.thumb-item { cursor: pointer; }
.thumb-img { width: 110px; height: 110px; object-fit: cover; border-radius: 6px; border: 1px solid #ebeef5; }
.thumb-video { width: 200px; height: 110px; border-radius: 6px; background: #000; object-fit: cover; }
.thumb-file { display: flex; align-items: center; gap: 6px; padding: 8px 12px; border: 1px solid #ebeef5; border-radius: 6px; }
.thumb-name { font-size: 12px; color: #409eff; }
.reply-upload { margin-top: 12px; }
</style>
