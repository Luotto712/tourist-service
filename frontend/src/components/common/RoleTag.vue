<template>
  <el-tag
    :type="tagType"
    :size="size"
    :effect="effect"
    :round="round"
    class="role-tag"
  >
    {{ roleLabelC }}
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'
import { roleLabel, roleType } from '@/constants/roles'

const props = defineProps({
  role: {
    type: String,
    required: true
  },
  size: {
    type: String,
    default: 'default'
  },
  effect: {
    type: String,
    default: 'light'
  },
  round: {
    type: Boolean,
    default: false
  }
})

const roleLabelC = computed(() => roleLabel[props.role] || props.role || '未知角色')

const tagType = computed(() => roleType[props.role] || 'info')
</script>

<style lang="scss" scoped>
.role-tag {
  font-weight: 500;
}
</style>
