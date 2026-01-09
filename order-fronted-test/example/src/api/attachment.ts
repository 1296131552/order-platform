/**
 * 附件相关 API
 */

import request from '@/utils/request'

/** 附件查询参数 */
export interface AttachmentQueryParams {
  page?: number
  pageSize?: number
  category?: string
  relatedType?: string
  relatedId?: number
  fileName?: string
  startDate?: string
  endDate?: string
}

/** 附件信息 */
export interface Attachment {
  id: number
  fileName: string
  fileOriginalName: string
  fileSize: number
  fileType: string
  category: string
  relatedType?: string
  relatedId?: number
  filePath: string
  url: string
  uploaderId: number
  uploaderName: string
  remark?: string
  createTime: string
  updateTime: string
}

/**
 * 分页查询附件列表
 */
export function getAttachmentList(params: AttachmentQueryParams) {
  return request.get<{ list: Attachment[]; total: number }>('/attachment/list', { params })
}

/**
 * 获取附件详情
 */
export function getAttachmentDetail(id: number) {
  return request.get<Attachment>(`/attachment/${id}`)
}

/**
 * 上传文件
 */
export function uploadFile(file: File, onProgress?: (percent: number) => void) {
  const formData = new FormData()
  formData.append('file', file)

  return request.post<{ filePath: string; url: string; fileName: string }>('/attachment/upload', formData, {
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
 * 批量上传文件
 */
export function uploadFiles(files: File[], onProgress?: (percent: number) => void) {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })

  return request.post<Array<{ filePath: string; url: string; fileName: string }>>('/attachment/upload/batch', formData, {
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
 * 关联附件到业务对象
 */
export function linkAttachment(data: {
  attachmentId: number
  relatedType: string
  relatedId: number
}) {
  return request.post('/attachment/link', data)
}

/**
 * 取消关联附件
 */
export function unlinkAttachment(data: {
  attachmentId: number
  relatedType: string
  relatedId: number
}) {
  return request.post('/attachment/unlink', data)
}

/**
 * 删除附件
 */
export function deleteAttachment(id: number) {
  return request.delete(`/attachment/${id}`)
}

/**
 * 下载附件
 */
export function downloadAttachment(id: number) {
  return request.get(`/attachment/${id}/download`, { responseType: 'blob' })
}

/**
 * 获取附件分类列表
 */
export function getAttachmentCategories() {
  return request.get<Array<{ value: string; label: string }>>('/attachment/categories')
}
