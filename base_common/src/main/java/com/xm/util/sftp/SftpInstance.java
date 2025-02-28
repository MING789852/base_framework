package com.xm.util.sftp;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.xm.advice.exception.exception.CommonException;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.session.SessionHeartbeatController;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.apache.sshd.sftp.client.fs.SftpFileSystem;
import org.apache.sshd.sftp.client.fs.SftpPath;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SftpInstance {
    private final String password;
    private final String username;
    private final String host;
    private final int port;

    private final Semaphore systemSize=new Semaphore(5);

    private ClientSession clientSession;

    public SftpInstance(String host, int port,String username,String password) {
        this.password = password;
        this.username = username;
        this.host = host;
        this.port = port;
    }

    public boolean upload(List<SftpUploadData> sftpUploadDataList){
        if (CollectionUtil.isEmpty(sftpUploadDataList)){
            return true;
        }
        SftpFileSystem sftpFileSystem=getSftpFileSystem();
        try {
            SftpPath defaultDir = sftpFileSystem.getDefaultDir();
            for (SftpUploadData data:sftpUploadDataList){
                String uploadDir= data.getUploadDir();
                String uploadFile= data.getUploadFile();
                InputStream inputStream=data.getInputStream();
                SftpPath resolveUploadDir = defaultDir.resolve(uploadDir);
                if (!Files.exists(resolveUploadDir)){
                    Files.createDirectories(resolveUploadDir);
                }
                SftpPath sftpPath = resolveUploadDir.resolve(uploadFile);
                Files.copy(inputStream,sftpPath, StandardCopyOption.REPLACE_EXISTING);
                log.info("文件->{},上传成功",sftpPath.toAbsolutePath());
            }
            return true;
        } catch (IOException e) {
            log.error("sftpFileSystem状态->{},上传文件失败->{}",sftpFileSystem.isOpen(), ExceptionUtil.stacktraceToString(e));
            throw new CommonException("上传文件失败");
        }finally {
            returnSftpFileSystem(sftpFileSystem);
        }
    }

    public boolean delete(List<String> pathList,boolean failContinue){
        if (CollectionUtil.isEmpty(pathList)){
            return true;
        }
        SftpFileSystem sftpFileSystem=getSftpFileSystem();
        try {
            SftpPath defaultDir = sftpFileSystem.getDefaultDir();
            for (String path:pathList){
                SftpPath sftpPath = defaultDir.resolve(path);
                try {
                    Files.delete(sftpPath);
                    log.info("文件->{},删除成功",sftpPath.toAbsolutePath());
                }catch (IOException e){
                    log.error("sftpFileSystem状态->{},删除文件失败->{}",sftpFileSystem.isOpen(), ExceptionUtil.stacktraceToString(e));
                    if (!failContinue){
                        throw new CommonException("删除文件失败");
                    }
                }
            }
            return true;
        } finally {
            returnSftpFileSystem(sftpFileSystem);
        }
    }

    private SftpFileSystem getSftpFileSystem(){
        try {
            systemSize.acquire();
            return createSftpFileSystem();
        } catch (InterruptedException e) {
            systemSize.release();
            throw new RuntimeException(e);
        }
    }

    private void returnSftpFileSystem(SftpFileSystem sftpFileSystem){
        try {
            sftpFileSystem.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            systemSize.release();
        }
    }

    private SftpFileSystem createSftpFileSystem(){
        ClientSession session = getSession();
        try{
            return SftpClientFactory.instance().createSftpFileSystem(session);
        } catch (IOException e) {
            log.info("session是否已打开->{}",session.isOpen());
            log.error("创建SftpFileSystem失败",e);
            throw new CommonException("创建SftpFileSystem失败");
        }
    }

    private ClientSession createSession(){
        SshClient client = SshClient.setUpDefaultClient();
        if (!client.isStarted()){
            client.start();
        }
        try {
            ClientSession session = client.connect(this.username, this.host, this.port).verify(60, TimeUnit.SECONDS).getClientSession();
            session.addPasswordIdentity(this.password);
            AuthFuture verify = session.auth().verify(60, TimeUnit.SECONDS);
            if (verify.isSuccess()){
                session.setSessionHeartbeat(SessionHeartbeatController.HeartbeatType.IGNORE,Duration.ofSeconds(15));
                return session;
            }else {
                throw new CommonException("创建session失败,登录失败");
            }
        } catch (IOException e) {
            throw new CommonException("创建session失败,登录失败");
        }
    }

    private synchronized ClientSession getSession(){
        if (this.clientSession==null){
            this.clientSession=createSession();
        }else {
            if (!this.clientSession.isOpen()){
                this.clientSession=createSession();
            }
        }
        return this.clientSession;
    }
}
