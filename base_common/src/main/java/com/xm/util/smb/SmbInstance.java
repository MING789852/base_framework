package com.xm.util.smb;


import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.xm.advice.exception.exception.CommonException;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@ToString
public class SmbInstance {
    private final String host;
    private final String username;
    private final String password;
    private final SMBClient client;
    private final AuthenticationContext ac;

    private Session session;
    private Connection connection;


    public SmbInstance(String host, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.client = new SMBClient();
        this.ac = new AuthenticationContext(this.username, this.password.toCharArray(), null);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (this.connection!=null){
            this.connection.close();
        }
        this.client.close();
    }

    private void login(){
        try {
            if (session==null||!session.getConnection().isConnected()){
                log.info("开始登录SMB->{}",this.host);
                connection=client.connect(this.host);
                session=connection.authenticate(ac);
                log.info("SMB登录成功->{}",this.host);
            }else {
                log.info("已登录SMB->{}",this.host);
            }
        }catch (Exception e){
            String msg= StrUtil.format("SMB登录失败->{}",this.toString());
            log.error(msg,e);
            throw new CommonException(msg);
        }
    }


    public boolean delete(String shareName,String remoteUrl,String fileName){
        login();
        String filePath=remoteUrl+fileName;
        try (DiskShare share = (DiskShare) session.connectShare(shareName)) {
            if (share.fileExists(filePath)){
                share.rm(filePath);
            }
            return true;
        }catch (Exception e){
            String msg= StrUtil.format("SMB删除文件失败->{}",filePath);
            log.error(msg,e);
            throw new CommonException(msg);
        }finally {
            log.info("SMB删除文件成功->{}",filePath);
        }
    }


    public List<SmbFile> list(String shareName,String remoteUrl){
        login();
        try (DiskShare share = (DiskShare) session.connectShare(shareName)) {
            List<FileIdBothDirectoryInformation> list = share.list(remoteUrl);
            List<SmbFile> smbFileList=new ArrayList<>();
            for (FileIdBothDirectoryInformation file : list) {
                SmbFile smbFile=new SmbFile();
                String path = remoteUrl;
                if (file.getFileAttributes()==FileAttributes.FILE_ATTRIBUTE_DIRECTORY.getValue()){
                    smbFile.setDir(true);
                    if ("..".equals(file.getFileName())){
                        Path parent = Paths.get(path).getParent();
                        if (parent!=null){
                            path=parent.toString();
                            path=path.replaceAll("\\\\","/");
                        }
                    }
                }else {
                    smbFile.setDir(false);
                }
                smbFile.setFileName(file.getFileName());
                smbFile.setPath(path);
                smbFile.setFileSize(file.getEndOfFile());
                smbFile.setShareName(shareName);
                smbFileList.add(smbFile);
            }
            return smbFileList;
        }catch (Exception e){
            String msg= StrUtil.format("SMB文件列展示失败->{}",remoteUrl);
            log.error(msg,e);
            throw new CommonException(msg);
        }finally {
            log.info("SMB文件列展示成功->{}",remoteUrl);
        }
    }


    public void downloadInputStream(String shareName, String remoteUrl, String fileName, Consumer<InputStream> inputStreamConsumer) {
        login();
        String filePath;
        if (!remoteUrl.endsWith("/")){
            filePath=remoteUrl+"/"+fileName;
        }else {
            filePath=remoteUrl+fileName;
        }
        try (DiskShare share = (DiskShare) session.connectShare(shareName)) {
            File shareFile = share.openFile(filePath, EnumSet.of(AccessMask.GENERIC_READ),
                    EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL), SMB2ShareAccess.ALL,
                    SMB2CreateDisposition.FILE_OPEN_IF, EnumSet.noneOf(SMB2CreateOptions.class));
            inputStreamConsumer.accept(shareFile.getInputStream());
        }catch (Exception e){
            String msg= StrUtil.format("SMB下载文件失败->{}",filePath);
            log.error(msg,e);
            throw new CommonException(msg);
        }finally {
            log.info("SMB下载文件成功->{}",filePath);
        }
    }

    public void upload(String shareName,String remoteUrl,String fileName,InputStream inputStream) {
        login();
        String filePath=remoteUrl+fileName;
        try (DiskShare share = (DiskShare) session.connectShare(shareName)) {
            if (remoteUrl.contains("/") || remoteUrl.contains("\\")) {
                remoteUrl = remoteUrl.replaceAll("\\\\", "/");
                String[] filepathArr = remoteUrl.split("/");
                String vfilepath = "";
                for (String s : filepathArr) {
                    vfilepath += s + "/";
                    if (!share.folderExists(vfilepath)) {
                        share.mkdir(vfilepath);
                    }
                }
            }
            // 获取文件流.上传文件.
            File shareFile = share.openFile(remoteUrl+fileName, EnumSet.of(AccessMask.GENERIC_WRITE),
                    EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL), SMB2ShareAccess.ALL,
                    SMB2CreateDisposition.FILE_OPEN_IF, EnumSet.noneOf(SMB2CreateOptions.class));
            try (OutputStream shareOs = shareFile.getOutputStream()) {
                shareOs.write(IoUtil.readBytes(inputStream));
            }
        } catch (Exception e) {
            String msg= StrUtil.format("SMB上传文件失败->{}",filePath);
            log.error(msg,e);
            throw new CommonException(msg);
        }finally {
            log.info("SMB上传文件成功->{}",filePath);
        }
    }
}
