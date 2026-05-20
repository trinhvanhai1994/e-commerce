package com.dragun.ecommerce.integration.meinvoice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeinvoicePagingRequest {

    @JsonProperty("Start")
    private int start;

    @JsonProperty("Length")
    private int length;

    @JsonProperty("Sort")
    private String sort;

    @JsonProperty("FromDate")
    private String fromDate;

    @JsonProperty("ToDate")
    private String toDate;

    /**
     * 0: chưa phát hành, 4: chờ cấp mã, 6: đã cấp mã, 7: từ chối cấp mã (theo tài liệu).
     */
    @JsonProperty("PublishStatus")
    private String publishStatus;
}
