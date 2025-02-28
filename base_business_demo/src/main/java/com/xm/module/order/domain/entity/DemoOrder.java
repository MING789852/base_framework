package com.xm.module.order.domain.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class DemoOrder implements Serializable {
    @Size(max = 50,message = "最大长度要小于 50")
    @NotBlank(message = "不能为空")
    private String id;

    @Size(max = 50,message = "最大长度要小于 50")
    private String orderNo;

    @Size(max = 50,message = "最大长度要小于 50")
    private String orderName;

    private BigDecimal orderPrice;

    private Date createDate;

    private Date updateDate;

    @Size(max = 50,message = "最大长度要小于 50")
    private String createUser;

    @Size(max = 50,message = "最大长度要小于 50")
    private String updateUser;

    private static final long serialVersionUID = 1L;
}