declare interface JumpResultPageParam{
    autoClose?: boolean,
    duration?: number,
    statusType?:'success'|'warn'|'danger'|'info'|'primary',
    statusText?:string,
    titleText?:string,
    contentText?:string
}