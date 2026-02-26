package com.xm.util.dingding.robot.requestParams;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <a href="https://open.dingtalk.com/document/orgapp/custom-robots-send-group-messages">参数说明</a>
 */
@NoArgsConstructor
@Data
public class DDSendRobotToGroupMsgParam {

    @JsonProperty("at")
    private AtDTO at;
    @JsonProperty("link")
    private LinkDTO link;
    @JsonProperty("markdown")
    private MarkdownDTO markdown;
    @JsonProperty("feedCard")
    private FeedCardDTO feedCard;
    @JsonProperty("text")
    private TextDTO text;
    @JsonProperty("msgtype")
    private String msgtype;
    @JsonProperty("actionCard")
    private ActionCardDTO actionCard;

    @NoArgsConstructor
    @Data
    public static class AtDTO {
        @JsonProperty("isAtAll")
        private Boolean isAtAll;
        @JsonProperty("atUserIds")
        private List<String> atUserIds;
        @JsonProperty("atMobiles")
        private List<String> atMobiles;
    }

    @NoArgsConstructor
    @Data
    public static class LinkDTO {
        @JsonProperty("messageUrl")
        private String messageUrl;
        @JsonProperty("picUrl")
        private String picUrl;
        @JsonProperty("text")
        private String text;
        @JsonProperty("title")
        private String title;
    }

    @NoArgsConstructor
    @Data
    public static class MarkdownDTO {
        @JsonProperty("text")
        private String text;
        @JsonProperty("title")
        private String title;
    }

    @NoArgsConstructor
    @Data
    public static class FeedCardDTO {
        @JsonProperty("links")
        private LinksDTO links;

        @NoArgsConstructor
        @Data
        public static class LinksDTO {
            @JsonProperty("picURL")
            private String picURL;
            @JsonProperty("messageURL")
            private String messageURL;
            @JsonProperty("title")
            private String title;
        }
    }

    @NoArgsConstructor
    @Data
    public static class TextDTO {
        @JsonProperty("content")
        private String content;
    }

    @NoArgsConstructor
    @Data
    public static class ActionCardDTO {
        @JsonProperty("hideAvatar")
        private String hideAvatar;
        //消息内按钮排列方式。
        //0：按钮竖直排列
        //1：按钮横向排列
        @JsonProperty("btnOrientation")
        private String btnOrientation;
        @JsonProperty("singleTitle")
        private String singleTitle;
        @JsonProperty("btns")
        private List<BtnsDTO> btns;
        @JsonProperty("text")
        private String text;
        @JsonProperty("singleURL")
        private String singleURL;
        @JsonProperty("title")
        private String title;

        @NoArgsConstructor
        @Data
        public static class BtnsDTO {
            @JsonProperty("actionURL")
            private String actionURL;
            @JsonProperty("title")
            private String title;
        }
    }
}
