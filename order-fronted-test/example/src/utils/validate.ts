/**
 * 验证工具函数
 */

/**
 * 验证手机号
 */
export function isPhone(value: string): boolean {
  return /^1[3-9]\d{9}$/.test(value)
}

/**
 * 验证邮箱
 */
export function isEmail(value: string): boolean {
  return /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)
}

/**
 * 验证身份证号
 */
export function isIdCard(value: string): boolean {
  return /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/.test(value)
}

/**
 * 验证URL
 */
export function isUrl(value: string): boolean {
  return /^https?:\/\//.test(value)
}

/**
 * 验证数字
 */
export function isNumber(value: string): boolean {
  return /^\d+$/.test(value)
}

/**
 * 验证金额（保留两位小数）
 */
export function isAmount(value: string): boolean {
  return /^\d+(\.\d{1,2})?$/.test(value)
}
