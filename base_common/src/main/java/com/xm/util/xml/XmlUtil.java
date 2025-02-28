package com.xm.util.xml;

import cn.hutool.json.JSONObject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.util.CollectionUtils;

import java.io.StringReader;
import java.util.List;

public class XmlUtil {

    public static void main(String[] args) throws DocumentException {
//        JSONObject jsonObject=convertXMLtoJSON("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
//                "    <soap:Body>\n" +
//                "        <ns1:doCreateWorkflowRequestResponse xmlns:ns1=\"webservices.services.weaver.com.cn\">\n" +
//                "            <ns1:out>6804866</ns1:out>\n" +
//                "        </ns1:doCreateWorkflowRequestResponse>\n" +
//                "    </soap:Body>\n" +
//                "</soap:Envelope>");
    }


    public static JSONObject convertXMLtoJSON(String xml) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document=reader.read(new StringReader(xml));
        Element rootElement=document.getRootElement();
        JSONObject jsonObject=new JSONObject();
        if (rootElement!=null){
            String name=rootElement.getName();
            jsonObject.put(name,recursionElement(rootElement));
        }
        return jsonObject;
    }


    private static JSONObject recursionElement(Element element){
        if (element==null){
         return null;
        }
        List<Element> elementList=element.elements();
        if (CollectionUtils.isEmpty(elementList)){
            return null;
        }
        JSONObject jsonObject=new JSONObject();
        for (Element item:elementList){
            String itemName=item.getName();
            if (CollectionUtils.isEmpty(item.elements())){
                String itemText=item.getText();
                jsonObject.put(itemName,itemText);
            }else {
                jsonObject.put(itemName,recursionElement(item));
            }
        }
        return jsonObject;
    }
}
