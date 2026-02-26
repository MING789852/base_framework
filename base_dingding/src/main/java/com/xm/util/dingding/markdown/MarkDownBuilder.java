package com.xm.util.dingding.markdown;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

import java.util.*;
import java.util.function.Function;

/**
 * 注意：每个markdown格式操作之后都需要\n换行
 */
public class MarkDownBuilder {
    private StringBuilder value;

    private int version;

//    private final String green="#67C23A";
//    private final String red="#F56C6C";
//    private final String yellow="#E6A23C";

    public static MarkDownBuilder creat(int version) {
        MarkDownBuilder markDownBuilder = new MarkDownBuilder();
        markDownBuilder.value=new StringBuilder();
        markDownBuilder.version = version;
        return markDownBuilder;
    }

    private static String getSpecLineBreak(){
        //格式换行
        return  "\n\n";
    }

    private static String getEnBlank(){
        return "&nbsp;";
    }

    private static String getCnBlank(){
        return "&emsp;";
    }

    private static String getBrBreak(){
        return "<br>";
    }

//    private String getFontColorStart(String color){
//        return StrUtil.format("<font color=\"{}\">",color)+ getSpecLineBreak();
//    }
//
//    public String getFontColorEnd(){
//        return "</font>"+ getSpecLineBreak();
//    }


//    public MarkDownBuilder addGreenStart(){
//        this.value.append(getFontColorStart(green));
//        return this;
//    }
//
//    public MarkDownBuilder addGreenEnd(){
//        this.value.append(getFontColorEnd());
//        return this;
//    }
//
//
//    public MarkDownBuilder addRedStart(){
//        this.value.append(getFontColorStart(red));
//        return this;
//    }
//
//    public MarkDownBuilder addRedEnd(){
//        this.value.append(getFontColorEnd());
//        return this;
//    }
//
//
//    public MarkDownBuilder addYellowStart(){
//        this.value.append(getFontColorStart(yellow));
//        return this;
//    }
//
//    public MarkDownBuilder addYellowEnd(){
//        this.value.append(getFontColorEnd());
//        return this;
//    }

    public MarkDownBuilder addBlank(){
        this.value.append(getEnBlank());
        return this;
    }

    public MarkDownBuilder addBrBreak(){
        this.value.append(getBrBreak());
        return this;
    }

    public MarkDownBuilder addLineBreak(){
        this.value.append(getSpecLineBreak());
        return this;
    }

    public MarkDownBuilder addTitle(String title, int level){
        if (StrUtil.isBlank(title)){
            return this;
        }
        if (level<=0){
            level=1;
        }
        if (level>6){
            level=6;
        }
        String titleLevel=StrUtil.fillAfter("", '#',level);
        this.value.append(StrUtil.format("{} {}",titleLevel,title));
        return this;
    }

    public MarkDownBuilder addQuoteStart(){
        this.value.append("> ");
        return this;
    }


    public MarkDownBuilder addQuoteEnd(){
        this.value.append(StrUtil.format("{} {}", getSpecLineBreak(), getSpecLineBreak()));
        return this;
    }

    public MarkDownBuilder addBoldText(String boldText){
        if (StrUtil.isBlank(boldText)){
            return this;
        }
        boldText=boldText.trim();
        this.value.append(StrUtil.format(" **{}** ",boldText));
        return this;
    }

    public MarkDownBuilder addItalicText(String italicText){
        if (StrUtil.isBlank(italicText)){
            return this;
        }
        this.value.append(StrUtil.format("*{}*",italicText));
        return this;
    }

    public MarkDownBuilder addLink(String linkName, String linkUrl){
        if (StrUtil.isBlank(linkName)||StrUtil.isBlank(linkUrl)){
            return this;
        }
        this.value.append(StrUtil.format("[{}]({})",linkName,linkUrl));
        return this;
    }

    public MarkDownBuilder addImage(String imgUrl){
        if (StrUtil.isBlank(imgUrl)){
            return this;
        }
        this.value.append(StrUtil.format("![]({})",imgUrl));
        return this;
    }

