package com.xm.util.excel;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.cell.values.ErrorCellValue;
import com.xm.advice.exception.exception.CommonException;
import com.xm.core.params.ColumnProps;
import com.xm.util.common.CommonUtil;
import com.xm.util.excel.convert.CellToStrConvert;
import com.xm.util.excel.convert.ExportConvert;
import com.xm.util.excel.convert.ImportConvert;
import com.xm.util.excel.enums.CellConvertType;
import com.xm.util.excel.params.*;
import com.xm.util.excel.params.img.ExcelImgIndexParams;
import com.xm.util.excel.params.img.ExcelImgIndexResult;
import com.xm.util.excel.params.img.ExcelImgIndexInterface;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class ExcelUtils {

    private final static Map<String, byte[]> iconMapping = new HashMap<>();

    public final static String sheetDefaultName = "数据";


    /**
     * 设置单元格宽度
     */
    private static void setColumnWidth(Sheet sheet, int columnIndex, int width) {
        sheet.setColumnWidth(columnIndex, width * 256);
    }

    /**
     * 单元格赋值
     */
    private static void setColumnValue(int row, int column, Sheet sheet, Object value, CellStyle style) {
        Row sheetRow = sheet.getRow(row);
        if (sheetRow == null) {
            sheetRow = sheet.createRow(row);
        }
        Cell cell = sheetRow.getCell(column);
        if (cell == null) {
            cell = sheetRow.createCell(column);
        }
        if (value instanceof String) {
            String cellStrValue = (String) value;
            cell.setCellValue(cellStrValue);
        } else if (value instanceof Integer) {
            Integer cellIntegerValue = (Integer) value;
            cell.setCellValue(cellIntegerValue);
        } else if (value instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal) value;
            cell.setCellValue(bigDecimal.doubleValue());
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        }

        if (style != null) {
            cell.setCellStyle(style);
        }

    }

    /**
     * 合并单元格
     */
    private static void mergedRegion(Sheet sheet, int startRow, int endRow, int startColumn, int endColumn, CellStyle style) {
        //只有一个格子时不合并
        if (startRow == endRow && startColumn == endColumn) {
            return;
        }
        CellRangeAddress cellAddresses = new CellRangeAddress(startRow, endRow, startColumn, endColumn);
        try {
            //设置边框样式
            for (int i = startRow; i <= endRow; i++) {
                for (int j = startColumn; j <= endColumn; j++) {
                    Row sheetRow = sheet.getRow(i);
                    if (sheetRow == null) {
                        sheetRow = sheet.createRow(i);
                    }
                    Cell cell = sheetRow.getCell(j);
                    if (cell == null) {
                        cell = sheetRow.createCell(j);
                    }
                    if (style != null) {
                        cell.setCellStyle(style);
                    }
                }
            }
            sheet.addMergedRegion(cellAddresses);
        } catch (Exception e) {
            String format = StrUtil.format("startRow->{},endRow->{},startColumn->{},startColumn->{},errorMsg->{}"
                    , startRow, endRow, startColumn, endColumn, ExceptionUtil.stacktraceToString(e));
            log.info(format);
            throw new CommonException(format);
        }
    }

    private static CellStyle createDefaultCellStyle(Workbook workbook) {
        //居中样式
        CellStyle style = workbook.createCellStyle();
        BorderStyle defaultBorderStyle = BorderStyle.THIN;
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(defaultBorderStyle);
        style.setBorderTop(defaultBorderStyle);
        style.setBorderLeft(defaultBorderStyle);
        style.setBorderRight(defaultBorderStyle);
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        font.setFontName("宋体");
        style.setFont(font);
        //换行
        style.setWrapText(true);
        return style;
    }


    private static int getLevel(List<ModelAndTitleExcelMapping> mappingList) {
        if (mappingList == null) {
            return 0;
        }
        List<Integer> childrenLevelList = new ArrayList<>();
        for (ModelAndTitleExcelMapping item : mappingList) {
            childrenLevelList.add(getLevel(item.getChildren()));
        }
        if (CollectionUtil.isEmpty(childrenLevelList)) {
            return 1;
        } else {
            return 1 + childrenLevelList.stream().max(Integer::compareTo).orElse(0);
        }
    }


    private static int createTitleRecursive(Sheet sheet, Map<String, ExcelIndex> indexMapping, CellStyle cellStyle,
                                            List<ModelAndTitleExcelMapping> mappingList, int startRow, int startColumn, int level) {
        int currentColumn = startColumn;
        for (ModelAndTitleExcelMapping mapping : mappingList) {
            if (CollectionUtil.isNotEmpty(mapping.getChildren())) {
                //先处理子项
                int endColumn = createTitleRecursive(sheet, indexMapping, cellStyle, mapping.getChildren(), startRow + 1, currentColumn, level - 1);
                //再处理父项
                mergedRegion(sheet, startRow, startRow, currentColumn, endColumn - 1, cellStyle);
                setColumnValue(startRow, currentColumn, sheet, mapping.getTitle(), cellStyle);
                currentColumn = endColumn;
            } else {
                //合并高度(处理带子项和非带子项)
                mergedRegion(sheet, startRow, startRow + level - 1, currentColumn, currentColumn, cellStyle);

                setColumnValue(startRow, currentColumn, sheet, mapping.getTitle(), cellStyle);
                ExcelIndex excelIndex = new ExcelIndex();
                excelIndex.setIndex(currentColumn);
                excelIndex.setMapping(mapping);
                indexMapping.put(mapping.getKey(), excelIndex);
                currentColumn++;
            }
        }
        return currentColumn;
    }

    public static List<ModelAndTitleExcelMapping> convertMappingListRecursion(List<ColumnProps> columnPropsList) {
        if (CollectionUtil.isEmpty(columnPropsList)) {
            return new ArrayList<>();
        }
        List<ModelAndTitleExcelMapping> mappingList = new ArrayList<>();
        for (ColumnProps columnProps : columnPropsList) {
            //不展示
            Boolean show = columnProps.getShow();
            if (show != null) {
                if (!show) {
                    continue;
                }
            }
            ModelAndTitleExcelMapping mapping = new ModelAndTitleExcelMapping();
            mapping.setKey(columnProps.getProp());
            mapping.setTitle(columnProps.getLabel());
            mapping.setType(columnProps.getType());
            mapping.setChildren(convertMappingListRecursion(columnProps.getChildren()));
            mappingList.add(mapping);
        }
        return mappingList;
    }

    public static <T> Workbook exportDynamicDataWithMergeListToExcel(List<DynamicExcelWithMerge<T>> dynamicExcelWithMergeList) throws Exception {
        if (CollectionUtil.isEmpty(dynamicExcelWithMergeList)) {
            log.error("[导出动态表格]数据不能为空");
            throw new Exception("[导出动态表格]数据不能为空");
        }
        SXSSFWorkbook workbook = new SXSSFWorkbook(3000);
        Map<String, String> sheetStartRowAndColMap =
                dynamicExcelWithMergeList.stream()
                        .map(DynamicExcelWithMerge::getSheetName)
                        .distinct()
                        .collect(Collectors.toMap(Function.identity(), item -> "0_0"));

        for (DynamicExcelWithMerge<T> dynamicExcelWithMerge : dynamicExcelWithMergeList) {
            List<ModelAndTitleExcelMapping> mappingList = dynamicExcelWithMerge.getMappingList();
            if (mappingList==null){
                List<ColumnProps> mainColumns = dynamicExcelWithMerge.getMainColumns();
                mappingList = ExcelUtils.convertMappingListRecursion(mainColumns);
                Function<List<ModelAndTitleExcelMapping>, List<ModelAndTitleExcelMapping>> handleMapping = dynamicExcelWithMerge.getHandleMapping();
                if (handleMapping!=null){
                    mappingList=handleMapping.apply(mappingList);
                }
            }
            List<Map<String, T>> mainTableData = dynamicExcelWithMerge.getMainTableData();
            String sheetName = dynamicExcelWithMerge.getSheetName();

            //获取sheet的起始行和列
            String startRowAndCol = sheetStartRowAndColMap.get(sheetName);
            String[] startRowAndColSplit = startRowAndCol.split("_");
            int startRow = Integer.parseInt(startRowAndColSplit[0]);
            int startColumn = Integer.parseInt(startRowAndColSplit[1]);

            ExcelOtherInfo excelOtherInfo = dynamicExcelWithMerge.getExcelOtherInfo();
            if (excelOtherInfo==null){
                excelOtherInfo = new ExcelOtherInfo();
            }
            String title = dynamicExcelWithMerge.getTitle();
            exportDataToExcelFileLevelWithParams(workbook, mappingList, mainTableData, excelOtherInfo, startRow, startColumn, sheetName,title);
            //如果有标题，开始行需要加1
            if (title!=null){
                startRow=startRow+1;
            }
            //获取表格结束行
            int endRow = excelOtherInfo.getEndRow();

            //合并单元格
            Map<String, RowColumnSpan> rowColumnSpanMap = dynamicExcelWithMerge.getRowColumnSpanMap();
            int level = getLevel(mappingList);
            Sheet sheetAt = workbook.getSheet(sheetName);
            CellStyle defaultCellStyle = createDefaultCellStyle(workbook);
            if (CollectionUtil.isNotEmpty(rowColumnSpanMap)) {
                //合并单元格
                for (Map.Entry<String, RowColumnSpan> entry : rowColumnSpanMap.entrySet()) {
                    String key = entry.getKey();
                    String[] split = key.split("_");
                    int dataRowIndex = Integer.parseInt(split[0]);
                    int dataColumnIndex = Integer.parseInt(split[1]);
                    RowColumnSpan value = entry.getValue();
                    if (value.getRowspan() == 0 && value.getColspan() == 0) {
                        continue;
                    }
                    if (value.getRowspan() == 1 && value.getColspan() == 1) {
                        continue;
                    }
                    int rowIndex = startRow + dataRowIndex + level;
                    mergedRegion(sheetAt, rowIndex, rowIndex + (value.getRowspan() - 1), dataColumnIndex, dataColumnIndex + (value.getColspan() - 1), defaultCellStyle);
                }
            }


            //重新设置下次填充数据的起始行和列
            int nextStartRow = endRow + 4;
            sheetStartRowAndColMap.put(sheetName, nextStartRow + "_" + startColumn);
        }
        return workbook;
    }


    public static <T> Workbook exportDynamicDataWithMergeToExcel(DynamicExcelWithMerge<T> dynamicExcelWithMerge) throws Exception {
        return exportDynamicDataWithMergeListToExcel(Collections.singletonList(dynamicExcelWithMerge));
    }

    //把children拍成同级
    public static List<ModelAndTitleExcelMapping> mappingListToLevelOne(List<ModelAndTitleExcelMapping> mappingList){
        if (CollectionUtil.isEmpty(mappingList)){
            return null;
        }
        List<ModelAndTitleExcelMapping> levelOneMappingList = new ArrayList<>();
        for (ModelAndTitleExcelMapping mapping:mappingList){
            //处理主项
            if (CollectionUtil.isNotEmpty(mapping.getChildren())){
                //递归处理子项
                List<ModelAndTitleExcelMapping> childrenList = mappingListToLevelOne(mapping.getChildren());
                if (CollectionUtil.isNotEmpty(childrenList)){
                    levelOneMappingList.addAll(childrenList);
                }
            }else {
                levelOneMappingList.add(mapping);
            }
        }
        return levelOneMappingList;
    }

    /**
     * @param mappingList 列名映射
     * @param dataList    数据导出
     * @param info        其它导出信息 可为null
     * @param startRow    导出开始行
     * @param sheetName   sheet名称
     * @param <T>         数据泛型
     * @throws Exception 异常
     */
    private static <T> void exportDataToExcelFileLevelWithParams(Workbook workbook, List<ModelAndTitleExcelMapping> mappingList,
                                                                    List<T> dataList, ExcelOtherInfo info, int startRow, int startColumn,
                                                                    String sheetName,String title) throws Exception {
        if (CollectionUtil.isEmpty(mappingList)) {
            throw new CommonException("映射为空");
        }
        int level = getLevel(mappingList);
        if (level <= 0) {
            throw new CommonException("非法层数");
        }
        //校验SXSSFWorkbook临时文件目录是否存在
        String tmpdirPath = System.getProperty("java.io.tmpdir");
        if (StrUtil.isNotBlank(tmpdirPath)) {
            String poifiles = tmpdirPath + "/poifiles";
            File tmpdir = new File(poifiles);
            if (!tmpdir.exists()) {
                boolean mkdirs = tmpdir.mkdirs();
                if (!mkdirs) {
                    throw new CommonException(StrUtil.format("创建临时目录失败->{}", poifiles));
                }
            }
        }
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
        }
        CellStyle defaultCellStyle = createDefaultCellStyle(workbook);
        Map<String, ExcelIndex> indexMapping = new HashMap<>();
        //拍成一级
        List<ModelAndTitleExcelMapping> levelOneMappingList = mappingListToLevelOne(mappingList);
        if (CollectionUtil.isEmpty(levelOneMappingList)){
            levelOneMappingList=new ArrayList<>();
        }
        //创建表格标题
        if (info == null) {
            info = new ExcelOtherInfo();
        }
        if (title!=null){
            //居中样式
            CellStyle titleStyle = workbook.createCellStyle();
            BorderStyle defaultBorderStyle = BorderStyle.THIN;
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            titleStyle.setBorderBottom(defaultBorderStyle);
            titleStyle.setBorderTop(defaultBorderStyle);
            titleStyle.setBorderLeft(defaultBorderStyle);
            titleStyle.setBorderRight(defaultBorderStyle);
            Font font = workbook.createFont();
            font.setFontHeightInPoints((short) 15);
            font.setFontName("宋体");
            font.setBold(true);
            titleStyle.setFont(font);
            //换行
            titleStyle.setWrapText(true);

            setColumnValue(startRow,startColumn,sheet,title,defaultCellStyle);
            mergedRegion(sheet,startRow,startRow,startColumn,startColumn+levelOneMappingList.size()-1,titleStyle);

            Row row = sheet.getRow(startRow);
            row.setHeightInPoints(50);

            startRow=startRow+1;
        }
        //创建列标题
        createTitleRecursive(sheet, indexMapping, defaultCellStyle, mappingList, startRow, startColumn, level);
        //设置列宽
        for (ModelAndTitleExcelMapping mapping : mappingList) {
            Integer width = mapping.getWidth();
            if (width != null) {
                ExcelIndex excelIndex = indexMapping.get(mapping.getKey());
                if (excelIndex != null) {
                    setColumnWidth(sheet, excelIndex.getIndex(), width);
                }
            }
        }
        Map<String, Map<String, List<ExcelFile>>> fileListMapping = info.getFileListMapping();
        if (fileListMapping == null) {
            fileListMapping = new HashMap<>();
        }

        int rowIndex = startRow + level;
        int dataRowIndex = 0;
        List<Field> fieldsDirectly = null;
        for (T t : dataList) {
            //设置单元格边框
            for (int colIndex=startColumn;colIndex<levelOneMappingList.size();colIndex++){
                putCommonValue(sheet, defaultCellStyle, rowIndex, colIndex, "", null);
            }
            if (t instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) t;
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    ExcelIndex excelIndex = indexMapping.get(entry.getKey());
                    if (excelIndex == null) {
                        continue;
                    }
                    Integer columnIndex = excelIndex.getIndex();
                    Object value = entry.getValue();
                    ModelAndTitleExcelMapping mapping = excelIndex.getMapping();
                    if (mapping.isFileColumn()) {
                        Map<String, List<ExcelFile>> excelFileList = fileListMapping.get(entry.getKey());
                        putFile(workbook, sheet, rowIndex, dataRowIndex, columnIndex, excelFileList);
                    } else {
                        putCommonValue(sheet, defaultCellStyle, rowIndex, columnIndex, value, mapping);
                    }
                }
            } else {
                Class<?> tClass = t.getClass();
                if (CollectionUtil.isEmpty(fieldsDirectly)) {
                    fieldsDirectly = Arrays.stream(ReflectUtil.getFieldsDirectly(tClass, true)).collect(Collectors.toList());
                }
                for (Field field : fieldsDirectly) {
                    field.setAccessible(true);
                    ExcelIndex excelIndex = indexMapping.get(field.getName());
                    if (excelIndex == null) {
                        continue;
                    }
                    Integer columnIndex = excelIndex.getIndex();
                    Object value = field.get(t);
                    ModelAndTitleExcelMapping mapping = excelIndex.getMapping();
                    if (mapping.isFileColumn()) {
                        Map<String, List<ExcelFile>> excelFileList = fileListMapping.get(field.getName());
                        putFile(workbook, sheet, rowIndex, dataRowIndex, columnIndex, excelFileList);
                    } else {
                        putCommonValue(sheet, defaultCellStyle, rowIndex, columnIndex, value, mapping);
                    }
                }
            }
            rowIndex++;
            dataRowIndex++;
        }

        //设置最后的定位行
        info.setEndRow(rowIndex - 1);
    }

    private static void putCommonValue(Sheet sheet, CellStyle defaultCellStyle, int rowIndex, Integer columnIndex, Object value, ModelAndTitleExcelMapping mapping) {
        if (mapping!=null){
            ExportConvert<?, ?> exportConvert = mapping.getExportConvert();
            if (exportConvert != null) {
                @SuppressWarnings("unchecked")
                ExportConvert<Object, Object> objectConvert = (ExportConvert<Object, Object>) exportConvert;
                value = objectConvert.convert(value, mapping);
            }
        }
        if (value == null) {
            setColumnValue(rowIndex, columnIndex, sheet, " ", defaultCellStyle);
        } else {
            if (value instanceof Date) {
                Date date = (Date) value;
                value = cn.hutool.core.date.DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
            }
            if (value instanceof LocalDateTime) {
                value = LocalDateTimeUtil.format((LocalDateTime) value, "yyyy-MM-dd HH:mm:ss");
            }
            setColumnValue(rowIndex, columnIndex, sheet, value, defaultCellStyle);
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


    private static byte[] getSystemIcon(String fileName) {
        String suffix = FileUtil.getSuffix(fileName);
        byte[] readBytes = iconMapping.get(suffix);
        if (readBytes != null) {
            return readBytes;
        } else {
            String prefix = FileUtil.getPrefix(fileName);
            File tempFile = FileUtil.createTempFile(prefix, "." + suffix, true);
            FileSystemView view = FileSystemView.getFileSystemView();
            Icon systemIcon = view.getSystemIcon(tempFile);
            Image image = iconToImage(systemIcon);
            readBytes = CommonUtil.imageToByteArray(image);
            iconMapping.put(suffix, readBytes);
            String absolutePath = tempFile.getAbsolutePath();
            if (tempFile.delete()) {
                log.info("临时文件->{}删除成功", absolutePath);
            }
            return readBytes;
        }
    }

    private static void putFile(Workbook workbook, Sheet sheet, int rowIndex, int dataRowIndex, Integer columnIndex, Map<String, List<ExcelFile>> excelFileMapping) throws IOException {
        Row sheetRow = sheet.getRow(rowIndex);
        if (sheetRow == null) {
            sheetRow = sheet.createRow(rowIndex);
        }
        Cell cell = sheetRow.getCell(columnIndex);
        if (cell == null) {
            sheetRow.createCell(columnIndex);
        }
        if (CollectionUtil.isNotEmpty(excelFileMapping)) {
            List<ExcelFile> excelFileList = excelFileMapping.get(String.valueOf(dataRowIndex));
            if (CollectionUtil.isEmpty(excelFileList)) {
                return;
            }
            for (ExcelFile excelFile : excelFileList) {
                byte[] readBytes = excelFile.getReadBytes();

                String fileName = excelFile.getName();
                if (StrUtil.isBlank(fileName)) {
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
                log.info("excel文件->{},大小->{}字节", fileName, readBytes.length);
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


    private static void getDeepTitleMappingRecursion(String parentTitle, List<ModelAndTitleExcelMapping> mappingList, Map<String, ModelAndTitleExcelMapping> deepTitleMapping) {
        for (ModelAndTitleExcelMapping mapping : mappingList) {
            if (CollectionUtil.isEmpty(mapping.getChildren())) {
                if (StrUtil.isBlank(parentTitle)) {
                    deepTitleMapping.put(mapping.getTitle(), mapping);
                } else {
                    deepTitleMapping.put(parentTitle + "_" + mapping.getTitle(), mapping);
                }
            } else {
                getDeepTitleMappingRecursion(mapping.getTitle(), mapping.getChildren(), deepTitleMapping);
            }
        }
    }


    //获取定位
    private static Map<Integer, ModelAndTitleExcelMapping> getTitleIndexMapping(Sheet sheet, List<ModelAndTitleExcelMapping> mappingList) {
        Map<Integer, ModelAndTitleExcelMapping> titleExcelMappingMap = new HashMap<>();
        int level = getLevel(mappingList);
        //获取列名对应
        Map<String, ModelAndTitleExcelMapping> deepTitleMapping = new HashMap<>();
        getDeepTitleMappingRecursion("", mappingList, deepTitleMapping);
        //处理映射
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        Map<Integer, Map<Integer, String>> levelTitleMapping = new HashMap<>();
        for (int row = 0; row < level; row++) {
            //只取第row行的列合并
            int finalRow = row;
            List<CellRangeAddress> cellRangeAddressList = mergedRegions.stream()
                    .filter(item -> item.getFirstRow() == item.getLastRow())
                    .filter(item -> item.getFirstRow() == finalRow)
                    .collect(Collectors.toList());
            //处理非合并项
            if (row == 0) {
                Map<Integer, Integer> ignoreCol = cellRangeAddressList
                        .stream()
                        .flatMap(item -> Arrays.stream(NumberUtil.range(item.getFirstColumn(), item.getLastColumn())).boxed())
                        .collect(Collectors.toMap(Function.identity(), Function.identity()));
                Row sheetRow = sheet.getRow(row);
                for (int col = 0; col <= sheetRow.getLastCellNum(); col++) {
                    Integer integer = ignoreCol.get(col);
                    if (integer == null) {
                        Cell cell = sheet.getRow(row).getCell(col);
                        String cellStrValue = getCellStrValue(cell, null, null);
                        ModelAndTitleExcelMapping mapping = deepTitleMapping.get(cellStrValue.trim());
                        if (mapping != null) {
                            titleExcelMappingMap.put(col, mapping);
                        }
                    }
                }
            }
            //处理合并项
            for (CellRangeAddress cellAddresses : cellRangeAddressList) {
                //合并项取值只取第一列
                Cell cell = sheet.getRow(row).getCell(cellAddresses.getFirstColumn());
                String cellStrValue = getCellStrValue(cell, null, null);

                for (int col = cellAddresses.getFirstColumn(); col <= cellAddresses.getLastColumn(); col++) {
                    Map<Integer, String> stringMap = levelTitleMapping.get(row);
                    if (CollectionUtil.isEmpty(stringMap)) {
                        stringMap = new HashMap<>();
                        levelTitleMapping.put(row, stringMap);
                    }
                    if (row == 0) {
                        stringMap.put(col, cellStrValue);
                    } else {
                        //拼接上层标题
                        Map<Integer, String> pre = levelTitleMapping.get(row - 1);
                        String preCellStrValue = pre.get(col);
                        cellStrValue = preCellStrValue + "_" + cellStrValue;
                        stringMap.put(col, cellStrValue);
                    }
                }
            }
            //合并项最后一层
            if (row != 0 && row == level - 1) {
                Row sheetRow = sheet.getRow(row);
                for (int col = 0; col <= sheetRow.getLastCellNum(); col++) {
                    Cell cell = sheet.getRow(row).getCell(col);
                    String cellStrValue = getCellStrValue(cell, null, null);

                    //获取上层名字然后拼接
                    Map<Integer, String> pre = levelTitleMapping.get(row - 1);
                    if (pre == null) {
                        continue;
                    }
                    String preCellStrValue = pre.get(col);
                    if (preCellStrValue != null) {
                        cellStrValue = preCellStrValue + "_" + cellStrValue;
                        ModelAndTitleExcelMapping mapping = deepTitleMapping.get(cellStrValue.trim());
                        if (mapping != null) {
                            titleExcelMappingMap.put(col, mapping);
                        }
                    }
                }
            }
        }

        return titleExcelMappingMap;
    }

    public static List<Map<String, Object>> readExcelData(InputStream inputStream, Integer sheetIndex, List<ModelAndTitleExcelMapping> mappingList) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheetAt = workbook.getSheetAt(sheetIndex);
            int level = getLevel(mappingList);
            Map<Integer, ModelAndTitleExcelMapping> titleIndexMapping = getTitleIndexMapping(sheetAt, mappingList);
            //从数据列开始读取
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            for (int rowIndex = level; rowIndex <= sheetAt.getLastRowNum(); rowIndex++) {
                Row row = sheetAt.getRow(rowIndex);
                Map<String, Object> result = new HashMap<>();
                for (int colIndex = 0; colIndex <= row.getLastCellNum(); colIndex++) {
                    ModelAndTitleExcelMapping mapping = titleIndexMapping.get(colIndex);
                    if (mapping == null) {
                        continue;
                    }
                    Cell cell = row.getCell(colIndex);
                    Object cellValue = getCellValue(cell, formulaEvaluator);
                    String key = mapping.getKey();
                    String[] splitKeyList = key.split("\\.");
                    if (splitKeyList.length == 1) {
                        result.put(mapping.getKey(), cellValue);
                    } else {
                        Map<String, Object> parentMap = result;
                        String parentKey = splitKeyList[0];
                        for (int i = 1; i < splitKeyList.length; i++) {
                            String childrenKey = splitKeyList[i];
                            Map<String, Object> childrenMap;
                            Object parentObj = parentMap.get(parentKey);
                            if (parentObj == null) {
                                childrenMap = new HashMap<>();
                            } else {
                                childrenMap = BeanUtil.beanToMap(parentObj);
                            }
                            childrenMap.put(childrenKey, cellValue);
                            parentMap.put(parentKey, childrenMap);
                            //循环下次
                            parentMap = childrenMap;
                            parentKey = childrenKey;
                        }
                    }
                }
                resultList.add(result);
            }
            return resultList;
        } catch (Exception e) {
            log.error("读取excel异常", e);
            String msg = StrUtil.format("读取excel异常->{}", ExceptionUtil.stacktraceToString(e));
            throw new CommonException(msg);
        }
    }

    /**
     * 从excel表格获取数据
     */
    public static <T> List<T> getDataByExcelFile(InputStream inputStream, Integer sheetIndex, List<ModelAndTitleExcelMapping> mappingList, Class<T> clazz) throws Exception {
        List<Map<String, Object>> readAll = readExcelData(inputStream, sheetIndex, mappingList);
        List<T> list = new ArrayList<>();
        //读取excel为空则直接返回
        if (CollectionUtil.isEmpty(readAll)) {
            return list;
        }
        if (CollectionUtil.isEmpty(mappingList)) {
            return list;
        }
        for (Map<String, Object> map : readAll) {
            T newInstance = clazz.newInstance();
            Map<String, Object> nestedObjMap = new HashMap<>();
            for (ModelAndTitleExcelMapping mapping : mappingList) {
                String key = mapping.getKey();
                String[] splitKeyList = key.split("\\.");
                if (splitKeyList.length != 1) {
                    String parentKey = splitKeyList[0];
                    Object parentValue = map.get(parentKey);
                    if (parentValue == null) {
                        continue;
                    }
                    Field parentField = ReflectUtil.getField(newInstance.getClass(), parentKey);
                    if (parentField == null) {
                        continue;
                    }
                    Object parentObj = nestedObjMap.get(parentKey);
                    if (parentObj == null) {
                        parentObj = parentField.getType().newInstance();
                        ReflectUtil.setFieldValue(newInstance, parentKey, parentObj);
                        nestedObjMap.put(parentKey, parentObj);
                    }
                    if (!(parentValue instanceof Map)) {
                        continue;
                    }
                    Map<String, Object> parentMap = BeanUtil.beanToMap(parentValue);

                    for (int i = 1; i < splitKeyList.length; i++) {
                        String childrenKey = splitKeyList[i];
                        //判断子属性是否存在
                        Field childrenField = ReflectUtil.getField(parentObj.getClass(), childrenKey);
                        if (childrenField == null) {
                            break;
                        }
                        //嵌套属性设置值
                        if (i == splitKeyList.length - 1) {
                            ImportConvert<?, ?> importConvert = mapping.getImportConvert();
                            Object value = parentMap.get(childrenKey);
                            if (importConvert != null) {
                                @SuppressWarnings("unchecked")
                                ImportConvert<Object, Object> objectConvert = (ImportConvert<Object, Object>) importConvert;
                                Object convertObject = objectConvert.convert(value, mapping);
                                ReflectUtil.setFieldValue(parentObj, childrenKey, convertObject);
                            } else {
                                ReflectUtil.setFieldValue(parentObj, childrenKey, value);
                            }
                        } else {
                            //获取子map属性值
                            Object childrenValue = parentMap.get(childrenKey);
                            if (!(childrenValue instanceof Map)) {
                                break;
                            }
                            parentMap = BeanUtil.beanToMap(childrenValue);
                            //创建子对象
                            Object childrenObj = ReflectUtil.getFieldValue(parentObj, childrenKey);
                            if (childrenObj == null) {
                                childrenObj = childrenField.getType().newInstance();
                                ReflectUtil.setFieldValue(parentObj, childrenKey, childrenObj);
                            }
                            parentObj = childrenObj;
                        }
                    }
                } else {
                    Object value = map.get(key);
                    if (value == null) {
                        continue;
                    }
                    Field field = ReflectUtil.getField(newInstance.getClass(), key);
                    if (field==null){
                        continue;
                    }
                    if (field.getType().equals(BigDecimal.class)){
                        if (StrUtil.isBlank(value.toString())){
                            continue;
                        }
                    }
                    ImportConvert<?, ?> importConvert = mapping.getImportConvert();
                    if (importConvert != null) {
                        @SuppressWarnings("unchecked")
                        ImportConvert<Object, Object> objectConvert = (ImportConvert<Object, Object>) importConvert;
                        Object convertObject = objectConvert.convert(value, mapping);
                        ReflectUtil.setFieldValue(newInstance, field, convertObject);
                    } else {
                        ReflectUtil.setFieldValue(newInstance, field, value);
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

        if (file != null) {
            if (StrUtil.isNotBlank(file.getOriginalFilename())) {
                if (!file.getOriginalFilename().endsWith(".xlsx")) {
                    throw new CommonException("导入文件的格式不正确，目前只支持xlsx");
                }
            }
        }

        return file;
    }


    private static Object getCellValue(Cell cell, FormulaEvaluator formulaEvaluator) {
        if (cell == null) {
            return null;
        }
        //日期
        if (CellType.NUMERIC.equals(cell.getCellType()) && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue();
        }
        //字符
        if (CellType.STRING.equals(cell.getCellType())) {
            return cell.getStringCellValue();
        }
        //数字
        if (CellType.NUMERIC.equals(cell.getCellType())) {
            double numericCellValue = cell.getNumericCellValue();
            return new BigDecimal(numericCellValue);
        }
        //公式
        if (CellType.FORMULA.equals(cell.getCellType())) {
            if (formulaEvaluator == null) {
                return "";
            }
            String formula = cell.getCellFormula();
            cell = formulaEvaluator.evaluateInCell(cell);
            switch (cell.getCellType()) {
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue();
                    } else {
                        double numericCellValue = cell.getNumericCellValue();
                        return new BigDecimal(numericCellValue);
                    }
                case STRING:
                    RichTextString value = cell.getRichStringCellValue();
                    return value == null ? null : value.getString();
                case ERROR:
                    ErrorCellValue errorCellValue = new ErrorCellValue(cell);
                    log.error(StrUtil.format("公式->{}解析错误->{}", formula, errorCellValue.getValue()));
                    return null;
                default:
                    log.error(StrUtil.format("不支持的类型->{}", cell.getCellType().name()));
                    return null;
            }
        }
        return null;
    }

    private static String getCellStrValue(Cell cell, Map<CellConvertType, CellToStrConvert<?>> cellToStrConvertMap, FormulaEvaluator formulaEvaluator) {
        if (cell == null) {
            return "";
        }
        String cellStrVale = "";
        //日期
        if (CellType.NUMERIC.equals(cell.getCellType()) && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            if (CollectionUtil.isEmpty(cellToStrConvertMap)) {
                cellStrVale = cn.hutool.core.date.DateUtil.format(date, "yyyy-MM-dd");
            } else {
                @SuppressWarnings("unchecked")
                CellToStrConvert<Date> dateConvert = (CellToStrConvert<Date>) cellToStrConvertMap.get(CellConvertType.DATE);
                cellStrVale = dateConvert.convert(date);
            }
        }
        //字符
        if (CellType.STRING.equals(cell.getCellType())) {
            String str = cell.getStringCellValue();
            cellStrVale = str == null ? "" : str;
        }
        //数字
        if (CellType.NUMERIC.equals(cell.getCellType())) {
            double numericCellValue = cell.getNumericCellValue();
            if (CollectionUtil.isEmpty(cellToStrConvertMap)) {
                cellStrVale = String.valueOf(numericCellValue);
            } else {
                @SuppressWarnings("unchecked")
                CellToStrConvert<Double> convert = (CellToStrConvert<Double>) cellToStrConvertMap.get(CellConvertType.NUMERIC);
                cellStrVale = convert.convert(numericCellValue);
            }
        }
        //公式
        if (CellType.FORMULA.equals(cell.getCellType())) {
            if (formulaEvaluator == null) {
                return "";
            }
            String formula = cell.getCellFormula();
            cell = formulaEvaluator.evaluateInCell(cell);
            switch (cell.getCellType()) {
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        Date date = cell.getDateCellValue();
                        if (CollectionUtil.isEmpty(cellToStrConvertMap)) {
                            cellStrVale = cn.hutool.core.date.DateUtil.format(date, "yyyy-MM-dd");
                        } else {
                            @SuppressWarnings("unchecked")
                            CellToStrConvert<Date> dateConvert = (CellToStrConvert<Date>) cellToStrConvertMap.get(CellConvertType.DATE);
                            cellStrVale = dateConvert.convert(date);
                        }
                    } else {
                        double numericCellValue = cell.getNumericCellValue();
                        if (CollectionUtil.isEmpty(cellToStrConvertMap)) {
                            cellStrVale = String.valueOf(numericCellValue);
                        } else {
                            @SuppressWarnings("unchecked")
                            CellToStrConvert<Double> convert = (CellToStrConvert<Double>) cellToStrConvertMap.get(CellConvertType.NUMERIC);
                            cellStrVale = convert.convert(numericCellValue);
                        }
                    }
                    break;
                case STRING:
                    RichTextString value = cell.getRichStringCellValue();
                    cellStrVale = value == null ? "" : value.getString();
                    break;
                case ERROR:
                    ErrorCellValue errorCellValue = new ErrorCellValue(cell);
                    throw new CommonException(StrUtil.format("公式->{}解析错误->{}", formula, errorCellValue.getValue()));
                default:
                    throw new CommonException(StrUtil.format("不支持的类型->{}", cell.getCellType().name()));
            }
        }
        return cellStrVale;
    }


    /**
     * 导出excel流到HTTP响应流（默认名称）
     */
    public static void exportExcel(Workbook workbook, HttpServletResponse response) {
        exportExcel(workbook, "导出", response);
    }


    /**
     * 导出excel流到HTTP响应流（自定义名称）
     */
    public static void exportExcel(Workbook workbook, String fileName, HttpServletResponse response) {
        OutputStream outputStream = null;
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
            outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            String msg = StrUtil.format("导出excel失败->{}", ExceptionUtil.stacktraceToString(e));
            log.error(msg);
            throw new CommonException(msg);
        } finally {
            try {
                if (workbook != null) {
                    workbook.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                log.error("导出excel后，关闭流失败", e);
            }
        }
    }

    private static MergedInfo findMergedRegion(Sheet sheet, Cell cell) {
        if (cell == null) {
            return null;
        }
        return findMergedRegion(sheet, cell.getRowIndex(), cell.getColumnIndex());
    }

    private static MergedInfo findMergedRegion(Sheet sheet, int rowIndex, int colIndex) {
        if (sheet == null) {
            return null;
        }
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.isInRange(rowIndex, colIndex)) {
                MergedInfo mergedInfo = new MergedInfo();
                mergedInfo.setMergedIndex(i);
                mergedInfo.setCellAddresses(mergedRegion);

                //row为局部行，从0开始
                //col为局部列，从0开始
                //合并单元格局部样式映射、行高映射、列宽映射
                //单元格局部样式映射为key=row_col，行高映射key=row,列宽映射key=col
                Map<String, CellStyle> mergedStyleMap = new HashMap<>();
                Map<Integer, Integer> rowHeightMap = new HashMap<>();
                Map<Integer, Integer> colWidthMap = new HashMap<>();
                int partRow = 0;
                for (int t = mergedRegion.getFirstRow(); t <= mergedRegion.getLastRow(); t++) {
                    int partCol = 0;
                    for (int u = mergedRegion.getFirstColumn(); u <= mergedRegion.getLastColumn(); u++) {
                        Row sheetRow = sheet.getRow(t);

                        short partRowHeight = sheetRow.getHeight();
                        rowHeightMap.put(partRow, (int) partRowHeight);
                        int partColWidth = sheet.getColumnWidth(u);
                        colWidthMap.put(partCol, partColWidth);

                        Cell sheetRowCell = sheetRow.getCell(u);
                        CellStyle originCellStyle = sheetRowCell.getCellStyle();
                        mergedStyleMap.put(partRow + "_" + partCol, originCellStyle);
                        partCol++;
                    }
                    partRow++;
                }
                mergedInfo.setMergedStyleMap(mergedStyleMap);
                mergedInfo.setRowHeightMap(rowHeightMap);
                mergedInfo.setColWidthMap(colWidthMap);

                return mergedInfo;
            }
        }
        return null;
    }

    //${prop} 替换数据
    //横竖for循环
    //$${list.supplier:row} 竖着填充数据,带.代表list里面的子属性
    //$${list.supplier:col} 横着填充数据,带.代表list里面的子属性
    //$${list:col} 不带.代表list
    //图片渲染
    //#IMG{key}
    public static void initTemplateData(Workbook workbook, String sheetName,
                                        Map<String, Object> dataMap){
        initTemplateData(workbook, sheetName, dataMap,null);
    }

    public static void initTemplateData(Workbook workbook, String sheetName,
                                        Map<String, Object> dataMap,ExcelTemplateOther other) {
        if (CollectionUtil.isEmpty(dataMap)) {
            return;
        }
        if (workbook == null) {
            throw new CommonException("workbook不能为空");
        }
        if (StrUtil.isBlank(sheetName)) {
            throw new CommonException("sheetName不能为空");
        }
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            throw new CommonException("sheetName不存在");
        }
        Pattern replacepattern = Pattern.compile("\\$\\{[^}]*\\}");
        Pattern calculatePattern = Pattern.compile("\\$\\$\\{[^}]*\\}");
        Pattern imgPattern = Pattern.compile("#IMG\\{[^}]*\\}");
        int lastRowNum = sheet.getLastRowNum();
        for (int i = 0; i < lastRowNum; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                sheet.createRow(i);
                row = sheet.getRow(i);
            }
            int lastCellNum = row.getLastCellNum();
            short rowHeight = row.getHeight();
            for (int j = 0; j < lastCellNum; j++) {
                //获取列宽
                int columnWidth = sheet.getColumnWidth(j);
                //原始cell
                Cell cell = row.getCell(j);
                if (cell == null) {
                    continue;
                }
                String cellStrValue = getCellStrValue(cell, null, null);
                if (StrUtil.isBlank(cellStrValue)) {
                    continue;
                }
                if (cellStrValue.startsWith("$${")) {
                    Matcher matcher = calculatePattern.matcher(cellStrValue);
                    String group;
                    if (matcher.find()) {
                        //group值为$${xxxx}
                        group = matcher.group();
                    } else {
                        continue;
                    }

                    String key = group.substring(3, group.length() - 1);
                    String[] split = key.split(":");
                    if (split.length != 2) {
                        continue;
                    }

                    List<?> list = new ArrayList<>();
                    String prop = null;
                    if (split[0].contains(".")) {
                        String[] listObjStr = split[0].split("\\.");
                        if (listObjStr.length != 2) {
                            continue;
                        }
                        Object object = dataMap.get(listObjStr[0]);
                        if (object instanceof List<?>) {
                            list = (List<?>) object;
                        }
                        prop = listObjStr[1];
                    } else {
                        Object object = dataMap.get(split[0]);
                        if (object instanceof List<?>) {
                            list = (List<?>) object;
                        }
                    }
                    //处理合并
                    MergedInfo mergedInfo = findMergedRegion(sheet, cell);
                    int rowjump = 1;
                    int coljump = 1;

                    //row为局部行，从0开始
                    //col为局部列，从0开始
                    //合并单元格局部样式映射、行高映射、列宽映射
                    //单元格局部样式映射为key=row_col，行高映射key=row,列宽映射key=col
                    Map<String, CellStyle> mergedStyleMap = new HashMap<>();
                    Map<Integer, Integer> rowHeightMap = new HashMap<>();
                    Map<Integer, Integer> colWidthMap = new HashMap<>();
                    if (mergedInfo != null) {
                        CellRangeAddress mergedRegion = mergedInfo.getCellAddresses();
                        if (mergedRegion != null) {
                            rowjump = mergedRegion.getLastRow() - mergedRegion.getFirstRow() + 1;
                            coljump = mergedRegion.getLastColumn() - mergedRegion.getFirstColumn() + 1;
                            mergedStyleMap = mergedInfo.getMergedStyleMap();
                            rowHeightMap = mergedInfo.getRowHeightMap();
                            colWidthMap = mergedInfo.getColWidthMap();
                        }
                    }
                    //row 行循环
                    //col 列循环
                    String forType = split[1];

                    int dataIndex = 0;
                    int initIndex = "row".equals(forType) ? i : j;
                    int jump = "row".equals(forType) ? rowjump : coljump;
                    int lastIndex = jump * list.size() + initIndex;
                    for (int n = initIndex; n < lastIndex; n = n + jump) {
                        Object value = list.get(dataIndex);
                        dataIndex++;

                        Cell originCell;
                        if ("row".equals(forType)) {
                            //固定列j不变
                            Row originRow = sheet.getRow(n);
                            if (originRow == null) {
                                originRow = sheet.createRow(n);
                            }
                            originCell = originRow.getCell(j);
                            if (originCell == null) {
                                originCell = originRow.createCell(j);
                            }
                        } else {
                            //固定行i不变
                            Row originRow = sheet.getRow(i);
                            if (originRow == null) {
                                originRow = sheet.createRow(i);
                            }
                            originCell = originRow.getCell(n);
                            if (originCell == null) {
                                originCell = originRow.createCell(n);
                            }
                        }
                        if (rowjump != 1 || coljump != 1) {
                            CellRangeAddress originCellAddresses;
                            if ("row".equals(forType)) {
                                //for循环删除需要合并的旧合并区，并设置样式
                                int partRow = 0;
                                for (int x = n; x < n + rowjump; x++) {
                                    int partCol = 0;
                                    for (int y = j; y < j + coljump; y++) {
                                        //删除旧的合并区
                                        Row sheetRow = sheet.getRow(x);
                                        if (sheetRow == null) {
                                            sheetRow = sheet.createRow(x);
                                        }
                                        Cell sheetCell = sheetRow.getCell(y);
                                        if (sheetCell == null) {
                                            sheetCell = sheetRow.createCell(y);
                                        }
                                        MergedInfo oldMergedInfo = findMergedRegion(sheet, sheetCell);
                                        if (oldMergedInfo != null) {
                                            sheet.removeMergedRegion(oldMergedInfo.getMergedIndex());
                                        }
                                        //设置样式
                                        CellStyle style = mergedStyleMap.get(partRow + "_" + partCol);
                                        if (style != null) {
                                            sheetCell.setCellStyle(style);
                                        }
                                        //设置宽高
                                        Integer partRowHeight = rowHeightMap.get(partRow);
                                        if (partRowHeight != null) {
                                            sheetRow.setHeight(partRowHeight.shortValue());
                                        }
                                        Integer partColWidth = colWidthMap.get(partCol);
                                        if (partColWidth != null) {
                                            sheet.setColumnWidth(y, partColWidth);
                                        }
                                        partCol++;
                                    }
                                    partRow++;
                                }
                                //新增新的合并区
                                originCellAddresses = new CellRangeAddress(n, n + rowjump - 1, j, j + coljump - 1);
                            } else {
                                //for循环删除需要合并的旧合并区
                                int partRow = 0;
                                for (int x = i; x < i + rowjump; x++) {
                                    int partCol = 0;
                                    for (int y = n; y < n + coljump; y++) {
                                        //删除旧的合并区
                                        Row sheetRow = sheet.getRow(x);
                                        if (sheetRow == null) {
                                            sheetRow = sheet.createRow(x);
                                        }
                                        Cell sheetCell = sheetRow.getCell(y);
                                        if (sheetCell == null) {
                                            sheetCell = sheetRow.createCell(y);
                                        }
                                        MergedInfo oldMergedInfo = findMergedRegion(sheet, sheetCell);
                                        if (oldMergedInfo != null) {
                                            sheet.removeMergedRegion(oldMergedInfo.getMergedIndex());
                                        }
                                        //设置样式
                                        CellStyle style = mergedStyleMap.get(partRow + "_" + partCol);
                                        if (style != null) {
                                            sheetCell.setCellStyle(style);
                                        }
                                        //设置宽高
                                        Integer partRowHeight = rowHeightMap.get(partRow);
                                        if (partRowHeight != null) {
                                            sheetRow.setHeight(partRowHeight.shortValue());
                                        }
                                        Integer partColWidth = colWidthMap.get(partCol);
                                        if (partColWidth != null) {
                                            sheet.setColumnWidth(y, partColWidth);
                                        }
                                        partCol++;
                                    }
                                    partRow++;
                                }
                                //新增新的合并区
                                originCellAddresses = new CellRangeAddress(i, i + rowjump - 1, n, n + coljump - 1);
                            }
                            sheet.addMergedRegion(originCellAddresses);
                        }

                        String originStr = getCellStrValue(originCell, null, null);
                        //先清空$${xxx}
                        originStr = originStr.replace(group, "");
                        //再设置值
                        if (value != null) {
                            if (value instanceof Map<?, ?>) {
                                Map<?, ?> map = (Map<?, ?>) value;
                                if (prop != null) {
                                    Object propValue = map.get(prop);
                                    if (propValue != null) {
                                        originStr = propValue.toString();
                                    }
                                }
                            } else {
                                originStr = value.toString();
                            }
                        }
                        //样式设置
                        CellStyle cellStyle = cell.getCellStyle();
                        if ("row".equals(forType)) {
                            //行设置高度
                            row.setHeight(rowHeight);
                            setColumnValue(n, j, sheet, originStr, cellStyle);
                        } else {
                            //列设置宽度
                            sheet.setColumnWidth(n, columnWidth);
                            setColumnValue(i, n, sheet, originStr, cellStyle);
                        }
                    }
                } else if (cellStrValue.startsWith("#IMG{")){
                    if (other==null){
                        continue;
                    }
                    Map<String, ExcelImgIndexInterface> excelTemplateImgInterfaceMap = other.getExcelTemplateImgInterfaceMap();
                    if (excelTemplateImgInterfaceMap==null){
                        continue;
                    }
                    Matcher matcher = imgPattern.matcher(cellStrValue);
                    String group;
                    if (matcher.find()) {
                        //group值为#IMG{xxxx}
                        group = matcher.group();
                    } else {
                        continue;
                    }
                    String key = group.substring(5, group.length() - 1);
                    ExcelImgIndexInterface excelTemplateImgInterface = excelTemplateImgInterfaceMap.get(key);
                    if (excelTemplateImgInterface==null){
                        continue;
                    }
                    //判断是否合并
                    MergedInfo mergedInfo = findMergedRegion(sheet, cell);
                    ExcelImgIndexParams excelTemplateImgIndexParams = new ExcelImgIndexParams();
                    excelTemplateImgIndexParams.setWorkbook(workbook);
                    excelTemplateImgIndexParams.setSheet(sheet);
                    excelTemplateImgIndexParams.setCell(cell);
                    excelTemplateImgIndexParams.setDataMap(dataMap);
                    excelTemplateImgIndexParams.setMergedInfo(mergedInfo);
                    int allHeight;
                    int allWidth;
                    if(mergedInfo!=null){
                        allHeight = mergedInfo.getRowHeightMap().values().stream().reduce(0,Integer::sum);
                        allWidth = mergedInfo.getColWidthMap().values().stream().reduce(0,Integer::sum);
                        excelTemplateImgIndexParams.setStartRow(mergedInfo.getCellAddresses().getFirstRow());
                        excelTemplateImgIndexParams.setStartColumn(mergedInfo.getCellAddresses().getFirstColumn());
                        excelTemplateImgIndexParams.setEndRow(mergedInfo.getCellAddresses().getLastRow());
                        excelTemplateImgIndexParams.setEndColumn(mergedInfo.getCellAddresses().getLastColumn());
                    }else {
                        allHeight = cell.getRow().getHeight();
                        allWidth =  sheet.getColumnWidth(cell.getColumnIndex());
                        excelTemplateImgIndexParams.setStartRow(cell.getRowIndex());
                        excelTemplateImgIndexParams.setStartColumn(cell.getColumnIndex());
                        excelTemplateImgIndexParams.setEndRow(cell.getRowIndex());
                        excelTemplateImgIndexParams.setEndColumn(cell.getColumnIndex());
                    }
                    excelTemplateImgIndexParams.setAllHeight(allHeight);
                    excelTemplateImgIndexParams.setAllWidth(allWidth);
                    List<ExcelImgIndexResult> imgIndexResultList = excelTemplateImgInterface.getImgIndexResult(excelTemplateImgIndexParams);
                    if (CollectionUtil.isEmpty(imgIndexResultList)){
                        continue;
                    }
                    for (ExcelImgIndexResult imgIndexResult : imgIndexResultList){
                        int pictureIndex = workbook.addPicture(imgIndexResult.getImgData(),imgIndexResult.getImgType());
                        Drawing<?> drawing = sheet.createDrawingPatriarch();
                        CreationHelper helper = workbook.getCreationHelper();
                        ClientAnchor anchor = helper.createClientAnchor();
                        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
                        if (imgIndexResult.getDy1()!=null){
                            anchor.setDy1(imgIndexResult.getDy1());
                        }
                        if (imgIndexResult.getDy2()!=null){
                            anchor.setDy2(imgIndexResult.getDy2());
                        }
                        if (imgIndexResult.getDx1()!=null){
                            anchor.setDx1(imgIndexResult.getDx1());
                        }
                        if (imgIndexResult.getDx2()!=null){
                            anchor.setDx2(imgIndexResult.getDx2());
                        }
                        if (imgIndexResult.getCol1()!=null){
                            anchor.setCol1(imgIndexResult.getCol1());
                        }
                        if (imgIndexResult.getCol2()!=null){
                            anchor.setCol2(imgIndexResult.getCol2());
                        }
                        if (imgIndexResult.getRow1()!=null){
                            anchor.setRow1(imgIndexResult.getRow1());
                        }
                        if (imgIndexResult.getRow2()!=null){
                            anchor.setRow2(imgIndexResult.getRow2());
                        }

                        Picture picture = drawing.createPicture(anchor, pictureIndex);
//                        picture.resize();
                    }
                    cellStrValue=cellStrValue.replace(group, "");
                    cell.setCellValue(cellStrValue);
                }else {
                    //${prop} 替换数据
                    Matcher matcher = replacepattern.matcher(cellStrValue);
                    while (matcher.find()) {
                        //group值为${xxx}
                        String group = matcher.group();
                        String key = group.substring(2, group.length() - 1);
                        Object value = dataMap.get(key);
                        if (value == null) {
                            cellStrValue = cellStrValue.replace(group, "");
                        } else {
                            cellStrValue = cellStrValue.replace(group, value.toString());
                        }
                    }
                    cell.setCellValue(cellStrValue);
                }
            }
        }
    }

    public static void cloneSheet(Workbook workbook, String sourceSheetName, String targetSheetName) {
        int index = workbook.getSheetIndex(sourceSheetName);
        if (index == -1){
            throw new CommonException(StrUtil.format("未找到【{}】工作表,无法复制", sourceSheetName));
        }
        Sheet targetSheet = workbook.cloneSheet(index);
        workbook.setSheetName(workbook.getSheetIndex(targetSheet), targetSheetName);
    }

    public static void removeSheet(Workbook workbook, String sheetName) {
        int index = workbook.getSheetIndex(sheetName);
        if (index != -1) {
            workbook.removeSheetAt(index);
        }
    }

    /**
     * 将26进制字符串转换为10进制数字（从1开始）
     * @param column 26进制字符串，如 "AA", "AB"
     * @return 对应的10进制数值
     */
    public static int titleToNumber(String column) {
        if (column == null || column.isEmpty()) {
            return 0;
        }

        int result = 0;
        // 将字符串转换为大写，确保一致性
        column = column.toUpperCase();

        for (int i = 0; i < column.length(); i++) {
            char c = column.charAt(i);
            // 获取当前字符对应的数值（A=1, B=2, ..., Z=26）
            int value = c - 'A' + 1;
            // 计算权重并累加
            result = result * 26 + value;
        }
        return result;
    }

    public static <T> T readValue(Workbook workbook,String sheetName,String rowIndexName,String colIndexName,Class<T> tClass){
        int colIndex= titleToNumber(colIndexName)-1;
        int rowIndex= Integer.parseInt(rowIndexName)-1;
        Sheet sheet = workbook.getSheet(sheetName);
        Row row = sheet.getRow(rowIndex);
        if (row==null){
            return null;
        }
        Cell cell = row.getCell(colIndex);
        if (cell==null){
            return null;
        }
        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
        Object cellValue = getCellValue(cell, formulaEvaluator);
        if (cellValue==null){
            return null;
        }
        return tClass.cast(cellValue);
    }

    public static byte[] convertWorkbookToByte(Workbook workbook){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            workbook.write(byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            return byteArray;
        } catch (IOException e) {
            String msg =StrUtil.format("转换Workbook为byte数组失败->{}", ExceptionUtil.stacktraceToString(e));
            throw new CommonException(msg);
        }
    }


    public static void main(String[] args) throws Exception {
//        FileInputStream fileInputStream = new FileInputStream("F:\\整单供应商绩效评分.xlsx");
//
//        Workbook workbook = new XSSFWorkbook(fileInputStream);
//        int index = workbook.getSheetIndex("明细表");
//        Sheet sheet = workbook.cloneSheet(index);
//        workbook.setSheetName(workbook.getSheetIndex(sheet), "明细表-复制");
//
//        FileOutputStream fileOutputStream = new FileOutputStream("F:\\整单供应商绩效评分-复制.xlsx");
//        workbook.write(fileOutputStream);
//        fileInputStream.close();
//        fileOutputStream.close();

        FileInputStream fileInputStream = new FileInputStream("C:\\Users\\Administrator.DESKTOP-FUF3NOI\\Desktop\\质量管理\\整单供应商绩效\\整单供应商绩效评分模板.xlsx");
        Workbook workbook = new XSSFWorkbook(fileInputStream);
        Map<String, Object> dataMap = new HashMap<>();
        List<Map<String, String>> mapList = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Map<String, String> map = new HashMap<>();
            map.put("supplier", "供应商" + i);
            map.put("zsjfl", "准时交付率" + i);
            map.put("sumLevel", "总等级" + i);
            mapList.add(map);
        }
        dataMap.put("list", mapList);
        ExcelUtils.initTemplateData(workbook, "总表", dataMap);
        dataMap.put("zsjfl_1_$_sjdf", 1111);
        dataMap.put("fw_1_$_sjdf", 2222);
        ExcelUtils.initTemplateData(workbook, "明细表", dataMap);
        FileOutputStream fileOutputStream = new FileOutputStream("F:\\整单供应商绩效评分-复制.xlsx");
        workbook.write(fileOutputStream);
        fileInputStream.close();
        fileOutputStream.close();


//        try (XSSFWorkbook wb = new XSSFWorkbook()) {
//            XSSFSheet sheet = wb.createSheet();
//            XSSFRow row = sheet.createRow(0);
//            row.createCell(0).setCellValue("Embedded File Test");
//
//            String content = "<html><head></head><body><h1>Test Embedded HTML File</h1></body></html>";
//            String objectName = "test_embedded";
//            String fileExtension = ".html";
//            int rowNo = 1;
//            int colNo = 1;
//
////            ByteArrayOutputStream firefoxIcon = new ByteArrayOutputStream();
//            //https://design.firefox.com/photon/visuals/product-identity-assets.html
////            URL iconURL = new URL("https://d33wubrfki0l68.cloudfront.net/06185f059f69055733688518b798a0feb4c7f160/9f07a/images/product-identity-assets/firefox.png");
////            Thread.currentThread().getContextClassLoader().getResourceAsStream("firefox.png").transferTo(excelIcon);
////            iconURL.openStream().transferTo(firefoxIcon);
//            InputStream stream = ResourceUtil.getStream("img.png");
//            final int iconId = wb.addPicture(IoUtil.readBytes(stream), XSSFWorkbook.PICTURE_TYPE_PNG);
//
//            final int oleIdx = wb.addOlePackage(content.getBytes(), objectName, objectName + fileExtension, objectName + fileExtension);
//            final Drawing<?> pat = sheet.createDrawingPatriarch();
//            final ClientAnchor anchor = pat.createAnchor(0, 0, 0, 0, colNo, rowNo, colNo + 1, rowNo + 2);//tweak cell ranges to minimize image distortion
//            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
//
//            final XSSFObjectData objectData = (XSSFObjectData) pat.createObjectData(anchor, oleIdx, iconId);
//            objectData.getCTShape().getNvSpPr().getCNvPr().setName(objectName);
//            objectData.getCTShape().getNvSpPr().getCNvPr().setHidden(false);
//            //objectData.getOleObject().setDvAspect(STDvAspect.DVASPECT_ICON);
//
//            wb.write(Files.newOutputStream(Paths.get("F:\\embedded-file-test.xlsx")));
//        } catch (Exception e) {
//            log.error("错误",e);
//        }
    }
}
