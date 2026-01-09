/**
 * 路由定义
 */

import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: {
      title: '登录',
      hideInMenu: true
    }
  },
  {
    path: '/',
    component: () => import('@/views/layout/DefaultLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Index.vue'),
        meta: {
          title: '数据看板',
          icon: 'DataBoard'
        }
      },
      {
        path: 'order',
        name: 'OrderList',
        component: () => import('@/views/order/List.vue'),
        meta: {
          title: '订单管理',
          icon: 'Document'
        }
      },
      {
        path: 'order/:id',
        name: 'OrderDetail',
        component: () => import('@/views/order/Detail.vue'),
        meta: {
          title: '订单详情',
          hidden: true
        }
      },
      {
        path: 'order/create',
        name: 'OrderCreate',
        component: () => import('@/views/order/Create.vue'),
        meta: {
          title: '创建订单',
          hidden: true
        }
      },
      {
        path: 'supplier',
        name: 'SupplierList',
        component: () => import('@/views/supplier/List.vue'),
        meta: {
          title: '供应商管理',
          icon: 'User'
        }
      },
      {
        path: 'supplier/:id',
        name: 'SupplierDetail',
        component: () => import('@/views/supplier/Detail.vue'),
        meta: {
          title: '供应商详情',
          hidden: true
        }
      },
      {
        path: 'supplier/create',
        name: 'SupplierCreate',
        component: () => import('@/views/supplier/Create.vue'),
        meta: {
          title: '新建供应商',
          hidden: true
        }
      },
      {
        path: 'supplier/edit/:id',
        name: 'SupplierEdit',
        component: () => import('@/views/supplier/Create.vue'),
        meta: {
          title: '编辑供应商',
          hidden: true
        }
      },
      {
        path: 'carrier',
        name: 'CarrierList',
        component: () => import('@/views/carrier/List.vue'),
        meta: {
          title: '承运商管理',
          icon: 'Van'
        }
      },
      {
        path: 'carrier/:id',
        name: 'CarrierDetail',
        component: () => import('@/views/carrier/Detail.vue'),
        meta: {
          title: '承运商详情',
          hidden: true
        }
      },
      {
        path: 'carrier/create',
        name: 'CarrierCreate',
        component: () => import('@/views/carrier/Create.vue'),
        meta: {
          title: '新建承运商',
          hidden: true
        }
      },
      {
        path: 'carrier/edit/:id',
        name: 'CarrierEdit',
        component: () => import('@/views/carrier/Create.vue'),
        meta: {
          title: '编辑承运商',
          hidden: true
        }
      },
      {
        path: 'shipment',
        name: 'ShipmentList',
        component: () => import('@/views/shipment/List.vue'),
        meta: {
          title: '发运管理',
          icon: 'Van'
        }
      },
      {
        path: 'shipment/:id',
        name: 'ShipmentDetail',
        component: () => import('@/views/shipment/Detail.vue'),
        meta: {
          title: '发运详情',
          hidden: true
        }
      },
      {
        path: 'receipt',
        name: 'ReceiptList',
        component: () => import('@/views/receipt/List.vue'),
        meta: {
          title: '签收管理',
          icon: 'Box'
        }
      },
      {
        path: 'receipt/confirm/:id',
        name: 'ReceiptConfirm',
        component: () => import('@/views/receipt/Confirm.vue'),
        meta: {
          title: '签收确认',
          hidden: true
        }
      },
      {
        path: 'exception',
        name: 'ExceptionList',
        component: () => import('@/views/exception/List.vue'),
        meta: {
          title: '异常管理',
          icon: 'Warning'
        }
      },
      {
        path: 'attachment',
        name: 'AttachmentList',
        component: () => import('@/views/attachment/List.vue'),
        meta: {
          title: '附件管理',
          icon: 'FolderOpened'
        }
      },
      {
        path: 'system',
        name: 'System',
        component: () => import('@/views/system/Index.vue'),
        meta: {
          title: '系统管理',
          icon: 'Setting'
        }
      },
      {
        path: 'data/import',
        name: 'DataImport',
        component: () => import('@/views/data/Import.vue'),
        meta: {
          title: '数据导入',
          icon: 'Upload',
          hideInMenu: true
        }
      },
      {
        path: 'data/export',
        name: 'DataExport',
        component: () => import('@/views/data/Export.vue'),
        meta: {
          title: '数据导出',
          icon: 'Download',
          hideInMenu: true
        }
      },
      {
        path: 'billing',
        name: 'BillingList',
        component: () => import('@/views/billing/List.vue'),
        meta: {
          title: '对账管理',
          icon: 'Tickets'
        }
      },
      {
        path: 'billing/:id',
        name: 'BillingDetail',
        component: () => import('@/views/billing/Detail.vue'),
        meta: {
          title: '对账单详情',
          hidden: true
        }
      },
      {
        path: 'billing/create',
        name: 'BillingCreate',
        component: () => import('@/views/billing/Create.vue'),
        meta: {
          title: '创建对账单',
          hidden: true
        }
      }
    ]
  }
]

export default routes
