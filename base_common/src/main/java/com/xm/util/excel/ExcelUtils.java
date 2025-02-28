package com.xm.util.excel;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.cell.values.ErrorCellValue;
import com.xm.advice.exception.exception.CommonException;
import com.xm.annotation.Comment;
import com.xm.core.params.ColumnProps;
import com.xm.util.common.CommonUtil;
import com.xm.util.excel.convert.CellToStrConvert;
import com.xm.util.excel.convert.ExportConvert;
import com.xm.util.excel.convert.ImportConvert;
import com.xm.util.excel.enums.CellConvertType;
import com.xm.util.excel.params.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFObjectData;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class ExcelUtils {

    private final static Map<String,byte[]> iconMapping=new HashMap<>();

    /**
     *  class需要带@Comment注解
     */
    public static List<ModelAndTitleExcelMapping>  getModelAndTitleMappingByClass(Class<?> clazz,
                                                                             Map<String, ImportConvert<?,?>> importConvertMap,
                                                                             Map<String, ExportConvert<?,?>> exportConvertMap){
        List<ModelAndTitleExcelMapping> mappingList=new ArrayList<>();
        Field[] fieldList = clazz.getDeclaredFields();
        for (Field field:fieldList) {
            //判断是否存在注解
            if (field.isAnnotationPresent(Comment.class)) {

                ModelAndTitleExcelMapping mapping=new ModelAndTitleExcelMapping();
                //获取注解里面的列名
                Comment comment=field.getDeclaredAnnotation(Comment.class);
                String commentValue=comment.value();
                mapping.setTitle(commentValue);
                mapping.setKey(field.getName());
                mapping.setField(field);

                mapping.setImportConvert(importConvertMap!=null?importConvertMap.get(field.getName()):null);
                mapping.setExportConvert(exportConvertMap!=null?exportConvertMap.get(field.getName()):null);

                mappingList.add(mapping);
            }
        }
        return mappingList;
    }

    /**
     * 设置单元格宽度
     */
    private static void setColumnWidth(Sheet sheet,int columnIndex,int width){
        sheet.setColumnWidth(columnIndex,width*256);
    }

    /**
     * 单元格赋值
     */
    private static void setColumnValue(int row, int column, Sheet sheet, Object value, CellStyle style){
        Row sheetRow=sheet.getRow(row);
        if (sheetRow==null){
            sheetRow=sheet.createRow(row);
        }
        Cell cell=sheetRow.getCell(column);
        if (cell==null){
            cell=sheetRow.createCell(column);
        }
        if (value instanceof String){
            String cellStrValue= (String) value;
            cell.setCellValue(cellStrValue);
        }else if (value instanceof Integer){
            Integer cellIntegerValue= (Integer) value;
            cell.setCellValue(cellIntegerValue);
        }else if (value instanceof BigDecimal){
            BigDecimal bigDecimal= (BigDecimal) value;
            cell.setCellValue(bigDecimal.toString());
        }else if (value instanceof Double){
            cell.setCellValue((Double)value);
        }

        if (style!=null){
            cell.setCellStyle(style);
        }

    }

    /**
     * 合并单元格
     */
    private static void mergedRegion(Sheet sheet,int startRow,int endRow,int startColumn,int endColumn,CellStyle style){
        //只有一个格子时不合并
        if (startRow==endRow&&startColumn==endColumn){
            return;
        }
        CellRangeAddress cellAddresses = new CellRangeAddress(startRow, endRow, startColumn, endColumn);
        try {
            //设置边框样式
            if (style!=null){
                for (int i=startRow;i<=endRow;i++){
                    for (int j=startColumn;j<=endColumn;j++){
                        Row sheetRow=sheet.getRow(i);
                        if (sheetRow==null){
                            sheetRow=sheet.createRow(i);
                        }
                        Cell cell=sheetRow.getCell(j);
                        if (cell==null){
                            cell=sheetRow.createCell(j);
                        }
                        cell.setCellStyle(style);
                    }
                }
            }
            sheet.addMergedRegion(cellAddresses);
        }catch (Exception e){
            String format = StrUtil.format("startRow->{},endRow->{},startColumn->{},startColumn->{},errorMsg->{}"
                    , startRow, endRow, startColumn, endColumn, ExceptionUtil.stacktraceToString(e));
            log.info(format);
            throw new CommonException(format);
        }
    }

    private static CellStyle createDefaultCellStyle(Workbook workbook){
        //居中样式
        CellStyle style = workbook.createCellStyle();
        BorderStyle defaultBorderStyle=BorderStyle.THIN;
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(defaultBorderStyle);
        style.setBorderTop(defaultBorderStyle);
        style.setBorderLeft(defaultBorderStyle);
        style.setBorderRight(defaultBorderStyle);
        Font font= workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        font.setFontName("宋体");
        style.setFont(font);
        //换行
        style.setWrapText(true);
        return style;
    }


    public static int getLevel(List<ModelAndTitleExcelMapping> mappingList) {
        if (mappingList == null) {
            return 0;
        }
        List<Integer> childrenLevelList=new ArrayList<>();
        for (ModelAndTitleExcelMapping item:mappingList){
            childrenLevelList.add(getLevel(item.getChildren()));
        }
        if (CollectionUtil.isEmpty(childrenLevelList)){
            return 1;
        }else {
            return 1+childrenLevelList.stream().max(Integer::compareTo).orElse(0);
        }
    }


    private static int createTitleRecursive(Sheet sheet, Map<String, ExcelIndex> indexMapping, CellStyle cellStyle,
                                            List<ModelAndTitleExcelMapping> mappingList, int startRow, int startColumn, int level){
        int currentColumn = startColumn;
        for (ModelAndTitleExcelMapping mapping:mappingList){
            if (CollectionUtil.isNotEmpty(mapping.getChildren())){
                //先处理子项
                int endColumn=createTitleRecursive(sheet,indexMapping,cellStyle,mapping.getChildren(), startRow +1,currentColumn,level-1);
                //再处理父项
                mergedRegion(sheet, startRow, startRow,currentColumn,endColumn-1,cellStyle);
                setColumnValue(startRow,currentColumn,sheet,mapping.getTitle(),cellStyle);
                currentColumn = endColumn;
            }else {
                //合并高度(处理带子项和非带子项)
                mergedRegion(sheet,startRow,startRow+level-1,currentColumn,currentColumn,cellStyle);

                setColumnValue(startRow,currentColumn,sheet,mapping.getTitle(),cellStyle);
                ExcelIndex excelIndex=new ExcelIndex();
                excelIndex.setIndex(currentColumn);
                excelIndex.setMapping(mapping);
                indexMapping.put(mapping.getKey(),excelIndex);
                currentColumn++;
            }
        }
        return currentColumn;
    }

    public static List<ModelAndTitleExcelMapping> convertMappingListRecursion(List<ColumnProps> columnPropsList){
        if (CollectionUtil.isEmpty(columnPropsList)){
            return null;
        }
        List<ModelAndTitleExcelMapping> mappingList=new ArrayList<>();
        for (ColumnProps columnProps:columnPropsList){
            //不展示
            Boolean show = columnProps.getShow();
            if (show!=null){
                if (!show){
                    continue;
                }
            }
            ModelAndTitleExcelMapping mapping=new ModelAndTitleExcelMapping();
            mapping.setKey(columnProps.getProp());
            mapping.setTitle(columnProps.getLabel());
            mapping.setType(columnProps.getType());
            mapping.setChildren(convertMappingListRecursion(columnProps.getChildren()));
            mappingList.add(mapping);
        }
        return mappingList;
    }

    public static Workbook exportDynamicDataWithMergeToExcel(DynamicExcelWithMerge dynamicExcelWithMerge) throws Exception {
        List<ColumnProps> mainColumns = dynamicExcelWithMerge.getMainColumns();
        List<Map<String, Object>> mainTableData = dynamicExcelWithMerge.getMainTableData();
        Map<String, RowColumnSpan> rowColumnSpanMap = dynamicExcelWithMerge.getRowColumnSpanMap();
        List<ModelAndTitleExcelMapping> mappingList = ExcelUtils.convertMappingListRecursion(mainColumns);
        int level = getLevel(mappingList);
        Workbook workbook = exportDataToExcelFileLevel(mappingList, mainTableData, new ExcelOtherInfo());
        Sheet sheetAt = workbook.getSheetAt(0);
        if (CollectionUtil.isNotEmpty(rowColumnSpanMap)){
            //合并单元格
            for (Map.Entry<String, RowColumnSpan> entry:rowColumnSpanMap.entrySet()){
                String key = entry.getKey();
                String[] split = key.split("_");
                int dataRowIndex=Integer.parseInt(split[0]);
                int dataColumnIndex=Integer.parseInt(split[1]);
                RowColumnSpan value = entry.getValue();
                if (value.getRowspan()==0&&value.getColspan()==0){
                    continue;
                }
                if (value.getRowspan()==1&&value.getColspan()==1){
                    continue;
                }
                int rowIndex=dataRowIndex + level;
                mergedRegion(sheetAt,rowIndex,rowIndex+(value.getRowspan()-1),dataColumnIndex,dataColumnIndex+(value.getColspan()-1),null);
            }
        }
        return workbook;
    }

    public static <T> Workbook exportDataToExcelFileLevel(List<ModelAndTitleExcelMapping> mappingList,
                                                          List<T> dataList,ExcelOtherInfo info) throws Exception {

        if (CollectionUtil.isEmpty(mappingList)){
            throw new CommonException("映射为空");
        }
        int level = getLevel(mappingList);
        if (level<=0){
            throw new CommonException("非法层数");
        }
        //校验SXSSFWorkbook临时文件目录是否存在
        String tmpdirPath=System.getProperty("java.io.tmpdir");
        if (StrUtil.isNotBlank(tmpdirPath)){
            String poifiles = tmpdirPath+"/poifiles";
            File tmpdir = new File(poifiles);
            if (!tmpdir.exists()){
                boolean mkdirs = tmpdir.mkdirs();
                if (!mkdirs){
                    throw new CommonException(StrUtil.format("创建临时目录失败->{}",poifiles));
                }
            }
        }
        SXSSFWorkbook workbook=new SXSSFWorkbook();
        SXSSFSheet sheet= workbook.createSheet("数据");
        CellStyle defaultCellStyle = createDefaultCellStyle(workbook);
        Map<String,ExcelIndex> indexMapping = new HashMap<>();
        //创建列标题
        createTitleRecursive(sheet,indexMapping,defaultCellStyle,mappingList,0,0,level);

        //设置列宽
        for (ModelAndTitleExcelMapping mapping:mappingList){
            Integer width = mapping.getWidth();
            if (width!=null){
                ExcelIndex excelIndex = indexMapping.get(mapping.getKey());
                if (excelIndex!=null){
                    setColumnWidth(sheet,excelIndex.getIndex(),width);
                }
            }
        }
        if (info==null){
            info=new ExcelOtherInfo();
        }
        Map<String, Map<String,List<ExcelFile>>> fileListMapping = info.getFileListMapping();
        if (fileListMapping==null){
            fileListMapping=new HashMap<>();
        }

        int rowIndex = level;
        int dataRowIndex=0;
        List<Field>  fieldsDirectly=null;
        for (T t:dataList){
            if (t instanceof Map){
                @SuppressWarnings("unchecked")
                Map<String,Object> map= (Map<String, Object>) t;
                for (Map.Entry<String,Object> entry:map.entrySet()){
                    ExcelIndex excelIndex = indexMapping.get(entry.getKey());
                    if (excelIndex==null){
                        continue;
                    }
                    Integer columnIndex = excelIndex.getIndex();
                    Object value = entry.getValue();
                    ModelAndTitleExcelMapping mapping = excelIndex.getMapping();
                    if (mapping.isFileColumn()){
                        Map<String,List<ExcelFile>> excelFileList = fileListMapping.get(entry.getKey());
                        putFile(workbook, sheet, rowIndex,dataRowIndex, columnIndex, excelFileList);
                    }else {
                        putCommonValue(sheet, defaultCellStyle, rowIndex, columnIndex, value, mapping);
                    }
                }
            }else {
                Class<?> tClass = t.getClass();
                if (CollectionUtil.isEmpty(fieldsDirectly)){
                    fieldsDirectly = Arrays.stream(ReflectUtil.getFieldsDirectly(tClass, true)).collect(Collectors.toList());
                }
                for (Field field:fieldsDirectly){
                    field.setAccessible(true);
                    ExcelIndex excelIndex = indexMapping.get(field.getName());
                    if (excelIndex==null){
                        continue;
                    }
                    Integer columnIndex = excelIndex.getIndex();
                    Object value = field.get(t);
                    ModelAndTitleExcelMapping mapping = excelIndex.getMapping();
                    if (mapping.isFileColumn()){
                        Map<String,List<ExcelFile>> excelFileList = fileListMapping.get(field.getName());
                        putFile(workbook, sheet, rowIndex,dataRowIndex, columnIndex, excelFileList);
                    }else {
                        putCommonValue(sheet, defaultCellStyle, rowIndex, columnIndex, value, mapping);
                    }
                }
            }
            rowIndex++;
            dataRowIndex++;
        }

        return workbook;
    }

    private static void putCommonValue(Sheet sheet, CellStyle defaultCellStyle, int rowIndex, Integer columnIndex, Object value, ModelAndTitleExcelMapping mapping) {
        ExportConvert<?,?> exportConvert = mapping.getExportConvert();
        if (exportConvert!=null){
            @SuppressWarnings("unchecked")
            ExportConvert<Object,Object> objectConvert= (ExportConvert<Object, Object>) exportConvert;
            value=objectConvert.convert(value,mapping);
        }
        if (value==null){
            setColumnValue(rowIndex,columnIndex,sheet," ",defaultCellStyle);
        }else {
            if (value instanceof Date){
                Date date= (Date) value;
                value=cn.hutool.core.date.DateUtil.format(date,"yyyy-MM-dd HH:mm:ss");
            }
            if (value instanceof LocalDateTime){
                value=LocalDateTimeUtil.format((LocalDateTime) value,"yyyy-MM-dd HH:mm:ss");
            }
            setColumnValue(rowIndex,columnIndex,sheet,value,defaultCellStyle);
        }
    }

    // 将 Icon 对象转换为 Image 对象
    private static Image iconToImage(Icon icon) {
        if (icon instanceof ImageIcon) {
            return ((ImageIcon) icon).getImage();
        } else {
            int width = icon.getIconWidth();
            int height = icon.getIconHeight();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = image.createGraphics();
            icon.paintIcon(null, graphics, 0, 0);
            graphics.dispose();
            return image;
        }
    }


    private static byte[] getSystemIcon(String fileName){
        String suffix = FileUtil.getSuffix(fileName);
        byte[] readBytes = iconMapping.get(suffix);
        if (readBytes!=null){
            return readBytes;
        }else {
            String prefix = FileUtil.getPrefix(fileName);
            File tempFile = FileUtil.createTempFile(prefix, "."+suffix, true);
            FileSystemView view = FileSystemView.getFileSystemView();
            Icon systemIcon = view.getSystemIcon(tempFile);
            Image image = iconToImage(systemIcon);
            readBytes = CommonUtil.imageToByteArray(image);
            iconMapping.put(suffix,readBytes);
            String absolutePath = tempFile.getAbsolutePath();
            if (tempFile.delete()){
                log.info("临时文件->{}删除成功",absolutePath);
            }
            return readBytes;
        }
    }

    private static void putFile(Workbook workbook, Sheet sheet, int rowIndex,int dataRowIndex, Integer columnIndex, Map<String,List<ExcelFile>> excelFileMapping) throws IOException {
        Row sheetRow=sheet.getRow(rowIndex);
        if (sheetRow==null){
            sheetRow=sheet.createRow(rowIndex);
        }
        Cell cell=sheetRow.getCell(columnIndex);
        if (cell==null){
            sheetRow.createCell(columnIndex);
        }
        if (CollectionUtil.isNotEmpty(excelFileMapping)){
            List<ExcelFile> excelFileList = excelFileMapping.get(String.valueOf(dataRowIndex));
            if (CollectionUtil.isEmpty(excelFileList)){
                return;
            }
            for (ExcelFile excelFile:excelFileList){
                byte[] readBytes = excelFile.getReadBytes();

                String fileName = excelFile.getName();
                if (StrUtil.isBlank(fileName)){
                    throw new CommonException("Excel添加附件失败,文件名为空");
                }

                /*
                  获取系统图标
                 */
                byte[] systemIcon = getSystemIcon(excelFile.getName());
                int iconId = workbook.addPicture(systemIcon, Workbook.PICTURE_TYPE_JPEG);
                /*
                 添加附件
                 readBytes: 表示文件的字节数组。
                 第二个参数: 表示 OLE 对象的类型。如果导出之后，在excel中操作另存文件，这个名称就是新保存文件的默认名称【文件标签】
                 第三个参数: 这指定了要在OLE Package中使用的类名。在此示例中，你可以将其设置为任何你想要的类名，因为它不会对后续的操作产生影响。它也只是用于标识特定的OLE Package【文件名】。
                 第四个参数: 这是在Excel工作簿中显示的OLE Package的名称,如果需要在excel中打开pdf文档，命名一定要以.pdf结尾，不然在excel中打不开！！！
                 */
                log.info("excel文件->{},大小->{}字节",fileName,readBytes.length);
                int fileIdx = workbook.addOlePackage(readBytes, fileName, fileName, fileName);
                // 创建画布和锚点
                Drawing<?> drawing = sheet.createDrawingPatriarch();
                //文件略缩图会占据整个单元格，锚点随单元格大小的改变而自动调整。这意味着当单元格的大小发生变化时，图片的大小也会相应地进行调整。
                //在后续应用业务的时候，可以给单元格固定的高度和宽度，让略缩图更加美观
                //参数1：起始列的偏移量（单位为字符宽度的 1/256）
                //参数2：起始行的偏移量（单位为字符高度的 1/256）
                //参数3：结束列的偏移量（单位为字符宽度的 1/256）
                //参数4：结束行的偏移量（单位为字符高度的 1/256）
                //参数5：起始列
                //参数6：起始行
                //参数7：结束列
                //参数8：结束行
                ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, columnIndex, rowIndex, columnIndex + 1, rowIndex + 1);

                drawing.createObjectData(anchor, fileIdx, iconId);
//                ObjectData objectData = drawing.createObjectData(anchor, fileIdx, iconId);
//                if (objectData instanceof XSSFObjectData){
//                    XSSFObjectData xssfObjectData= (XSSFObjectData) objectData;
//                }
            }
        }
    }

    public static <T> void exportDataToExcelFileSimple(OutputStream outputStream,List<ModelAndTitleExcelMapping> mappingList,List<T> dataList) throws Exception {
        if (CollectionUtil.isEmpty(dataList)){
            return;
        }
        XSSFWorkbook xssfWorkbook=new XSSFWorkbook();
        ExcelWriter excelWriter=new ExcelWriter(xssfWorkbook,"导出");
        List<Map<String,Object>> mapList=new ArrayList<>();
        for (T data:dataList){
            Map<String,Object> stringObjectMap=new LinkedHashMap<>();
            for (ModelAndTitleExcelMapping mapping:mappingList){
                Field field=mapping.getField();
                field.setAccessible(true);
                if (field.isAnnotationPresent(Comment.class)) {
                    //获取注解里面的列名
                    Comment comment=field.getDeclaredAnnotation(Comment.class);
                    String commentValue=comment.value();
                    ExportConvert<?,?> exportConvert=mapping.getExportConvert();
                    Object value = field.get(data);
                    if (exportConvert!=null){
                        @SuppressWarnings("unchecked")
                        ExportConvert<Object,Object> objectConvert= (ExportConvert<Object, Object>) exportConvert;
                        stringObjectMap.put(commentValue,objectConvert.convert(value,mapping));
                    }else {
                        stringObjectMap.put(commentValue,value);
                    }
                }
            }
            mapList.add(stringObjectMap);
        }


        excelWriter.write(mapList);
//        //自动列宽
//        excelWriter.autoSizeColumnAll();
        excelWriter.getWorkbook().write(outputStream);
        // 关闭writer，释放内存
        excelWriter.close();
    }



    private static void getDeepTitleMappingRecursion(String parentTitle,List<ModelAndTitleExcelMapping> mappingList,Map<String,ModelAndTitleExcelMapping> deepTitleMapping){
        for (ModelAndTitleExcelMapping mapping:mappingList){
            if (CollectionUtil.isEmpty(mapping.getChildren())){
                if (StrUtil.isBlank(parentTitle)){
                    deepTitleMapping.put(mapping.getTitle(),mapping);
                }else {
                    deepTitleMapping.put(parentTitle+"_"+mapping.getTitle(),mapping);
                }
            }else {
                getDeepTitleMappingRecursion(mapping.getTitle(),mapping.getChildren(),deepTitleMapping);
            }
        }
    }


    //获取定位
    public static Map<Integer,ModelAndTitleExcelMapping> getTitleIndexMapping(Sheet sheet, List<ModelAndTitleExcelMapping> mappingList){
        Map<Integer,ModelAndTitleExcelMapping> titleExcelMappingMap=new HashMap<>();
        int level = getLevel(mappingList);
        //获取列名对应
        Map<String,ModelAndTitleExcelMapping> deepTitleMapping=new HashMap<>();
        getDeepTitleMappingRecursion("",mappingList,deepTitleMapping);
        //处理映射
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        Map<Integer,Map<Integer,String>> levelTitleMapping=new HashMap<>();
        for (int row=0;row<level;row++){
            //只取第row行的列合并
            int finalRow = row;
            List<CellRangeAddress> cellRangeAddressList = mergedRegions.stream()
                    .filter(item -> item.getFirstRow() == item.getLastRow())
                    .filter(item -> item.getFirstRow() == finalRow)
                    .collect(Collectors.toList());
            //处理非合并项
            if (row==0){
                Map<Integer, Integer> ignoreCol = cellRangeAddressList
                        .stream()
                        .flatMap(item -> Arrays.stream(NumberUtil.range(item.getFirstColumn(), item.getLastColumn())).boxed())
                        .collect(Collectors.toMap(Function.identity(), Function.identity()));
                Row sheetRow = sheet.getRow(row);
                for (int col=0;col<=sheetRow.getLastCellNum();col++){
                    Integer integer = ignoreCol.get(col);
                    if (integer==null){
                        Cell cell = sheet.getRow(row).getCell(col);
                        String cellStrValue = getCellStrValue(cell, null, null);
                        ModelAndTitleExcelMapping mapping = deepTitleMapping.get(cellStrValue.trim());
                        if (mapping!=null){
                            titleExcelMappingMap.put(col,mapping);
                        }
                    }
                }
            }
            //处理合并项
            for (CellRangeAddress cellAddresses:cellRangeAddressList){
                //合并项取值只取第一列
                Cell cell = sheet.getRow(row).getCell(cellAddresses.getFirstColumn());
                String cellStrValue = getCellStrValue(cell, null, null);

                for (int col=cellAddresses.getFirstColumn();col<=cellAddresses.getLastColumn();col++){
                    Map<Integer, String> stringMap = levelTitleMapping.get(row);
                    if (CollectionUtil.isEmpty(stringMap)){
                        stringMap=new HashMap<>();
                        levelTitleMapping.put(row,stringMap);
                    }
                    if (row==0){
                        stringMap.put(col,cellStrValue);
                    }else {
                        //拼接上层标题
                        Map<Integer, String> pre = levelTitleMapping.get(row - 1);
                        String preCellStrValue = pre.get(col);
                        cellStrValue=preCellStrValue+"_"+cellStrValue;
                        stringMap.put(col,cellStrValue);
                    }
                }
            }
            //合并项最后一层
            if (row!=0&&row==level-1){
                Row sheetRow = sheet.getRow(row);
                for (int col=0;col<=sheetRow.getLastCellNum();col++){
                    Cell cell = sheet.getRow(row).getCell(col);
                    String cellStrValue = getCellStrValue(cell, null, null);

                    //获取上层名字然后拼接
                    Map<Integer, String> pre = levelTitleMapping.get(row - 1);
                    String preCellStrValue = pre.get(col);
                    if (preCellStrValue!=null){
                        cellStrValue=preCellStrValue+"_"+cellStrValue;
                        ModelAndTitleExcelMapping mapping = deepTitleMapping.get(cellStrValue.trim());
                        if (mapping!=null){
                            titleExcelMappingMap.put(col,mapping);
                        }
                    }
                }
            }
        }

        return titleExcelMappingMap;
    }

    public static List<Map<String,Object>> readExcelData(InputStream inputStream,Integer sheetIndex,List<ModelAndTitleExcelMapping> mappingList){
        Workbook workbook = null;
        List<Map<String,Object>> resultList=new ArrayList<>();
        try {
            workbook=new XSSFWorkbook(inputStream);
            Sheet sheetAt = workbook.getSheetAt(sheetIndex);
            int level = getLevel(mappingList);
            Map<Integer, ModelAndTitleExcelMapping> titleIndexMapping = getTitleIndexMapping(sheetAt, mappingList);
            //从数据列开始读取
            FormulaEvaluator formulaEvaluator=workbook.getCreationHelper().createFormulaEvaluator();
            for (int rowIndex=level;rowIndex<=sheetAt.getLastRowNum();rowIndex++){
                Row row = sheetAt.getRow(rowIndex);
                Map<String,Object> result=new HashMap<>();
                for (int colIndex=0;colIndex<=row.getLastCellNum();colIndex++){
                    ModelAndTitleExcelMapping mapping = titleIndexMapping.get(colIndex);
                    if (mapping==null){
                        continue;
                    }
                    Cell cell = row.getCell(colIndex);
                    Object cellValue = getCellValue(cell, formulaEvaluator);
                    String key = mapping.getKey();
                    String[] splitKeyList = key.split("\\.");
                    if (splitKeyList.length == 1) {
                        result.put(mapping.getKey(),cellValue);
                    }else {
                        Map<String,Object> parentMap=result;
                        String parentKey = splitKeyList[0];
                        for (int i=1;i<splitKeyList.length;i++){
                            String childrenKey=splitKeyList[i];
                            Map<String,Object> childrenMap;
                            Object parentObj = parentMap.get(parentKey);
                            if (parentObj==null){
                                childrenMap=new HashMap<>();
                            }else {
                                childrenMap= BeanUtil.beanToMap(parentObj);
                            }
                            childrenMap.put(childrenKey,cellValue);
                            parentMap.put(parentKey,childrenMap);
                            //循环下次
                            parentMap=childrenMap;
                            parentKey=childrenKey;
                        }
                    }
                }
                resultList.add(result);
            }
            return resultList;
        }catch (Exception e){
            log.error("读取excel异常",e);
            String msg=StrUtil.format("读取excel异常->{}", ExceptionUtil.stacktraceToString(e));
            throw new CommonException(msg);
        }finally {
            try {
                if (workbook!=null){
                    workbook.close();
                }
            }catch (Exception e){
                log.error("关闭excel文件流失败");
            }
        }
    }

    /**
     * 从excel表格获取数据
     */
    public static <T> List<T> getDataByExcelFile(InputStream inputStream,Integer sheetIndex,List<ModelAndTitleExcelMapping> mappingList,Class<T> clazz) throws Exception {
        List<Map<String, Object>> readAll = readExcelData(inputStream, sheetIndex, mappingList);
        List<T> list=new ArrayList<>();
        //读取excel为空则直接返回
        if (CollectionUtil.isEmpty(readAll)){
            return list;
        }
        if (CollectionUtil.isEmpty(mappingList)){
            return list;
        }
        for (Map<String,Object> map:readAll){
            T newInstance=clazz.newInstance();
            Map<String,Object> nestedObjMap=new HashMap<>();
            for (ModelAndTitleExcelMapping mapping:mappingList){
                String key = mapping.getKey();
                String[] splitKeyList = key.split("\\.");
                if (splitKeyList.length!=1){
                    String parentKey = splitKeyList[0];
                    Object parentValue = map.get(parentKey);
                    if (parentValue==null){
                        continue;
                    }
                    Field parentField = ReflectUtil.getField(newInstance.getClass(), parentKey);
                    if (parentField==null){
                        continue;
                    }
                    Object parentObj = nestedObjMap.get(parentKey);
                    if (parentObj==null){
                        parentObj = parentField.getType().newInstance();
                        ReflectUtil.setFieldValue(newInstance,parentKey,parentObj);
                        nestedObjMap.put(parentKey,parentObj);
                    }
                    if (!(parentValue instanceof Map)){
                        continue;
                    }
                    Map<String, Object> parentMap = BeanUtil.beanToMap(parentValue);

                    for (int i=1;i<splitKeyList.length;i++){
                        String childrenKey = splitKeyList[i];
                        //判断子属性是否存在
                        Field childrenField = ReflectUtil.getField(parentObj.getClass(), childrenKey);
                        if (childrenField==null){
                            break;
                        }
                        //嵌套属性设置值
                        if (i==splitKeyList.length-1){
                            ImportConvert<?,?> importConvert=mapping.getImportConvert();
                            Object value = parentMap.get(childrenKey);
                            if (importConvert!=null){
                                @SuppressWarnings("unchecked")
                                ImportConvert<Object,Object> objectConvert= (ImportConvert<Object, Object>) importConvert;
                                Object convertObject=objectConvert.convert(value,mapping);
                                ReflectUtil.setFieldValue(parentObj,childrenKey,convertObject);
                            }else {
                                ReflectUtil.setFieldValue(parentObj,childrenKey,value);
                            }
                        }else {
                            //获取子map属性值
                            Object childrenValue = parentMap.get(childrenKey);
                            if (!(childrenValue instanceof Map)){
                                break;
                            }
                            parentMap = BeanUtil.beanToMap(childrenValue);
                            //创建子对象
                            Object childrenObj = ReflectUtil.getFieldValue(parentObj, childrenKey);
                            if (childrenObj==null){
                                childrenObj = childrenField.getType().newInstance();
                                ReflectUtil.setFieldValue(parentObj,childrenKey,childrenObj);
                            }
                            parentObj=childrenObj;
                        }
                    }
                }else {
                    Object value = map.get(key);
                    if (value==null){
                        continue;
                    }
                    ImportConvert<?,?> importConvert=mapping.getImportConvert();
                    if (importConvert!=null){
                        @SuppressWarnings("unchecked")
                        ImportConvert<Object,Object> objectConvert= (ImportConvert<Object, Object>) importConvert;
                        Object convertObject=objectConvert.convert(value,mapping);
                        ReflectUtil.setFieldValue(newInstance,key,convertObject);
                    }else {
                        ReflectUtil.setFieldValue(newInstance,key,value);
                    }
                }
            }
            list.add(newInstance);
        }
        return list;
    }

    public static MultipartFile validFile(Map<String, MultipartFile> fileMap) {
        if (CollectionUtil.isEmpty(fileMap)) {
            throw new CommonException("上传文件为空");
        }
        if (fileMap.size() > 1) {
            throw new CommonException("只接受单个文件导入");
        }

        Set<String> fileKeySet = fileMap.keySet();// 获取所有的key集合

        Iterator<String> keyIterator = fileKeySet.iterator();
        MultipartFile file = null;
        while (keyIterator.hasNext()) {
            file = fileMap.get(keyIterator.next());
        }

        if (file!=null){
            if (StrUtil.isNotBlank(file.getOriginalFilename())){
                if (!file.getOriginalFilename().endsWith(".xlsx")) {
                    throw new CommonException("导入文件的格式不正确，目前只支持xlsx");
                }
            }
        }

        return file;
    }


    /**
     * 从1开始
     */
    private static int getLetterPosition(char letter){
        return letter-'A'+1;
    }


    public static Object getCellValue(Cell cell, FormulaEvaluator formulaEvaluator){
        if (cell==null){
            return null;
        }
        //日期
        if (CellType.NUMERIC.equals(cell.getCellType())&& DateUtil.isCellDateFormatted(cell)){
            return cell.getDateCellValue();
        }
        //字符
        if (CellType.STRING.equals(cell.getCellType())){
            return cell.getStringCellValue();
        }
        //数字
        if (CellType.NUMERIC.equals(cell.getCellType())){
            double numericCellValue=cell.getNumericCellValue();
            return new BigDecimal(numericCellValue);
        }
        //公式
        if (CellType.FORMULA.equals(cell.getCellType())){
            if (formulaEvaluator==null){
                return "";
            }
            String formula=cell.getCellFormula();
            cell=formulaEvaluator.evaluateInCell(cell);
            switch(cell.getCellType()) {
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)){
                        return cell.getDateCellValue();
                    }else {
                        double numericCellValue=cell.getNumericCellValue();
                        return new BigDecimal(numericCellValue);
                    }
                case STRING:
                    RichTextString value=cell.getRichStringCellValue();
                    return value==null?null:value.getString();
                case ERROR:
                    ErrorCellValue errorCellValue=new ErrorCellValue(cell);
                    log.error(StrUtil.format("公式->{}解析错误->{}",formula, errorCellValue.getValue()));
                    return null;
                default:
                    log.error(StrUtil.format("不支持的类型->{}",cell.getCellType().name()));
                    return null;
            }
        }
        return null;
    }

    public static String getCellStrValue(Cell cell, Map<CellConvertType,CellToStrConvert<?>> cellToStrConvertMap, FormulaEvaluator formulaEvaluator){
        if (cell==null){
            return "";
        }
        String cellStrVale = "";
        //日期
        if (CellType.NUMERIC.equals(cell.getCellType())&& DateUtil.isCellDateFormatted(cell)){
            Date date=cell.getDateCellValue();
            if (CollectionUtil.isEmpty(cellToStrConvertMap)){
                cellStrVale=cn.hutool.core.date.DateUtil.format(date,"yyyy-MM-dd");
            }else {
                @SuppressWarnings("unchecked")
                CellToStrConvert<Date> dateConvert= (CellToStrConvert<Date>) cellToStrConvertMap.get(CellConvertType.DATE);
                cellStrVale=dateConvert.convert(date);
            }
        }
        //字符
        if (CellType.STRING.equals(cell.getCellType())){
            String str=cell.getStringCellValue();
            cellStrVale=str==null?"":str;
        }
        //数字
        if (CellType.NUMERIC.equals(cell.getCellType())){
            double numericCellValue=cell.getNumericCellValue();
            if (CollectionUtil.isEmpty(cellToStrConvertMap)){
                cellStrVale=String.valueOf(numericCellValue);
            }else {
                @SuppressWarnings("unchecked")
                CellToStrConvert<Double> convert = (CellToStrConvert<Double>) cellToStrConvertMap.get(CellConvertType.NUMERIC);
                cellStrVale=convert.convert(numericCellValue);
            }
        }
        //公式
        if (CellType.FORMULA.equals(cell.getCellType())){
            if (formulaEvaluator==null){
                return "";
            }
            String formula=cell.getCellFormula();
            cell=formulaEvaluator.evaluateInCell(cell);
            switch(cell.getCellType()) {
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)){
                        Date date=cell.getDateCellValue();
                        if (CollectionUtil.isEmpty(cellToStrConvertMap)){
                            cellStrVale=cn.hutool.core.date.DateUtil.format(date,"yyyy-MM-dd");
                        }else {
                            @SuppressWarnings("unchecked")
                            CellToStrConvert<Date> dateConvert= (CellToStrConvert<Date>) cellToStrConvertMap.get(CellConvertType.DATE);
                            cellStrVale=dateConvert.convert(date);
                        }
                    }else {
                        double numericCellValue=cell.getNumericCellValue();
                        if (CollectionUtil.isEmpty(cellToStrConvertMap)){
                            cellStrVale=String.valueOf(numericCellValue);
                        }else {
                            @SuppressWarnings("unchecked")
                            CellToStrConvert<Double> convert = (CellToStrConvert<Double>) cellToStrConvertMap.get(CellConvertType.NUMERIC);
                            cellStrVale=convert.convert(numericCellValue);
                        }
                    }
                    break;
                case STRING:
                    RichTextString value=cell.getRichStringCellValue();
                    cellStrVale=value==null?"":value.getString();
                    break;
                case ERROR:
                    ErrorCellValue errorCellValue=new ErrorCellValue(cell);
                    throw new CommonException(StrUtil.format("公式->{}解析错误->{}",formula, errorCellValue.getValue()));
                default:
                    throw new CommonException(StrUtil.format("不支持的类型->{}",cell.getCellType().name()));
            }
        }
        return cellStrVale;
    }

    /**
     * 根据字母，获取位置（即26进制转10进制）
     * @return 从0开始算起
     */
    public static int changeStrToIntPosition(String strPosition){
        if (strPosition==null){
            throw new CommonException("excel字符位置格式不正确");
        }
        int intPosition;
        if (strPosition.length()==1){
            intPosition=getLetterPosition(strPosition.charAt(0));
        }else {
            int base = 1;
            int sum=0;
            for (int j = strPosition.length()-1; j >= 0; j--) {
                sum=sum+getLetterPosition(strPosition.charAt(j))*base;
                base=base*26;
            }
            intPosition = sum;
        }
        //从开始算起
        intPosition--;
        return intPosition;
    }

    /**
     * 导出excel流到HTTP响应流
     */
    public static void exportExcel(Workbook workbook, HttpServletResponse response){
        OutputStream outputStream = null;
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode("导出", "UTF-8") + ".xlsx");
            outputStream=response.getOutputStream();
            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
        }catch (Exception e){
            String msg=StrUtil.format("导出excel失败->{}", ExceptionUtil.stacktraceToString(e));
            log.error(msg);
            throw new CommonException(msg);
        }finally {
            try {
                if (workbook!=null){
                    workbook.close();
                }
                if (outputStream!=null){
                    outputStream.close();
                }
            }catch (Exception e){
                log.error("导出excel后，关闭流失败",e);
            }
        }
    }


    public static void main(String[] args) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            XSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("Embedded File Test");

            String content = "<html><head></head><body><h1>Test Embedded HTML File</h1></body></html>";
            String objectName = "test_embedded";
            String fileExtension = ".html";
            int rowNo = 1;
            int colNo = 1;

