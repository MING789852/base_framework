package com.xm.util.dingding.robot.requestParams;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Data
public class DDSendRobotInteractiveCardParam {
    private String cardTemplateId;
    private String openConversationId;
    private List<String> receiverUserIdList;
    private String outTrackId;
    private String robotCode;
    private Integer conversationType;
    private String callbackRouteKey;
    private CardDataDTO cardData;
    private PrivateDataDTO privateData;
    private String chatBotId;
    private Integer userIdType;
    private AtOpenIdsDTO atOpenIds;
    private CardOptionsDTO cardOptions;
    private String pullStrategy;

    @NoArgsConstructor
    @Data
    public static class CardDataDTO {
        private Map<String,String> cardParamMap;
        private Map<String, String> cardMediaIdParamMap;
    }

    @NoArgsConstructor
    @Data
    public static class PrivateDataDTO {
        private KeyDTO key;

        @NoArgsConstructor
        @Data
        public static class KeyDTO {
            private CardParamMapDTO cardParamMap;
            private CardMediaIdParamMapDTO cardMediaIdParamMap;

            @NoArgsConstructor
            @Data
            public static class CardParamMapDTO {
                private String key;
            }

            @NoArgsConstructor
            @Data
            public static class CardMediaIdParamMapDTO {
                private String key;
            }
        }
    }

    @NoArgsConstructor
    @Data
    public static class AtOpenIdsDTO {
        private String key;
    }

    @NoArgsConstructor
    @Data
    public static class CardOptionsDTO {
        private String supportForward;
    }
}
