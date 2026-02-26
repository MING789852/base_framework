package com.xm.util.file;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public class FileValidUtil {

    //文件名不包含以下任何字符  \\ / : * ? " < > |
    @Getter
    private final   static List<String> illegalCharList= Arrays.asList("\\", "/", ":", "*", "?", "\"", "<", ">", "|");
    //Windows系统中保留名称：CON、PRN、AUX、NUL、COM1、COM2、COM3、COM4、COM5、COM6、COM7、COM8、COM9、LPT1、LPT2、LPT3、LPT4、LPT5、LPT6、LPT7、LPT8 、 LPT9
    @Getter
    private final  static List<String> reservedNameList=Arrays
            .asList("CON", "PRN", "AUX", "NUL", "COM1", "COM2",
                    "COM3", "COM4", "COM5", "COM6", "COM7", "COM8",
                    "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5",
                    "LPT6", "LPT7", "LPT8", "LPT9");


    //是否包含非法字符
    public static boolean fileNameHaveIllegalChar(String fileName){
        if (StrUtil.isBlank(fileName)){
            return false;
        }
        return illegalCharList.stream().anyMatch(fileName::contains);
    }

    //是否包含Windows系统中保留名称
    public static boolean fileNameHaveWindowReservedName(String fileName){
        if (StrUtil.isBlank(fileName)){
            return false;
        }
        return reservedNameList.stream().anyMatch(fileName::equalsIgnoreCase);
    }

    /**
     * 处理文件名：移除非法字符，处理保留名称
     * @param fileName 原始文件名
     * @return 处理后的安全文件名
     */
    public static String processFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "default_filename";
        }

        // 处理一：替换非法字符为空字符串
        String processedName = removeIllegalCharacters(fileName);

        // 处理二：检查并处理保留名称
        processedName = handleReservedNames(processedName);

        return processedName;
    }


    /**
     * 移除文件名中的非法字符
     */
    private static String removeIllegalCharacters(String fileName) {
        String result = fileName;

        // 遍历所有非法字符，逐一替换
        for (String illegalChar : illegalCharList) {
            result = result.replace(illegalChar, "");
        }

        return result;
    }

    /**
     * 处理Windows保留名称
     */
    private static String handleReservedNames(String fileName) {
        // 获取文件名（不含扩展名）进行保留名称检查
        String nameWithoutExt = fileName;
        String extension = "";

        // 分离文件名和扩展名
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            nameWithoutExt = fileName.substring(0, lastDotIndex);
            extension = fileName.substring(lastDotIndex);
        }

        // 检查是否为保留名称（不区分大小写）
        if (reservedNameList.contains(nameWithoutExt.toUpperCase())) {
            // 如果是保留名称，在前面加上"SMB"
            return "SMB" + nameWithoutExt + extension;
        }

        return fileName;
    }
}
