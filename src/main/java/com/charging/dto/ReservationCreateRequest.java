package com.charging.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationCreateRequest {

    @NotNull(message = "充电桩ID不能为空")
    private Long pileId;

    @NotNull(message = "预约开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "预约结束时间不能为空")
    private LocalDateTime endTime;
}
