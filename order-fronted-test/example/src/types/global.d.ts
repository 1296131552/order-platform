/**
 * API 类型定义
 * 统一管理所有API相关的类型定义
 */

declare namespace Api {
  // ==================== 通用类型 ====================

  /**
   * 分页查询参数
   */
  interface PageParams {
    /** 当前页码 */
    current: number
    /** 每页条数 */
    size: number
    /** 排序字段 */
    sortField?: string
    /** 排序方式：asc/desc */
    sortOrder?: 'asc' | 'desc'
  }

  /**
   * 分页响应数据
   */
  interface PageResult<T> {
    /** 数据列表 */
    records: T[]
    /** 总记录数 */
    total: number
    /** 当前页码 */
    current: number
    /** 每页条数 */
    size: number
    /** 总页数 */
    pages: number
  }

  /**
   * 统一响应格式
   */
  interface Result<T = any> {
    /** 响应码：200成功，其他失败 */
    code: number
    /** 响应消息 */
    message: string
    /** 响应数据 */
    data: T
    /** 时间戳 */
    timestamp: string
  }

  // ==================== 订单模块 ====================

  namespace Order {
    /**
   * 订单查询参数
   */
  interface QueryParams extends PageParams {
    /** 订单号 */
    orderNo?: string
    /** 客户名称 */
    customerName?: string
    /** 订单状态 */
    statusCode?: string
    /** 创建开始日期 */
    startDate?: string
    /** 创建结束日期 */
    endDate?: string
  }

  /**
   * 订单信息
   */
  interface Order {
    /** 订单ID */
    id: number
    /** 订单号 */
    orderNo: string
    /** 客户ID */
    customerId: number
    /** 客户名称 */
    customerName: string
    /** 订单状态代码 */
    statusCode: string
    /** 订单总额 */
    totalAmount: number
    /** 订单行数 */
    lineCount: number
    /** 创建人ID */
    createdBy: string
    /** 创建人姓名 */
    createdByName: string
    /** 创建时间 */
    createdAt: string
    /** 更新时间 */
    updatedAt: string
    /** 备注 */
    remark: string
  }

  /**
   * 订单行信息
   */
  interface OrderLine {
    /** 订单行ID */
    id: number
    /** 订单ID */
    orderId: number
    /** 行号 */
    lineNo: number
    /** 供应商ID */
    supplierId: number
    /** 供应商名称 */
    supplierName: string
    /** 产品编码 */
    productCode: string
    /** 产品名称 */
    productName: string
    /** 数量 */
    quantity: number
    /** 单价 */
    unitPrice: number
    /** 金额 */
    totalAmount: number
    /** 状态代码 */
    statusCode: string
    /** 交货日期 */
    deliveryDate: string
    /** 备注 */
    remark: string
  }

  /**
   * 创建订单DTO
   */
  interface CreateDTO {
    /** 客户ID */
    customerId: number
    /** 订单行列表 */
    lines: OrderLineDTO[]
    /** 备注 */
    remark?: string
  }

  /**
   * 更新订单DTO
   */
  interface UpdateDTO {
    /** 订单ID */
    id: number
    /** 客户ID */
    customerId?: number
    /** 订单行列表 */
    lines?: OrderLineDTO[]
    /** 备注 */
    remark?: string
  }

  /**
   * 订单行DTO
   */
  interface OrderLineDTO {
    /** 行号 */
    lineNo: number
    /** 供应商ID */
    supplierId: number
    /** 产品编码 */
    productCode: string
    /** 数量 */
    quantity: number
    /** 单价 */
    unitPrice: number
    /** 交货日期 */
    deliveryDate: string
    /** 备注 */
    remark?: string
  }
}

// ==================== 发运模块 ====================

namespace Shipment {
  interface QueryParams extends PageParams {
    shipmentNo?: string
    orderNo?: string
    statusCode?: string
  }

