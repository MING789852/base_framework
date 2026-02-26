package com.xm.util.dingding.workMsg.requestParams;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class DDWorkMsgParam {

    private MsgDTO msg;
    private Boolean to_all_user;
    private String agent_id;
    private String dept_id_list;
    private String userid_list;

    @NoArgsConstructor
    @Data
    public static class MsgDTO {
        private VoiceDTO voice;
        private ImageDTO image;
        private OaDTO oa;
        private FileDTO file;
        private ActionCardDTO action_card;
        private LinkDTO link;
        private MarkdownDTO markdown;
        private TextDTO text;
        private String msgtype;

        public static class MsgType{
            public static String text="text";
            public static String image="image";
            public static String voice="voice";
            public static String file="file";
            public static String link="link";
            public static String oa="oa";
            public static String markdown="markdown";
            public static String action_card="action_card";
        }

        @NoArgsConstructor
        @Data
        public static class VoiceDTO {
            private String duration;
            private String media_id;
        }

        @NoArgsConstructor
        @Data
        public static class ImageDTO {
            private String media_id;
        }

        @NoArgsConstructor
        @Data
        public static class OaDTO {
            private HeadDTO head;
            private String pc_message_url;
            private StatusBarDTO status_bar;
            private BodyDTO body;
            private String message_url;

            @NoArgsConstructor
            @Data
            public static class HeadDTO {
                private String bgcolor;
                private String text;
            }

            @NoArgsConstructor
            @Data
            public static class StatusBarDTO {
                private String status_value;
                private String status_bg;
            }

            @NoArgsConstructor
            @Data
            public static class BodyDTO {
                private String file_count;
                private String image;
                private FormDTO form;
                private String author;
                private RichDTO rich;
                private String title;
                private String content;

                @NoArgsConstructor
                @Data
                public static class FormDTO {
                    private String value;
                    private String key;
                }

                @NoArgsConstructor
                @Data
                public static class RichDTO {
                    private String unit;
                    private String num;
                }
            }
        }

        @NoArgsConstructor
        @Data
        public static class FileDTO {
            private String media_id;
        }

        @NoArgsConstructor
        @Data
        public static class ActionCardDTO {
            private List<BtnJsonListDTO> btn_json_list;
            private String single_url;
            private String btn_orientation;
            private String single_title;
            private String markdown;
            private String title;

            @NoArgsConstructor
            @Data
            public static class BtnJsonListDTO {
                private String action_url;
                private String title;
            }
        }

        @NoArgsConstructor
        @Data
        public static class LinkDTO {
            private String picUrl;
            private String messageUrl;
            private String text;
            private String title;
        }

        @NoArgsConstructor
        @Data
        public static class MarkdownDTO {
            private String text;
            private String title;
        }

        @NoArgsConstructor
        @Data
        public static class TextDTO {
            private String content;
        }
    }
}
