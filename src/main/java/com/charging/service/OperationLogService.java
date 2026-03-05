package com.charging.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.entity.OperationLog;
import com.charging.vo.OperationLogVO;

public interface OperationLogService {

    void save(OperationLog log);

    Page<OperationLogVO> list(String operatorName, int page, int size);
}
