package com.charging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReservationCreateRequest {

    @NotNull(message = "充电桩ID不能为空")
    private Long pileId;

    @NotNull(message = "充电枪ID不能为空")
    private Long gunId;

    @NotNull(message = "预约开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "预约结束时间不能为空")
    private LocalDateTime endTime;
}
