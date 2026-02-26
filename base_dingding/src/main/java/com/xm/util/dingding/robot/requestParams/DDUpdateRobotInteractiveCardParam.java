package com.xm.util.dingding.robot.requestParams;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@Data
public class DDUpdateRobotInteractiveCardParam {

    private String outTrackId;
    private CardDataDTO cardData;
    private PrivateDataDTO privateData;
    private int userIdType;
    private CardOptionsDTO cardOptions;

    @NoArgsConstructor
    @Data
    public static class CardDataDTO {
        private Map<String,String> cardParamMap;
        private Map<String,String> cardMediaIdParamMap;
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
    public static class CardOptionsDTO {
        private boolean updateCardDataByKey;
        private boolean updatePrivateDataByKey;
    }
}
