import {isNullOrUnDef} from "@pureadmin/utils";
import {FileSystemAccessShowOpenFileOptions, FileSystemFileHandle} from "@vueuse/core";

// 辅助函数：将accept字符串转换为showOpenFilePicker需要的格式
// 辅助函数：转换accept字符串为showOpenFilePicker需要的格式
function parseAcceptTypes(accept: string): Record<string, string[]> {
    if (accept.includes('/*')) {
        // 处理类似 'image/*' 的MIME类型
        return { [accept]: getExtensionFromMimeType(accept) };
    } else if (accept.startsWith('.')) {
        // 处理类似 '.pdf,.docx' 的扩展名列表
        const exts = accept.split(',').map(e => e.trim());
        const mimeType = getMimeTypeFromExtension(exts[0]) || 'application/octet-stream';
        return { [mimeType]: exts };
    } else {
        return { ['application/octet-stream']: ['*'] };
    }
}

function getExtensionFromMimeType(mimeType: string): string[] | undefined {
    const mapping: Record<string, string[]> = {
        'image/*': ['.jpg', '.jpeg', '.png', '.gif']
    };
    return mapping[mimeType];
}

// 简单MIME类型映射
function getMimeTypeFromExtension(ext: string): string | undefined {
    const mapping: Record<string, string> = {
        '.jpg': 'image/jpeg',
        '.jpeg': 'image/jpeg',
        '.png': 'image/png',
        '.gif': 'image/gif',
        '.pdf': 'application/pdf',
        '.doc': 'application/msword',
        '.docx': 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    };
    return mapping[ext.toLowerCase()];
}

// 创建模拟Event对象以保持接口一致性
function createMockEvent(files: File[]): Event {
    const event = new Event('change');
    Object.defineProperty(event, 'target', {
        value: {
            files: files
        },
        writable: false
    });
    return event;
}

async function uploadFile(callBack: (e: Event) => void, multiple: boolean, accept?: string) {
    // 现代API封装
    const useModernAPI = 'showOpenFilePicker' in window;
    if (useModernAPI) {
        try {
            const options:FileSystemAccessShowOpenFileOptions = {
                multiple,
                types: accept ? [
                    {
                        description: 'Allowed Files',
                        accept: parseAcceptTypes(accept)
                    }
                ] : undefined,
                excludeAcceptAllOption: false
            };
            const fileHandles:Promise<FileSystemFileHandle[]> =  (window as any).showOpenFilePicker(options);
            fileHandles.then(fileHandles=>{
                Promise.all(fileHandles.map((fileHandle)=>fileHandle.getFile())).then( files=>{
                    // 现代API回调传递File数组
                    callBack(createMockEvent(files));
                })
            })
        } catch (error) {
            if (error.name !== 'AbortError') {
                console.error('File picker error:', error);
            }
        }
    } else {
        let inputObj = document.createElement('input')
        inputObj.type = 'file'
        if (!isNullOrUnDef(accept)) {
            inputObj.accept = accept
        }
        inputObj.multiple = multiple
        inputObj.onchange = (e: Event) => {
            callBack(e)
            //清理dom
            inputObj.remove()
        }
        inputObj.click()
    }
}

export  default {
    uploadFile
}