/**
 * 附件相关 API
 * Requirements: 12.1, 12.2, 12.3, 12.4, 12.5
 */

import request from '@/utils/request'

/** 附件信息 */
export interface Attachment {
  id: number
  fileName: string
  fileOriginalName: string
  fileSize: number
  fileType: string
  filePath: string
  url: string
  uploaderId: number
  uploaderName: string
  createdAt: string
}

/** 上传响应 */
export interface UploadResult {
  filePath: string
  url: string
  fileName: string
}

/**
 * 上传单个附件
 * POST /api/attachment/upload
 * Requirements: 12.1
 */
export function uploadAttachment(file: File, onProgress?: (percent: number) => void) {
  const formData = new FormData()
  formData.append('file', file)

  return request.post<UploadResult>('/attachment/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percent)
      }
    }
  })
}

/**
 * 批量上传附件
 * POST /api/attachment/batch-upload
 * Requirements: 12.2
 */
export function batchUploadAttachment(files: File[], onProgress?: (percent: number) => void) {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })

  return request.post<UploadResult[]>('/attachment/batch-upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percent)
      }
    }
  })
}

/**
 * 下载附件
 * GET /api/attachment/download/{id}
 * Requirements: 12.3
 */
export function downloadAttachment(id: number) {
  return request.get(`/attachment/download/${id}`, { responseType: 'blob' })
}

/**
 * 删除附件
 * DELETE /api/attachment/{id}
 * Requirements: 12.4
 */
export function deleteAttachment(id: number) {
  return request.delete(`/attachment/${id}`)
}

/**
 * 查询业务对象的附件列表
 * GET /api/attachment/list/{bizId}
 * Requirements: 12.5
 */
export function getAttachmentListByBizId(bizId: number | string) {
  return request.get<Attachment[]>(`/attachment/list/${bizId}`)
}
