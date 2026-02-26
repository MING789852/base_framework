package com.xm.util.excel.params.img;

import cn.hutool.core.collection.CollectionUtil;
import com.xm.util.excel.params.MergedInfo;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.Units;

import java.util.*;

@Data
@RequiredArgsConstructor
public class ExcelImgAverage implements ExcelImgIndexInterface {

    private final List<ExcelImgData> imgDataList;
    //平均分成几行几列
    private final int row;
    private final int col;

    @Override
    public List<ExcelImgIndexResult> getImgIndexResult(ExcelImgIndexParams params) {
        if (CollectionUtil.isEmpty(imgDataList)){
            return new ArrayList<>();
        }
        if (imgDataList.size()>row*col){
            throw new RuntimeException("图片数量不能大于行数*列数");
        }
        List<ExcelImgIndexResult> imgIndexResultList = new ArrayList<>();
        int allWidth= params.getAllWidth();
        int allHeight= params.getAllHeight();
        int emuWidthAll = Units.columnWidthToEMU(allWidth);
        int emuHeightAll = Units.toEMU((double)allHeight/(double)20);
        MergedInfo mergedInfo = params.getMergedInfo();
        Map<Integer, Integer> colWidthMap;
        Map<Integer, Integer> rowHeightMap;
        int columnNum;
        int rowNum;
        if (mergedInfo==null){
            colWidthMap = new HashMap<>();
            colWidthMap.put(0,allWidth);
            columnNum = 1;
            rowHeightMap = new HashMap<>();
            rowHeightMap.put(0,allHeight);
            rowNum = 1;
        }else {
            colWidthMap = mergedInfo.getColWidthMap();
            columnNum = colWidthMap.size();
            rowHeightMap = mergedInfo.getRowHeightMap();
            rowNum = rowHeightMap.size();
        }
        int needWidthEmu=(int)((double)emuWidthAll/(double)col);
        int needHeightEmu=(int)((double)emuHeightAll/(double)row);
        int imgIndex = 0;

        Map<Integer,ExcelImgIndexResult> imgIndexResultMap = new HashMap<>();

        int scanRow = 0;
        int startPartRow = 0;
        int endPartRow = 0;
        int dy1=0;
        int dy2=0;
        int remainHeightEMU=0;
        //处理行占用
        for (int rowIndex=1;rowIndex<=row;rowIndex++){
            int occurHeightEmu = needHeightEmu;
            for (int i=scanRow;i<rowNum;i++){
                int rowHeightEMU;
                if (remainHeightEMU>0){
                    rowHeightEMU = remainHeightEMU;
                }else {
                    rowHeightEMU = Units.toEMU((double)rowHeightMap.get(i)/(double) 20);
                }
                if (rowHeightEMU > occurHeightEmu||i==rowNum-1){
                    if (startPartRow==endPartRow){
                        dy2=dy1+occurHeightEmu;
                    }else {
                        dy2=occurHeightEmu;
                    }
                    remainHeightEMU=rowHeightEMU-occurHeightEmu;
                    occurHeightEmu=0;
                } else {
                    occurHeightEmu = occurHeightEmu - rowHeightEMU;
                    remainHeightEMU=0;
                    endPartRow = i+1;
                }
                if (occurHeightEmu==0){
                    //根据剩余高度判断下次扫描的行索引
                    if (remainHeightEMU==0){
                        scanRow = i+1;
                    }else {
                        scanRow = i;
                    }
                    break;
                }
            }

            //处理列占用
            int scanColumn = 0;
            int startPartColumn = 0;
            int endPartColumn = 0;
            int dx1=0;
            int dx2=0;
            int remainWidthEMU=0;
            for (int colIndex=1;colIndex<=col;colIndex++){
                int occurWidthEmu = needWidthEmu;
                for (int j = scanColumn; j < columnNum; j++) {
                    int columnWidthEMU;
                    if (remainWidthEMU>0){
                        columnWidthEMU = remainWidthEMU;
                    }else {
                        columnWidthEMU = Units.columnWidthToEMU(colWidthMap.get(j));
                    }
                    if (columnWidthEMU > occurWidthEmu||j==columnNum-1) {
                        if (startPartColumn==endPartColumn){
                            dx2=dx1+occurWidthEmu;
                        }else {
                            dx2=occurWidthEmu;
                        }
                        remainWidthEMU=columnWidthEMU-occurWidthEmu;
                        occurWidthEmu=0;
                    } else {
                        occurWidthEmu = occurWidthEmu - columnWidthEMU;
                        remainWidthEMU=0;
                        endPartColumn=j+1;
                    }
                    if (occurWidthEmu==0){
                        //根据剩余宽度判断下次扫描的列索引
                        if (remainWidthEMU==0){
                            scanColumn = j+1;
                        }else {
                            scanColumn = j;
                        }
                        break;
                    }
                }
                ExcelImgIndexResult imgIndexResult = new ExcelImgIndexResult();
                imgIndexResult.setDx1(dx1);
                imgIndexResult.setDx2(dx2);
                imgIndexResult.setDy1(dy1);
                imgIndexResult.setDy2(dy2);
                imgIndexResult.setRow1(params.getStartRow() + startPartRow);
                imgIndexResult.setRow2(params.getStartRow() + endPartRow);
                imgIndexResult.setCol1(params.getStartColumn() + startPartColumn);
                imgIndexResult.setCol2(params.getStartColumn() + endPartColumn);
                imgIndexResultMap.put(imgIndex,imgIndexResult);

                if (remainWidthEMU==0){
                    dx1 = 0;
                    startPartColumn = endPartColumn+1;
                }else {
                    dx1 = dx2;
                    startPartColumn = endPartColumn;
                }
                imgIndex++;
            }

            if (remainHeightEMU==0){
                dy1 = 0;
                startPartRow = endPartRow+1;
            }else {
                startPartRow = endPartRow;
                dy1 = dy2;
            }
        }
        for (int index=0;index<imgDataList.size();index++) {
            ExcelImgIndexResult imgIndexResult = imgIndexResultMap.get(index);
            ExcelImgData imgData = imgDataList.get(index);
            int imgType;
            if ("jpeg".equalsIgnoreCase(imgData.getExtName()) || "jpg".equalsIgnoreCase(imgData.getExtName())) {
                imgType = Workbook.PICTURE_TYPE_JPEG;
            } else if ("png".equalsIgnoreCase(imgData.getExtName())) {
                imgType = Workbook.PICTURE_TYPE_PNG;
            } else {
                continue;
            }
            imgIndexResult.setImgType(imgType);
            imgIndexResult.setImgData(imgData.getData());
            imgIndexResultList.add(imgIndexResult);
        }
        return imgIndexResultList;
    }
}
