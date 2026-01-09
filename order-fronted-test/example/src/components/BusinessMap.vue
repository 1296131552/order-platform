<template>
  <div class="business-map">
    <div id="map-container" ref="mapContainer" class="map-container"></div>
    <!-- 图例 -->
    <div class="map-legend" v-if="showLegend">
      <div class="legend-title">图例</div>
      <div class="legend-items">
        <div class="legend-item" v-for="item in legendItems" :key="item.status">
          <span class="legend-color" :style="{ background: item.color }"></span>
          <span class="legend-label">{{ item.label }}</span>
        </div>
      </div>
    </div>
    <!-- 统计信息 -->
    <div class="map-stats" v-if="showStats">
      <div class="stat-item">
        <span class="stat-label">总发运批次</span>
        <span class="stat-value">{{ routeCount }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">在途</span>
        <span class="stat-value stat-warning">{{ inTransitCount }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">已到货</span>
        <span class="stat-value stat-success">{{ deliveredCount }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'

/** 线路信息 */
export interface RouteInfo {
  shipmentId: number
  shipmentNo: string
  status: string // pending, in_transit, delivered
  start: {
    longitude: number
    latitude: number
    location: string
  }
  end: {
    longitude: number
    latitude: number
    location: string
  }
  driverName?: string
  driverPhone?: string
  estimatedArrivalTime?: string
}

interface Props {
  routes: RouteInfo[]
  showLegend?: boolean
  showStats?: boolean
  center?: { longitude: number; latitude: number }
  zoom?: number
}

const props = withDefaults(defineProps<Props>(), {
  showLegend: true,
  showStats: true,
  center: undefined,
  zoom: 5
})

const emit = defineEmits<{
  routeClick: [route: RouteInfo]
}>()

// 地图实例
const mapContainer = ref<HTMLElement>()
let mapInstance: any = null
let markers: any[] = []
let polylines: any[] = []

// 状态颜色映射
const statusColorMap: Record<string, string> = {
  pending: '#909399',      // 灰色 - 待提货
  in_transit: '#e6a23c',   // 橙色 - 在途
  delivered: '#67c23a'      // 绿色 - 已到货
}

// 图例数据
const legendItems = [
  { status: 'pending', label: '待提货', color: statusColorMap.pending },
  { status: 'in_transit', label: '在途', color: statusColorMap.in_transit },
  { status: 'delivered', label: '已到货', color: statusColorMap.delivered }
]

// 统计数据
const routeCount = computed(() => props.routes.length)
const inTransitCount = computed(() => props.routes.filter(r => r.status === 'in_transit').length)
const deliveredCount = computed(() => props.routes.filter(r => r.status === 'delivered').length)

// 初始化地图
const initMap = () => {
  if (!window.AMap) {
    console.error('高德地图未加载')
    return
  }

  // 创建地图实例
  const center = props.center || { longitude: 116.397428, latitude: 39.90923 } // 默认北京
  mapInstance = new window.AMap.Map('map-container', {
    zoom: props.zoom,
    center: [center.longitude, center.latitude],
    mapStyle: 'amap://styles/normal',
    features: ['bg', 'road', 'building'],
    viewMode: '2D'
  })

  // 添加工具条
  mapInstance.addControl(new window.AMap.ToolBar({
    position: {
      top: '110px',
      right: '40px'
    }
  }))

  // 添加比例尺
  mapInstance.addControl(new window.AMap.Scale())

  // 绘制线路
  drawRoutes()
}

// 绘制线路
const drawRoutes = () => {
  if (!mapInstance || !props.routes.length) return

  // 清除旧的标记和线路
  clearMap()

  props.routes.forEach(route => {
    const color = statusColorMap[route.status] || '#409eff'

    // 创建起点标记
    const startMarker = new window.AMap.Marker({
      position: [route.start.longitude, route.start.latitude],
      icon: new window.AMap.Icon({
        size: new window.AMap.Size(32, 32),
        image: 'https://webapi.amap.com/theme/v1.3/markers/n/start.png',
        imageSize: new window.AMap.Size(32, 32)
      }),
      title: route.start.location
    })

    // 创建终点标记
    const endMarker = new window.AMap.Marker({
      position: [route.end.longitude, route.end.latitude],
      icon: new window.AMap.Icon({
        size: new window.AMap.Size(32, 32),
        image: 'https://webapi.amap.com/theme/v1.3/markers/n/end.png',
        imageSize: new window.AMap.Size(32, 32)
      }),
      title: route.end.location
    })

    // 创建线路
    const polyline = new window.AMap.Polyline({
      path: [
        [route.start.longitude, route.start.latitude],
        [route.end.longitude, route.end.latitude]
      ],
      strokeColor: color,
      strokeWeight: 4,
      strokeOpacity: 0.8,
      strokeStyle: 'solid',
      lineJoin: 'round'
    })

    // 添加信息窗体
    const infoWindow = new window.AMap.InfoWindow({
      content: `
        <div style="padding: 8px;">
          <div style="font-weight: bold; margin-bottom: 4px;">${route.shipmentNo}</div>
          <div>起点：${route.start.location}</div>
          <div>终点：${route.end.location}</div>
          ${route.driverName ? `<div>司机：${route.driverName}</div>` : ''}
          ${route.driverPhone ? `<div>电话：${route.driverPhone}</div>` : ''}
          ${route.estimatedArrivalTime ? `<div>预计到达：${route.estimatedArrivalTime}</div>` : ''}
        </div>
      `
    })

    // 标记点击事件
    startMarker.on('click', () => {
      infoWindow.open(mapInstance, startMarker.getPosition())
      emit('routeClick', route)
    })

    endMarker.on('click', () => {
      infoWindow.open(mapInstance, endMarker.getPosition())
      emit('routeClick', route)
    })

    // 线路点击事件
    polyline.on('click', () => {
      infoWindow.open(mapInstance, polyline.getPath()[0])
      emit('routeClick', route)
    })

    mapInstance.add(startMarker)
    mapInstance.add(endMarker)
    mapInstance.add(polyline)

    markers.push(startMarker, endMarker)
    polylines.push(polyline)
  })

  // 自适应视野
  const bounds = new window.AMap.Bounds()
  props.routes.forEach(route => {
    bounds.extend([route.start.longitude, route.start.latitude])
    bounds.extend([route.end.longitude, route.end.latitude])
  })
  mapInstance.setFitView()
}

// 清除地图上的标记和线路
const clearMap = () => {
  markers.forEach(marker => mapInstance.remove(marker))
  polylines.forEach(polyline => mapInstance.remove(polyline))
  markers = []
  polylines = []
}

// 刷新地图
const refreshMap = () => {
  if (mapInstance) {
    drawRoutes()
  }
}

// 监听路由变化
watch(() => props.routes, () => {
  refreshMap()
}, { deep: true })

// 组件挂载
onMounted(() => {
  // 动态加载高德地图脚本
  if (!window.AMap) {
    const script = document.createElement('script')
    script.type = 'text/javascript'
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${import.meta.env.VITE_AMAP_KEY || 'your-amap-key'}&plugin=AMap.Scale,AMap.ToolBar`
    script.onload = () => {
      initMap()
    }
    document.head.appendChild(script)
  } else {
    initMap()
  }
})

// 组件卸载
onUnmounted(() => {
  if (mapInstance) {
    mapInstance.destroy()
  }
})

// 暴露方法
defineExpose({
  refreshMap,
  clearMap
})
</script>

<style scoped lang="scss">
.business-map {
  position: relative;
  width: 100%;
  height: 500px;

  .map-container {
    width: 100%;
    height: 100%;
  }

  .map-legend {
    position: absolute;
    top: 16px;
    left: 16px;
    padding: 12px;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    z-index: 100;

    .legend-title {
      font-weight: bold;
      margin-bottom: 8px;
      font-size: 14px;
    }

    .legend-items {
      .legend-item {
        display: flex;
        align-items: center;
        margin-bottom: 6px;

        &:last-child {
          margin-bottom: 0;
        }

        .legend-color {
          width: 16px;
          height: 4px;
          border-radius: 2px;
          margin-right: 8px;
        }

        .legend-label {
          font-size: 12px;
          color: var(--el-text-color-regular);
        }
      }
    }
  }

  .map-stats {
    position: absolute;
    top: 16px;
    right: 16px;
    padding: 12px 16px;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    z-index: 100;

    .stat-item {
      display: flex;
      align-items: center;
      margin-bottom: 8px;

      &:last-child {
        margin-bottom: 0;
      }

      .stat-label {
        font-size: 12px;
        color: var(--el-text-color-secondary);
        margin-right: 12px;
      }

      .stat-value {
        font-size: 18px;
        font-weight: bold;
        color: var(--el-text-color-primary);

        &.stat-warning {
          color: var(--el-color-warning);
        }

        &.stat-success {
          color: var(--el-color-success);
        }
      }
    }
  }
}
</style>