  interface Shipment {
    id: number
    shipmentNo: string
    orderId: number
    orderNo: string
    statusCode: string
    createdAt: string
    updatedAt: string
  }

  interface CreateDTO {
    orderId: number
    lines: ShipmentLineDTO[]
    remark?: string
  }

  interface ShipmentLineDTO {
    lineNo: number
    supplierId: number
    productId: number
    trackingNo: string
    quantity: number
    remark?: string
  }
}

// ==================== 签收模块 ====================

namespace Receipt {
  interface QueryParams extends PageParams {
    trackingNo?: string
    statusCode?: string
  }

  interface Receipt {
    id: number
    shipmentLineId: number
    trackingNo: string
    productId: number
    expectedQuantity: number
    receivedQuantity: number
    differenceQuantity: number
    receiptDate: string
    remark: string
  }

  interface ConfirmDTO {
    receiptInfoList: ReceiptDTO[]
    remark?: string
  }

  interface ReceiptDTO {
    shipmentLineId: number
    receivedQuantity: number
    remark?: string
  }
}

// ==================== 异常模块 ====================

namespace Exception {
  interface QueryParams extends PageParams {
    exceptionType?: string
    statusCode?: string
    priority?: string
  }

  interface Exception {
    id: number
    exceptionNo: string
    exceptionType: string
    title: string
    description: string
    statusCode: string
    priority: string
    createdBy: string
    createdAt: string
  }

  interface CreateDTO {
    exceptionType: string
    title: string
    description: string
    priority: string
    businessType: string
    businessId: number
  }

  interface HandleDTO {
    exceptionId: number
    handleResult: string
    handleRemark: string
  }
}

// ==================== 附件模块 ====================

namespace Attachment {
  interface UploadParams {
    fileType: string
    businessType: string
    businessId: number
    file: File
  }

  interface Attachment {
    id: number
    fileName: string
    fileType: string
    fileSize: number
    filePath: string
    uploadBy: string
    uploadTime: string
  }

  interface ListParams {
    businessType: string
    businessId: number
  }
}

// ==================== 合作方模块 ====================

namespace Partner {
  interface Customer {
    id: number
    customerCode: string
    customerName: string
    contactName: string
    contactPhone: string
    address: string
  }

  interface Supplier {
    id: number
    supplierCode: string
    supplierName: string
    contactName: string
    contactPhone: string
    address: string
  }

  interface Carrier {
    id: number
    carrierCode: string
    carrierName: string
    contactName: string
    contactPhone: string
    address: string
  }
}

// ==================== 系统配置模块 ====================

namespace System {
  interface Config {
    id: number
    configKey: string
    configValue: string
    configType: string
    description?: string
  }

  interface UpdateDTO {
    id: number
    configValue: string
    description?: string
  }

  interface ConfigQueryParams {
    configType?: string
    configKey?: string
  }
}

// ==================== 用户权限模块 ====================

namespace User {
  interface LoginParams {
    username: string
    password: string
    captcha?: string
  }

  interface UserInfo {
    id: number
    username: string
    realName: string
    email: string
    phone: string
    avatar?: string
    isEnabled: boolean
    roles: Role[]
  }

  interface Role {
    id: number
    roleCode: string
    roleName: string
    roleDesc?: string
    permissions: Permission[]
  }

  interface Permission {
    id: number
    permissionCode: string
    permissionName: string
    permissionType: string
    resource: string
    action: string
  }
}

/**
 * 高德地图类型声明
 */

declare global {
  interface Window {
    AMap: {
      Map: new (containerId: string, options?: any) => AMapInstance
      Marker: new (options: any) => any
      Icon: new (options: any) => any
      Polyline: new (options: any) => any
      InfoWindow: new (content: string) => any
      ToolBar: new (options?: any) => any
      Scale: new (options?: any) => any
      Bounds: new () => any
    }

    AMapInstance: any
  }
}

export {}

