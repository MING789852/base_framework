package com.xm.util.file;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.configuration.file.FileProperty;
import com.xm.file.domain.entity.TcFile;
import com.xm.file.enums.FileStatusEnum;
import com.xm.file.mapper.TcFileMapper;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.id.SnowIdUtil;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class CommonFileUtil {

    public static FileProperty fileProperty;
    public static TcFileMapper tcFileMapper;

    static {
        fileProperty = SpringBeanUtil.getBeanByClass(FileProperty.class);
        tcFileMapper = SpringBeanUtil.getBeanByClass(TcFileMapper.class);
    }

    public static String getUploadUserNickName(){
        TcUser currentLoginUserBySessionOrToken = UserInfoUtil.getCurrentLoginUserBySessionOrToken();
        if (currentLoginUserBySessionOrToken==null){
            return "system";
        }else {
            return currentLoginUserBySessionOrToken.getNickName();
        }
    }

    public static TcFile copyMD5FileRef(TcFile file) {
        if (file==null){
            return null;
        }
        String md5 = file.getMd5();
        if (StrUtil.isBlank(md5)){
            throw new CommonException("md5码为空，无法复制引用");
        }
        TcFile copy=new TcFile();
        BeanUtils.copyProperties(file,copy);
        copy.setStatus(FileStatusEnum.AVAILABLE.getValue());
        copy.setId(SnowIdUtil.getSnowId());
        copy.setCreateDate(new Date());
        copy.setUpdateUser(getUploadUserNickName());
        copy.setUpdateUser("MD5 COPY");
        return copy;
    }

    public static TcFile getFileByMd5(String md5,TcFile temp) {
        if (StrUtil.isBlank(md5)){
            throw new CommonException("文件MD5码不能为空");
        }
        if (fileProperty.getAllowRepeatUpload()){
            return null;
        }else {
            LambdaQueryWrapper<TcFile> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper
                    .eq(TcFile::getMd5, md5);
            List<TcFile> fileList = tcFileMapper.selectList(lambdaQueryWrapper);
            if (CollectionUtil.isEmpty(fileList)){
                return null;
            }else {
                TcFile alreadyFile = fileList.get(0);
                //处理md5码冲突
                //先判断文件大小是否相同
                if (!Objects.equals(temp.getFileSize(), alreadyFile.getFileSize())){
                    return null;
                }
                //判断文件后缀是否相同
                if (!Objects.equals(temp.getExtName(), alreadyFile.getExtName())){
                    return null;
                }
                TcFile copyMd5 = copyMD5FileRef(alreadyFile);
                //如果传入参数非空，修改文件名
                copyMd5.setFileName(temp.getFileName());
                copyMd5.setOriginalFileName(temp.getOriginalFileName());
                copyMd5.setExtName(temp.getExtName());
                tcFileMapper.insert(copyMd5);
                return copyMd5;
            }
        }
    }

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
        tcFile.setCreateUser(getUploadUserNickName());
        return tcFile;
    }
}
