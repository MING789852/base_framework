package com.xm.flowable.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TransferUser implements Serializable {
    private String actId;
    private String userId;
    private String transferUserId;

    public TransferUser(String actId,String userId,String transferUserId) {
        this.actId = actId;
        this.userId = userId;
        this.transferUserId=transferUserId;
    }
}
