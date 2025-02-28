import type {PureHttpResponse} from "@/utils/http/types";

class HttpInterceptor {
    public static instance: HttpInterceptor;

    private  httpResponseInterceptorFn:Map<string,(response: PureHttpResponse)=>void> = new Map<string,(response: PureHttpResponse)=>void>();

    public static getInstance() {
        if (HttpInterceptor.instance === undefined || HttpInterceptor.instance === null) {
            HttpInterceptor.instance = new HttpInterceptor()
        }
        return HttpInterceptor.instance
    }

    public executeHttpResponseInterceptor(response: PureHttpResponse){
        if (this.httpResponseInterceptorFn.size !== 0) {
            for (const fn of this.httpResponseInterceptorFn.values()){
                fn(response)
            }
        }
    }

    public addHttpResponseInterceptorFn(interceptorName:string,interceptor:(response: PureHttpResponse)=>void){
        this.httpResponseInterceptorFn.set(interceptorName,interceptor)
    }
}

const instance = HttpInterceptor.getInstance()
export default instance