//            ByteArrayOutputStream firefoxIcon = new ByteArrayOutputStream();
            //https://design.firefox.com/photon/visuals/product-identity-assets.html
//            URL iconURL = new URL("https://d33wubrfki0l68.cloudfront.net/06185f059f69055733688518b798a0feb4c7f160/9f07a/images/product-identity-assets/firefox.png");
//            Thread.currentThread().getContextClassLoader().getResourceAsStream("firefox.png").transferTo(excelIcon);
//            iconURL.openStream().transferTo(firefoxIcon);
            InputStream stream = ResourceUtil.getStream("img.png");
            final int iconId = wb.addPicture(IoUtil.readBytes(stream), XSSFWorkbook.PICTURE_TYPE_PNG);

            final int oleIdx = wb.addOlePackage(content.getBytes(), objectName, objectName + fileExtension, objectName + fileExtension);
            final Drawing<?> pat = sheet.createDrawingPatriarch();
            final ClientAnchor anchor = pat.createAnchor(0, 0, 0, 0, colNo, rowNo, colNo + 1, rowNo + 2);//tweak cell ranges to minimize image distortion
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);

            final XSSFObjectData objectData = (XSSFObjectData) pat.createObjectData(anchor, oleIdx, iconId);
            objectData.getCTShape().getNvSpPr().getCNvPr().setName(objectName);
            objectData.getCTShape().getNvSpPr().getCNvPr().setHidden(false);
            //objectData.getOleObject().setDvAspect(STDvAspect.DVASPECT_ICON);

            wb.write(Files.newOutputStream(Paths.get("F:\\embedded-file-test.xlsx")));
        } catch (Exception e) {
            log.error("错误",e);
        }
    }
}
