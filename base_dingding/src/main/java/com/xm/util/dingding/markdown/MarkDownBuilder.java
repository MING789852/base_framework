package com.xm.util.dingding.markdown;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Arrays;
import java.util.List;

public class MarkDownBuilder {
    private StringBuilder value;

    private int version;

    public static MarkDownBuilder creat(int version) {
        MarkDownBuilder markDownBuilder = new MarkDownBuilder();
        markDownBuilder.value=new StringBuilder();
        markDownBuilder.version = version;
        return markDownBuilder;
    }

    private String getLineBreak(){
        if (version==2){
            return  " \n\n ";
        }else {
            return  " <br> ";
        }
    }

    public MarkDownBuilder addBlank(){
        this.value.append(" &nbsp; ");
        return this;
    }

    public MarkDownBuilder addLineBreak(){
        this.value.append(getLineBreak());
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
        this.value.append(StrUtil.format("{} {}",getLineBreak(),getLineBreak()));
        return this;
    }

    public MarkDownBuilder addBoldText(String boldText){
        if (StrUtil.isBlank(boldText)){
            return this;
        }
        this.value.append(StrUtil.format("**{}**",boldText));
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
            this.value.append(StrUtil.format("- {}{}",s,getLineBreak()));
        }
        return this;
    }

    public MarkDownBuilder addOrderedList(List<String> strList){
        if (CollectionUtil.isEmpty(strList)){
            return this;
        }
        for (int i = 0; i < strList.size() ; i++) {
            this.value.append(StrUtil.format("{}. {}{}",(i+1), strList.get(i),getLineBreak()));
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

    public static void main(String[] args) {
        String s=MarkDownBuilder.creat(2)
                .addText("标题").addLineBreak()
                .addTitle("一级标题",1).addLineBreak()
                .addTitle("二级标题",2).addLineBreak()
                .addTitle("三级标题",3).addLineBreak()
                .addTitle("四级标题",4).addLineBreak()
                .addTitle("五级标题",5).addLineBreak()
                .addBoldText("加粗").addLineBreak()
                .addItalicText("斜体").addLineBreak()
                .addQuoteStart().addText("这是一段引用").addQuoteEnd()
                .addLink("链接地址","https://www.baidu.com/").addLineBreak()
                .addImage("https://img2.baidu.com/it/u=3976722208,1729629707&fm=253&app=120&size=w931&n=0&f=JPEG&fmt=auto?sec=1716742800&t=d425bbe7fa26a87105677cd5fbd44f5a").addLineBreak()
                .addText("无序列表").addLineBreak()
                .addUnorderedList(Arrays.asList("item1","item2","item3")).addLineBreak()
                .addText("有序列表").addLineBreak()
                .addOrderedList(Arrays.asList("item1","item2","item3")).addLineBreak().build();
        System.out.println(s);
    }
}
