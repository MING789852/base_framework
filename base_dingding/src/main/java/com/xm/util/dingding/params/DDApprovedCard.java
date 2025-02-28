package com.xm.util.dingding.params;

import cn.hutool.json.JSONUtil;
import com.xm.util.crypto.aes.AESUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Data
public class DDApprovedCard {
    //必填
    private String outTrackId;
    //发送时必填
    private List<String> receiverUserIdList;
    //需要回调操作必填
    private String callbackRouteKey;

    private List<ButtonListDTO> buttonList;
    private StatusDTO status;
    private String title;
    private String markdown;
    private CardDTO card;

    public Map<String,String> toLegalParameters(){
        Map<String,String> data=new HashMap<>();
        data.put("sys_full_json_obj",JSONUtil.toJsonStr(this));
        return data;
    }

    @NoArgsConstructor
    @Data
    public static class StatusDTO {
        private String name;
        private String color;
        private String value;

        public StatusDTO(String name, String color, String value) {
            this.name = name;
            this.color = color;
            this.value = value;
        }

        public static StatusDTO createApproval(){
            return new StatusDTO("审批中","blue","approval");
        }
        public static StatusDTO createReject(){
            return new StatusDTO("已拒绝","red","reject");
        }
        public static StatusDTO createSuccess(){
            return new StatusDTO("已批准","green","success");
        }
    }

    @NoArgsConstructor
    @Data
    public static class CardDTO {
        private boolean jumpFlag;
        private String jumpUrl;


    }

    public static enum ButtonTypeEnum{
        action,
        jump;
    }

    public static ButtonListDTO createJumpButton(String buttonName,String url){
        ButtonListDTO buttonListDTO = new ButtonListDTO();
        buttonListDTO.setButtonColor("blue");
        buttonListDTO.setButtonData(url);
        buttonListDTO.setButtonShow(true);
        buttonListDTO.setButtonName(buttonName);
        buttonListDTO.setButtonType(ButtonTypeEnum.jump.name());
        buttonListDTO.setButtonStatus("normal");
        return buttonListDTO;
    }

    public static ButtonListDTO createActionButton(String buttonName,String data){
        ButtonListDTO buttonListDTO = new ButtonListDTO();
        buttonListDTO.setButtonColor("blue");
        buttonListDTO.setButtonData(AESUtil.encrypt(data));
        buttonListDTO.setButtonShow(true);
        buttonListDTO.setButtonName(buttonName);
        buttonListDTO.setButtonType(ButtonTypeEnum.action.name());
        buttonListDTO.setButtonStatus("normal");
        return buttonListDTO;
    }

    @NoArgsConstructor
    @Data
    public static class ButtonListDTO {
        private String buttonName;
        private String buttonData;
        private boolean buttonShow;
        private String buttonColor;
        private String buttonStatus;
        private String buttonType;
    }
}