    public MarkDownBuilder addUnorderedList(List<String> strList){
        if (CollectionUtil.isEmpty(strList)){
            return this;
        }
        for (String s:strList){
            this.value.append(StrUtil.format("- {}{}",s, getSpecLineBreak()));
        }
        return this;
    }

    public MarkDownBuilder addOrderedList(List<String> strList){
        if (CollectionUtil.isEmpty(strList)){
            return this;
        }
        for (int i = 0; i < strList.size() ; i++) {
            this.value.append(StrUtil.format("{}. {}{}",(i+1), strList.get(i), getSpecLineBreak()));
        }
        return this;
    }

    public MarkDownBuilder addDivider(){
        this.value.append("***");
        this.addLineBreak();
        return this;
    }

    public MarkDownBuilder addStrList(List<String> strList,boolean divider){
        if (CollectionUtil.isEmpty(strList)){
            return this;
        }
        if (!this.toString().endsWith(getSpecLineBreak())){
            this.addLineBreak();
        }
        for (String s:strList){
            this.value.append(StrUtil.format("{}{}",s, getSpecLineBreak()));
            if (divider){
                this.addDivider();
            }
        }
        return this;
    }

    public MarkDownBuilder addText(String text){
        if (StrUtil.isBlank(text)){
            return this;
        }
        this.value.append(text);
        return this;
    }

    private void setValue(StringBuilder stringBuilder){
        if (stringBuilder!=null){
            this.value=stringBuilder;
        }
    }

    private StringBuilder getValue(){
        return  this.value;
    }

    public String build(){
        return  this.value.toString();
    }

    public MarkDownBuilder generateWithMap(Map<String,Object> map){
        if (map==null||map.isEmpty()){
            return this;
        }
        if (!this.toString().endsWith(getSpecLineBreak())){
            this.addLineBreak();
        }
        for (Map.Entry<String, Object> entry: map.entrySet()){
            this.addBoldText(entry.getKey()).addText(" :").addBlank()
                    .addBoldText(Optional.ofNullable(entry.getValue()).map(Object::toString).orElse("")).addLineBreak();
        }
        return this;
    }

    public static String generateWithMapReturnString(Map<String,Object> map){
        if (map==null||map.isEmpty()){
            return "";
        }
        StringBuilder stringBuilder=new StringBuilder();
        int maxKeyLength = map.keySet().stream()
                .map(String::length)
                .max(Comparator.comparing(Function.identity())).orElse(4);
        for (Map.Entry<String, Object> entry: map.entrySet()){
            StringBuilder key= new StringBuilder(entry.getKey());
            if (key.length()!=maxKeyLength){
                int length =  maxKeyLength-key.length();
                for (int i=1;i<=length;i++){
                    key.insert(0, getCnBlank());
                }
            }
            stringBuilder.append(key).append(getCnBlank()).append(":").append(getCnBlank()).append(entry.getValue()).append(getBrBreak());
        }
        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        String s=MarkDownBuilder.creat(2)
                .addTitle("一级标题",1).addLineBreak()
                .addTitle("二级标题",2).addLineBreak()
                .addTitle("三级标题",3).addLineBreak()
                .addTitle("四级标题",4).addLineBreak()
                .addTitle("五级标题",5).addLineBreak()
//                .addYellowStart()
                .addBoldText("\uD83D\uDD14加粗").addLineBreak()
                .addItalicText("❗ 斜体").addLineBreak()
//                .addYellowEnd()
//                .addRedStart()
                .addQuoteStart().addText("这是一段引用").addQuoteEnd()
                .addLink("链接地址","https://www.baidu.com/").addLineBreak()
                .addImage("https://img2.baidu.com/it/u=3976722208,1729629707&fm=253&app=120&size=w931&n=0&f=JPEG&fmt=auto?sec=1716742800&t=d425bbe7fa26a87105677cd5fbd44f5a").addLineBreak()
                .addText("无序列表").addLineBreak()
                .addUnorderedList(Arrays.asList("item1","item2","item3")).addLineBreak()
                .addText("有序列表").addLineBreak()
                .addOrderedList(Arrays.asList("item1","item2","item3")).addLineBreak()
//                .addRedEnd()
                .build();
        System.out.println(s);
    }
}
