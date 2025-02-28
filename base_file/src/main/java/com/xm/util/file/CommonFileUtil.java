package com.xm.util.file;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.xm.file.domain.entity.TcFile;
import com.xm.util.id.SnowIdUtil;

import java.util.Date;

public class CommonFileUtil {

    public static TcFile getSaveFileEntity(String fileName, Long size){
        String prefix= FileUtil.getPrefix(fileName);
        String suffix=FileUtil.getSuffix(fileName);
        String id= SnowIdUtil.getSnowId();

        TcFile tcFile=new TcFile();
        tcFile.setId(id);
        tcFile.setOriginalFileName(fileName);
        tcFile.setFileName(prefix);
        tcFile.setExtName(suffix);

        Date now=new Date();
        int year= DateUtil.year(now);
        int month=DateUtil.month(now)+1;
        int date=DateUtil.dayOfMonth(now);

        StringBuilder reNameFile=new StringBuilder();
        //后缀不为空则拼接后缀
        if (StrUtil.isNotBlank(suffix)){
            reNameFile.append(id).append(".").append(suffix);
        }else {
            reNameFile.append(id);
        }
        //路径=年/月/日/后缀名/xxx.xxx
        tcFile.setPath(StrUtil.format("{}/{}/{}/{}/{}", Integer.toString(year), Integer.toString(month), Integer.toString(date),suffix,reNameFile.toString()));
//        tcFile.setPath(Paths.get(year.toString(),month.toString(),date.toString(),suffix,reNameFile.toString()).toString());
        if (size!=null){
            tcFile.setFileSize(size);
        }
        tcFile.setCreateDate(now);
        tcFile.setCreateUser("system");
        return tcFile;
    }
}
