declare interface TagStyle  {
  color?:string,
  backgroundColor?:string
}

declare interface DictTag  {
  // 类型
  type?: 'primary' | 'success' | 'info' | 'warning' | 'danger',
  // 样式
  style?: TagStyle,
}
