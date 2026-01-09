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
  }

  // ==================== 订单模块 ====================

  namespace Order {
    /**
     * 订单查询参数
     */
    interface ListParams extends PageParams {
      /** 订单号 */
      orderNo?: string
      /** 客户名称 */
      customerName?: string
      /** 订单状态 */
      statusCode?: string
      /** 开始日期 */
      startDate?: string
      /** 结束日期 */
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
    }
  }

  // ==================== 发运模块 ====================

  namespace Shipment {
    interface ListParams extends PageParams {
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
    }
  }

  // ==================== 签收模块 ====================

  namespace Receipt {
    interface ListParams extends PageParams {
      shipmentNo?: string
      statusCode?: string
    }

    interface Receipt {
      id: number
      shipmentLineId: number
      trackingNo: string
      receivedQuantity: number
      expectedQuantity: number
      differenceQuantity: number
      receiptDate: string
    }
  }

  // ==================== 用户模块 ====================

  namespace User {
    interface LoginParams {
      username: string
      password: string
    }

    interface UserInfo {
      id: number
      username: string
      realName: string
      email: string
      phone: string
      avatar?: string
    }

    interface LoginResult {
      token: string
      userInfo: UserInfo
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
