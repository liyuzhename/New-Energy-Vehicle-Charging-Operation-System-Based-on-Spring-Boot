package com.charging.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.entity.OperationLog;
import com.charging.vo.OperationLogVO;

import java.time.LocalDate;

public interface OperationLogService {

    void save(OperationLog log);

    Page<OperationLogVO> list(String operatorName, String keyword, LocalDate startDate, LocalDate endDate, int page, int size);
}
