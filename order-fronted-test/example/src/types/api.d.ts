/**
 * API 类型定义
 * 统一管理所有API相关的类型定义
 */

declare namespace Api {
  // ==================== 通用类型 ====================

  /**
   * 统一API响应格式
   */
  interface Response<T = any> {
    code: number
    message: string
    data: T
    timestamp: string
  }

  /**
   * 分页查询参数
   */
  interface PageParams {
    /** 当前页码，从1开始 */
    page?: number
    /** 每页条数，默认10 */
    pageSize?: number
    /** 排序字段 */
    sortField?: string
    /** 排序方式 */
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
    /** 每页条数 */
    size: number
    /** 当前页码 */
    current: number
    /** 总页数 */
    pages: number
  }

  // ==================== 状态枚举 ====================

  /**
   * 订单状态
   */
  type OrderStatus = 'DRAFT' | 'EXECUTING' | 'PARTIALLY_RECEIVED' | 'COMPLETED' | 'CANCELLED'

  /**
   * 发运状态
   */
  type ShipmentStatus = 'PENDING' | 'IN_TRANSIT' | 'DELIVERED'

  /**
   * 签收状态
   */
  type ReceiptStatus = 'PENDING' | 'RECEIVED' | 'DIFFERENCE' | 'PROCESSED'

  /**
   * 用户状态
   */
  type UserStatus = 'ACTIVE' | 'LOCKED' | 'DISABLED'

  /**
   * 合作方状态
   */
  type PartnerStatus = 'ACTIVE' | 'INACTIVE'

  /**
   * 异常级别
   */
  type ExceptionLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

  /**
   * 异常状态
   */
  type ExceptionStatus = 'PENDING' | 'PROCESSING' | 'RESOLVED' | 'CLOSED'

  // ==================== 认证模块 ====================

  namespace Auth {
    /**
     * 登录参数
     */
    interface LoginParams {
      /** 账号（用户名/邮箱/手机号） */
      account: string
      /** 密码 */
      password: string
    }

    /**
     * 用户信息
     */
    interface UserInfo {
      id: number
      username: string
      realName: string
      email?: string
      phone?: string
      avatar?: string
      status: UserStatus
    }

    /**
     * 数据权限范围
     */
    interface DataScope {
      type: string
      scopeIds?: number[]
    }

    /**
     * 登录响应
     */
    interface LoginResult {
      token: string
      tokenType: string
      expiresIn: number
      userInfo: UserInfo
      roles: string[]
      permissions: string[]
      dataScope: DataScope
    }

    /**
     * 修改密码参数
     */
    interface ChangePasswordParams {
      oldPassword: string
      newPassword: string
      confirmPassword: string
    }
  }

  // ==================== 订单模块 ====================

  namespace Order {
    /**
     * 订单查询参数
     */
    interface ListParams extends PageParams {
      /** 订单号 */
      orderNo?: string
      /** 客户ID */
      customerId?: number
      /** 订单状态 */
      status?: OrderStatus
      /** 开始日期 */
      startDate?: string
      /** 结束日期 */
      endDate?: string
      /** 关键字搜索 */
      keyword?: string
    }

    /**
     * 订单信息
     */
    interface Order {
      id: number
      orderNo: string
      customerId: number
      customerName: string
      status: OrderStatus
      totalAmount: number
      lineCount: number
      createdBy: string
      createdByName: string
      createdAt: string
      updatedAt: string
    }

    /**
     * 订单详情（含订单行）
     */
    interface OrderWithLines extends Order {
      lines: OrderLine.OrderLine[]
    }

    /**
     * 创建订单参数
     */
    interface CreateParams {
      customerId: number
      remark?: string
      lines?: OrderLine.AddParams[]
    }

    /**
     * 更新订单参数
     */
    interface UpdateParams {
      customerId?: number
      remark?: string
    }

    /**
     * 订单统计
     */
    interface Statistics {
      totalCount: number
      draftCount: number
      executingCount: number
      completedCount: number
      cancelledCount: number
    }
  }

  // ==================== 订单行模块 ====================

  namespace OrderLine {
    /**
     * 订单行信息
     */
    interface OrderLine {
      id: number
      orderId: number
      lineNo: number
      supplierId: number
      supplierName: string
      productCode: string
      productName: string
      quantity: number
      unitPrice: number
      totalAmount: number
      statusCode: string
      remark?: string
      createdAt: string
      updatedAt: string
    }

    /**
     * 添加订单行参数
     */
    interface AddParams {
      orderId: number
      supplierId: number
      productCode: string
      productName: string
      quantity: number
      unitPrice: number
      remark?: string
    }

    /**
     * 更新订单行参数
     */
    interface UpdateParams {
      id: number
      supplierId?: number
      productCode?: string
      productName?: string
      quantity?: number
      unitPrice?: number
      remark?: string
    }
  }

  // ==================== 发运模块 ====================

  namespace Shipment {
    /**
     * 发运查询参数
     */
    interface ListParams extends PageParams {
      shipmentNo?: string
      orderId?: number
      carrierId?: number
      status?: ShipmentStatus
      startDate?: string
      endDate?: string
    }

