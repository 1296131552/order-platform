-- ============================================================
-- 附件表 (t_attachment)
-- 说明:附件主表,记录附件信息,通过多态关联支持关联多种业务实体
-- 关系:Attachment N:1 (通过business_type + business_id多态关联业务实体)
-- 关系:Attachment N:M AttachmentTag (通过attachment_tag_relation中间表关联)
-- ============================================================

CREATE TABLE `t_attachment` (
  -- ========== 主键 ==========
  -- 附件ID,主键,自增。每条附件记录的唯一标识
  -- 注意:目前使用自增ID,若使用分布式架构应改为雪花ID
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '附件ID',

  -- ========== 多态关联业务实体 ==========
  -- 【多态关联设计】通过business_type + business_id实现多态关联,支持关联多种业务实体
  -- 业务类型,标识该附件关联的业务实体类型
  -- 可选值:order(订单)、order_line(订单行)、shipment(发运计划)、shipment_line(快递单)、supplier(供应商)、carrier(承运商)、exception(异常记录)等
  `business_type` VARCHAR(50) NOT NULL COMMENT '业务类型:order/order_line/shipment/shipment_line/supplier/carrier/exception等',
  -- 业务ID,关联业务实体的主键ID
  -- 示例:business_type='order', business_id=1001 表示附件关联订单ID=1001(Order表的id字段)
  -- 示例:business_type='supplier', business_id=2001 表示附件关联供应商ID=2001(Supplier表的id字段)
  -- 示例:business_type='exception', business_id=3001 表示附件关联异常记录ID=3001(Exception表的id字段)
  `business_id` BIGINT NOT NULL COMMENT '业务ID:关联业务实体的主键ID',

  -- ========== 业务实体冗余信息(便于查询和展示) ==========
  -- 业务实体编号(冗余字段,便于查询和展示)
  -- 示例:订单号、发运计划号、供应商编码、承运商编码等
  `business_no` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '业务实体编号(冗余字段)',
  -- 业务实体名称(冗余字段,便于展示)
  -- 示例:订单标题、供应商名称、承运商名称等
  `business_name` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '业务实体名称(冗余字段)',

  -- ========== 文件基本信息 ==========
  -- 文件名称,原始文件名
  `file_name` VARCHAR(255) NOT NULL COMMENT '文件名称',
  -- 文件原始名称,上传时的原始文件名(可能包含路径信息)
  `original_file_name` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '文件原始名称',
  -- 文件扩展名,如:pdf、jpg、png、docx等
  `file_extension` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '文件扩展名',
  -- 文件类型/MIME类型,如:application/pdf、image/jpeg、image/png等
  `file_type` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '文件类型/MIME类型',
  -- 文件大小(字节),文件的实际大小
  `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
  -- 文件大小(格式化显示),如:1.5MB、500KB等,便于前端展示
  `file_size_display` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '文件大小(格式化显示)',
  -- 文件存储路径,文件在存储系统中的完整路径(OSS、本地文件系统等)
  -- 注意:文件实际存储在OSS或本地文件系统,数据库只存储路径
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件存储路径',
  -- 文件存储类型,标识文件存储位置。示例:OSS(阿里云OSS)、LOCAL(本地文件系统)、S3(Amazon S3)等
  `storage_type` VARCHAR(50) NOT NULL DEFAULT 'LOCAL' COMMENT '文件存储类型:OSS/LOCAL/S3等',
  -- 文件访问URL,文件的访问地址(如果支持直接URL访问)
  -- 使用NULL表示无访问URL,必须区分"无URL"和"已设置但URL为空"的业务含义,因此允许使用NULL
  `file_url` VARCHAR(500) NULL DEFAULT NULL COMMENT '文件访问URL:NULL表示无访问URL',
  -- 文件MD5值,用于文件去重和完整性校验
  -- 默认空字符串表示未计算MD5,避免使用NULL防止索引失效和简化查询逻辑
  `file_md5` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '文件MD5值:默认空字符串表示未计算MD5',
  -- 文件SHA256值,用于文件完整性校验(可选,比MD5更安全)
  -- 默认空字符串表示未计算SHA256,避免使用NULL防止索引失效和简化查询逻辑
  `file_sha256` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '文件SHA256值:默认空字符串表示未计算SHA256',

  -- ========== 附件分类信息 ==========
  -- 附件类型,用于附件分类。示例:CONTRACT(合同)、QUOTATION(报价)、VOUCHER(凭证)、PHOTO(照片)、QUALIFICATION(资质)、INVOICE(发票)等
  -- 默认空字符串表示未设置附件类型,避免使用NULL防止索引失效和简化查询逻辑
  `attachment_type` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '附件类型',
  -- 附件分类,用于附件分组。示例:ORDER(订单相关)、SHIPMENT(发运相关)、RECEIPT(签收相关)、PARTNER(合作方相关)等
  -- 默认空字符串表示未设置附件分类,避免使用NULL防止索引失效和简化查询逻辑
  `attachment_category` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '附件分类',
  -- 附件用途描述,说明附件的用途和内容
  `attachment_description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '附件用途描述',

  -- ========== 附件状态信息 ==========
  -- 附件状态:UPLOADING(上传中)、UPLOADED(已上传)、PROCESSING(处理中)、AVAILABLE(可用)、FAILED(上传失败)、DELETED(已删除)
  -- 默认UPLOADING表示上传中,避免使用NULL防止索引失效和简化查询逻辑
  `attachment_status` VARCHAR(50) NOT NULL DEFAULT 'UPLOADING' COMMENT '附件状态:默认UPLOADING表示上传中',
  -- 是否公开:0-不公开(仅内部可见),1-公开(可对外分享)
  -- 默认0表示不公开,避免使用NULL防止索引失效和简化查询逻辑
  `is_public` TINYINT NOT NULL DEFAULT 0 COMMENT '是否公开:0-不公开,1-公开',
  -- 是否有效:0-无效(已删除或损坏),1-有效
  -- 默认1表示有效,避免使用NULL防止索引失效和简化查询逻辑
  `is_valid` TINYINT NOT NULL DEFAULT 1 COMMENT '是否有效:0-无效,1-有效',

  -- ========== 上传信息 ==========
  -- 上传时间(精确到毫秒),记录附件上传的时间
  `upload_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '上传时间',
  -- 上传人ID,外键关联t_user表。记录上传附件的用户。-1表示系统自动上传,0及以上表示用户ID
  -- 默认-1表示系统上传,避免使用NULL防止空指针问题和简化查询逻辑
  `upload_by` BIGINT NOT NULL DEFAULT -1 COMMENT '上传人ID:默认-1表示系统上传',
  -- 上传人姓名(冗余字段,便于查询和展示)
  `upload_by_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '上传人姓名(冗余字段)',
  -- 上传IP地址,记录上传时的IP地址,便于审计
  `upload_ip` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '上传IP地址',
  -- 上传来源,记录上传来源。示例:WEB(网页上传)、MOBILE(移动端上传)、API(接口上传)、SYSTEM(系统自动)等
  `upload_source` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '上传来源',

  -- ========== 下载统计信息 ==========
  -- 下载次数,记录附件被下载的次数
  `download_count` INT NOT NULL DEFAULT 0 COMMENT '下载次数',
  -- 最后下载时间(精确到毫秒),记录附件最后一次被下载的时间
  -- 使用NULL表示从未被下载,必须区分"未下载"和"已下载但时间为空"的业务含义,因此允许使用NULL
  `last_download_time` DATETIME(3) NULL DEFAULT NULL COMMENT '最后下载时间:NULL表示从未被下载',
  -- 最后下载人ID,外键关联t_user表。记录最后一次下载附件的用户。-1表示系统下载,0及以上表示用户ID
  -- 默认-1表示未下载,避免使用NULL防止空指针问题和简化查询逻辑
  `last_download_by` BIGINT NOT NULL DEFAULT -1 COMMENT '最后下载人ID:默认-1表示未下载',

  -- ========== 备注信息 ==========
  -- 附件备注
  `remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '附件备注',
  -- 内部备注(不对外展示)
  `internal_remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '内部备注',

  -- ========== 扩展信息 ==========
  -- 扩展信息,JSON格式存储扩展字段,便于扩展且无需修改表结构
  -- 注意:仅支持MySQL 5.7+版本
  -- 使用NULL表示无扩展信息,避免空JSON对象占用存储空间,且NULL可以明确区分"无扩展信息"和"空扩展信息"
  `extra_info` JSON DEFAULT NULL COMMENT '扩展信息:NULL表示无扩展信息',

  -- ========== 公共字段 ==========
  -- 创建时间(精确到毫秒),记录附件创建的时间
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  -- 创建人ID,记录附件的创建人,外键关联t_user表。-1表示系统自动操作,0及以上表示用户ID
  -- 默认-1表示系统创建,避免使用NULL防止空指针问题和简化查询逻辑
  `created_by` BIGINT NOT NULL DEFAULT -1 COMMENT '创建人ID:默认-1表示系统创建',
  -- 更新时间(精确到毫秒),记录附件信息最后更新的时间
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  -- 更新人ID,记录附件信息的最后更新人,外键关联t_user表。-1表示系统自动操作,0及以上表示用户ID
  -- 默认-1表示系统更新,避免使用NULL防止空指针问题和简化查询逻辑
  `updated_by` BIGINT NOT NULL DEFAULT -1 COMMENT '更新人ID:默认-1表示系统更新',
  -- 是否删除:0-未删除,1-已删除
  -- 默认0表示未删除,避免使用NULL防止索引失效和简化查询逻辑
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除,1-已删除',

  PRIMARY KEY (`id`),

  -- ========== 外键约束 ==========
  -- 注意:多态关联无法设置外键约束,业务层需保证business_id的有效性
  -- 注意:外键约束可选择性启用,取决于业务需求。启用外键可保证数据一致性,但可能影响性能

  -- ========== 约束设计 ==========

  -- ========== 索引设计 ==========
  -- 索引设计原则:只保留核心查询场景的索引,避免过度索引导致写入性能下降
  -- InnoDB联合索引遵循最左前缀原则,单字段索引被联合索引覆盖时需删除以避免冗余

  -- 业务类型+业务ID+软删除联合索引,用于查询业务实体的所有附件(高频场景:查询订单的所有附件)
  -- 覆盖场景:按业务类型+业务ID查询附件、按业务类型+业务ID+删除标记查询附件
  KEY `idx_attachment_business` (`business_type`, `business_id`, `is_deleted`) COMMENT '业务实体关联索引',

  -- 附件类型+附件状态+软删除联合索引,用于按类型和状态查询附件(高频场景:查询特定类型的可用附件)
  -- 覆盖场景:按附件类型查询、按附件类型+附件状态查询
  KEY `idx_attachment_type_status` (`attachment_type`, `attachment_status`, `is_deleted`) COMMENT '附件类型状态联合索引',

  -- 上传人ID+上传时间+软删除联合索引,用于查询上传人的附件(统计场景:查询用户上传的附件)
  KEY `idx_attachment_upload_time` (`upload_by`, `upload_time`, `is_deleted`) COMMENT '上传人时间索引',

  -- 创建时间+附件状态+软删除联合索引,用于时间排序和统计查询(覆盖创建时间单字段查询)
  KEY `idx_attachment_created_status` (`created_at`, `attachment_status`, `is_deleted`) COMMENT '创建时间状态联合索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='附件表:附件主表,记录附件信息,通过多态关联支持关联多种业务实体。'
  '【多态关联设计】本表通过business_type + business_id实现多态关联,支持关联订单、订单行、发运计划、快递单、供应商、承运商、异常记录等多种业务实体。'
  'business_type字段标识关联的业务实体类型,business_id字段存储关联业务实体的主键ID。'
  '【文件存储】文件实际存储在OSS或本地文件系统,数据库只存储文件元数据(文件名、大小、路径等)。'
  '【附件标签】附件与附件标签通过attachment_tag_relation中间表实现N:M关联,支持一个附件多个标签。'
  '【业务实体冗余信息】business_no、business_name字段作为冗余字段,便于查询和展示,避免频繁关联查询业务实体表。';

