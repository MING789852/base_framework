interface BusinessProjectItemConfig {
  id: string,
  name:string,
  fieldKey:string,
  fieldType: string,
  canQuery: boolean
  executor: string
}
interface BusinessProjectConfig {
  id: string,
  name: string,
  code: string,
  executor: string,
  disabled: boolean,
  businessProjectItemList: Array<BusinessProjectItemConfig>
}

interface ReviewData{
  [key:string]:any,
  failReason?: string,
  status?: string,
  createUser?: string,
  createDate?: string,
  dynamicField: Record<string, any>
}