    /**
     * 发运单信息
     */
    interface Shipment {
      id: number
      shipmentNo: string
      orderId: number
      orderNo: string
      carrierId: number
      carrierName: string
      status: ShipmentStatus
      departureTime?: string
      estimatedArrivalTime?: string
      actualArrivalTime?: string
      createdAt: string
      updatedAt: string
    }

    /**
     * 创建发运单参数
     */
    interface CreateParams {
      orderId: number
      carrierId: number
      estimatedArrivalTime?: string
      remark?: string
    }

    /**
     * 更新发运单参数
     */
    interface UpdateParams {
      carrierId?: number
      estimatedArrivalTime?: string
      remark?: string
    }

    /**
     * 发运统计
     */
    interface Statistics {
      totalCount: number
      pendingCount: number
      inTransitCount: number
      deliveredCount: number
    }
  }

  // ==================== 快递单模块 ====================

  namespace ShipmentLine {
    /**
     * 快递单查询参数
     */
    interface ListParams extends PageParams {
      shipmentId?: number
      trackingNo?: string
      carrierId?: number
      status?: string
    }

    /**
     * 快递单信息
     */
    interface ShipmentLine {
      id: number
      shipmentId: number
      lineNo: number
      trackingNo: string
      carrierId: number
      carrierName: string
      quantity: number
      weight?: number
      status: string
      createdAt: string
      updatedAt: string
    }

    /**
     * 添加快递单参数
     */
    interface AddParams {
      shipmentId: number
      trackingNo: string
      carrierId: number
      quantity: number
      weight?: number
      remark?: string
    }

    /**
     * 物流事件
     */
    interface TrackingEvent {
      time: string
      location: string
      description: string
    }

    /**
     * 物流信息
     */
    interface TrackingInfo {
      trackingNo: string
      carrier: string
      status: string
      events: TrackingEvent[]
    }
  }

  // ==================== 签收模块 ====================

  namespace Receipt {
    /**
     * 签收查询参数
     */
    interface ListParams extends PageParams {
      shipmentNo?: string
      status?: ReceiptStatus
    }

    /**
     * 签收记录
     */
    interface Receipt {
      id: number
      shipmentLineId: number
      trackingNo: string
      receivedQuantity: number
      expectedQuantity: number
      differenceQuantity: number
      status: ReceiptStatus
      receiptDate: string
      receiverName: string
      hasDifference: boolean
      differenceReason?: string
      photos?: string[]
      remark?: string
      createdAt: string
    }

    /**
     * 确认签收参数
     */
    interface ConfirmParams {
      shipmentLineId: number
      receivedQuantity: number
      receiptDate: string
      receiverName: string
      hasDifference: boolean
      differenceQuantity?: number
      differenceReason?: string
      photos?: string[]
      remark?: string
    }

    /**
     * 差异记录
     */
    interface DifferenceRecord {
      id: number
      receiptId: number
      trackingNo: string
      expectedQuantity: number
      receivedQuantity: number
      differenceQuantity: number
      differenceReason: string
      status: string
      createdAt: string
    }
  }

  // ==================== 供应商模块 ====================

  namespace Supplier {
    /**
     * 供应商查询参数
     */
    interface ListParams extends PageParams {
      supplierNo?: string
      name?: string
      status?: PartnerStatus
    }

    /**
     * 供应商信息
     */
    interface Supplier {
      id: number
      supplierNo: string
      name: string
      contactPerson: string
      contactPhone: string
      contactEmail?: string
      address: string
      status: PartnerStatus
      createdAt: string
      updatedAt: string
    }

    /**
     * 创建供应商参数
     */
    interface CreateParams {
      supplierNo: string
      name: string
      contactPerson: string
      contactPhone: string
      contactEmail?: string
      address: string
    }

    /**
     * 更新供应商参数
     */
    interface UpdateParams {
      name?: string
      contactPerson?: string
      contactPhone?: string
      contactEmail?: string
      address?: string
    }
  }

  // ==================== 客户模块 ====================

  namespace Customer {
    /**
     * 客户查询参数
     */
    interface ListParams extends PageParams {
      customerNo?: string
      name?: string
      status?: PartnerStatus
    }

    /**
     * 客户信息
     */
    interface Customer {
      id: number
      customerNo: string
      name: string
      contactPerson: string
      contactPhone: string
      contactEmail?: string
      address: string
      status: PartnerStatus
      createdAt: string
      updatedAt: string
    }

    /**
     * 创建客户参数
     */
    interface CreateParams {
      customerNo: string
      name: string
      contactPerson: string
      contactPhone: string
      contactEmail?: string
      address: string
    }

    /**
     * 更新客户参数
     */
    interface UpdateParams {
      name?: string
      contactPerson?: string
      contactPhone?: string
      contactEmail?: string
      address?: string
    }
  }

  // ==================== 承运商模块 ====================

