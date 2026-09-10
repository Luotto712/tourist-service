// 查询类目录配置：资源路径 + 展示列。前后端都走「通用目录」模式。
// resource 为后端 /api/<resource> 的路径；列 prop 对应实体 JSON 字段。
export const catalogs = {
  attractions: {
    title: '景点',
    resource: 'attractions',
    columns: [
      { prop: 'name', label: '名称' },
      { prop: 'type', label: '类型' },
      { prop: 'address', label: '地址' },
      { prop: 'openTime', label: '开放时间' },
      { prop: 'intro', label: '简介' }
    ]
  },
  routes: {
    title: '旅游线路',
    resource: 'routes',
    columns: [
      { prop: 'name', label: '线路名称' },
      { prop: 'duration', label: '建议时长' },
      { prop: 'attractionIds', label: '包含景点', ref: 'attractions' },
      { prop: 'intro', label: '简介' }
    ]
  },
  catering: {
    title: '餐饮娱乐',
    resource: 'catering',
    columns: [
      { prop: 'name', label: '名称' },
      { prop: 'type', label: '类型' },
      { prop: 'address', label: '地址' },
      { prop: 'price', label: '价格' },
      { prop: 'intro', label: '简介' }
    ]
  },
  'performance-groups': {
    title: '演出团体',
    resource: 'performance-groups',
    columns: [
      { prop: 'name', label: '团体名称' },
      { prop: 'type', label: '类型' },
      { prop: 'address', label: '地址' },
      { prop: 'contact', label: '联系方式' }
    ]
  },
  transport: {
    title: '景区交通',
    resource: 'transport',
    columns: [
      { prop: 'name', label: '名称' },
      { prop: 'type', label: '类型' },
      { prop: 'area', label: '区域' },
      { prop: 'schedule', label: '班次/时刻' },
      { prop: 'price', label: '价格' }
    ]
  },
  'hotels/star': {
    title: '星级酒店',
    resource: 'hotels/star',
    columns: [
      { prop: 'name', label: '酒店名称' },
      { prop: 'level', label: '星级' },
      { prop: 'address', label: '地址' },
      { prop: 'tel', label: '电话' },
      { prop: 'intro', label: '简介' }
    ]
  },
  'hotels/nonstar': {
    title: '非星级/乡村酒店',
    resource: 'hotels/nonstar',
    columns: [
      { prop: 'name', label: '酒店名称' },
      { prop: 'type', label: '类型' },
      { prop: 'address', label: '地址' },
      { prop: 'tel', label: '电话' },
      { prop: 'intro', label: '简介' }
    ]
  }
}
