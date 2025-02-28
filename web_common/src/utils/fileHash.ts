//hash.js
import SparkMD5 from 'spark-md5'
import { ElMessage } from 'element-plus'

// 生成文件 hash
// 返回 {percentage:number,md5:string}
self.onmessage = e => {
    let chunkList:Blob[] = e.data.chunkList
    let chunkNum = chunkList.length
    let currentChunkIndex = 0 //当前读到第几个分片，最开始为0
    let spark = new SparkMD5.ArrayBuffer();
    let fileReader = new FileReader();
    let loadNext = () => {
        fileReader.readAsArrayBuffer(chunkList[currentChunkIndex])
    }
    loadNext()
    fileReader.onload = (e) => {  //读取成功后调用
        spark.append(e.target.result)
        currentChunkIndex++
        if (currentChunkIndex < chunkNum) { //继续读取分片
            let percent = Math.floor(currentChunkIndex / chunkNum * 100)
            self.postMessage({
                percentage: percent,
            });
            loadNext()
        } else {
            let md5 = spark.end()
            spark.destroy()
            self.postMessage({
                md5,
                percentage: 100
            });
            loadNext = null
            self.close(); // 关闭 worker 线程，线程如果不关闭，则会一直在后台运行着，
        }
    }
    fileReader.onerror = (e) => {  //读取出错调用
        console.log(e);
        self.close();
        ElMessage.error('读取文件出错')
        loadNext = null
    }
};