  namespace Carrier {
    /**
     * 承运商查询参数
     */
    interface ListParams extends PageParams {
      carrierNo?: string
      name?: string
      status?: PartnerStatus
    }

    /**
     * 承运商信息
     */
    interface Carrier {
      id: number
      carrierNo: string
      name: string
      contactPerson: string
      contactPhone: string
      contactEmail?: string
      address: string
      status: PartnerStatus
      createdAt: string
      updatedAt: string
    }

    /**
     * 创建承运商参数
     */
    interface CreateParams {
      carrierNo: string
      name: string
      contactPerson: string
      contactPhone: string
      contactEmail?: string
      address: string
    }

    /**
     * 更新承运商参数
     */
    interface UpdateParams {
      name?: string
      contactPerson?: string
      contactPhone?: string
      contactEmail?: string
      address?: string
    }
  }

  // ==================== 用户模块 ====================

  namespace User {
    /**
     * 用户查询参数
     */
    interface ListParams extends PageParams {
      username?: string
      realName?: string
      status?: UserStatus
    }

    /**
     * 用户信息
     */
    interface User {
      id: number
      username: string
      realName: string
      email?: string
      phone?: string
      avatar?: string
      status: UserStatus
      roles: string[]
      createdAt: string
      updatedAt: string
    }

    /**
     * 创建用户参数
     */
    interface CreateParams {
      username: string
      realName: string
      email?: string
      phone?: string
      password: string
      roleIds: number[]
    }

    /**
     * 更新用户参数
     */
    interface UpdateParams {
      realName?: string
      email?: string
      phone?: string
      roleIds?: number[]
    }
  }

  // ==================== 角色模块 ====================

  namespace Role {
    /**
     * 角色查询参数
     */
    interface ListParams extends PageParams {
      roleCode?: string
      roleName?: string
      status?: string
    }

    /**
     * 角色信息
     */
    interface Role {
      id: number
      roleCode: string
      roleName: string
      description?: string
      permissions: string[]
      status: string
      createdAt: string
      updatedAt: string
    }

    /**
     * 创建角色参数
     */
    interface CreateParams {
      roleCode: string
      roleName: string
      description?: string
      permissionIds?: number[]
    }

    /**
     * 更新角色参数
     */
    interface UpdateParams {
      roleName?: string
      description?: string
    }

    /**
     * 分配权限参数
     */
    interface AssignPermissionsParams {
      permissionIds: number[]
    }
  }

  // ==================== 异常管理模块 ====================

  namespace Exception {
    /**
     * 异常查询参数
     */
    interface ListParams extends PageParams {
      exceptionNo?: string
      type?: string
      level?: ExceptionLevel
      status?: ExceptionStatus
      relatedType?: string
      relatedId?: number
    }

    /**
     * 异常记录
     */
    interface ExceptionRecord {
      id: number
      exceptionNo: string
      type: string
      level: ExceptionLevel
      status: ExceptionStatus
      title: string
      description: string
      relatedType: string
      relatedId: number
      reporterId: number
      reporterName: string
      handlerId?: number
      handlerName?: string
      reportTime: string
      handleTime?: string
      solution?: string
      createdAt: string
      updatedAt: string
    }

    /**
     * 创建异常参数
     */
    interface CreateParams {
      type: string
      level: ExceptionLevel
      title: string
      description: string
      relatedType: string
      relatedId: number
    }

    /**
     * 处理异常参数
     */
    interface HandleParams {
      solution: string
      remark?: string
    }

    /**
     * 异常统计
     */
    interface Statistics {
      totalCount: number
      pendingCount: number
      processingCount: number
      resolvedCount: number
    }
  }

  // ==================== 附件模块 ====================

  namespace Attachment {
    /**
     * 附件信息
     */
    interface Attachment {
      id: number
      fileName: string
      fileOriginalName: string
      fileSize: number
      fileType: string
      filePath: string
      url: string
      uploaderId: number
      uploaderName: string
      bizType?: string
      bizId?: number
      createdAt: string
    }

    /**
     * 上传参数
     */
    interface UploadParams {
      file: File
      bizType?: string
      bizId?: number
    }
  }
}

// ==================== 组件类型 ====================

export namespace Component {
  /**
   * 表格列配置
   */
  interface TableColumn {
    /** 列属性名 */
    prop?: string
    /** 列标题 */
    label?: string
    /** 列宽度 */
    width?: number
    /** 最小宽度 */
    minWidth?: number
    /** 是否固定列 */
    fixed?: boolean | 'left' | 'right'
    /** 是否使用插槽 */
    useSlot?: boolean
    /** 对齐方式 */
    align?: 'left' | 'center' | 'right'
  }

  /**
   * 表单项配置
   */
  interface FormItem {
    /** 字段名 */
    key: string
    /** 标签 */
    label?: string
    /** 类型 */
    type?: 'input' | 'select' | 'date' | 'number'
    /** 占比 */
    span?: number
    /** 选项 */
    options?: { label: string; value: any }[]
    /** 其他属性 */
    props?: Record<string, any>
  }
}
