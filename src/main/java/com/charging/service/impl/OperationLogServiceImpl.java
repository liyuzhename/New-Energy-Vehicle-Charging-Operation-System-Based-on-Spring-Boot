package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.entity.OperationLog;
import com.charging.mapper.OperationLogMapper;
import com.charging.service.OperationLogService;
import com.charging.vo.OperationLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    @Override
    public void save(OperationLog log) {
        operationLogMapper.insert(log);
    }

    @Override
    public Page<OperationLogVO> list(String operatorName, int page, int size) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<OperationLog>()
                .orderByDesc(OperationLog::getCreateTime);
        if (operatorName != null && !operatorName.isEmpty()) {
            wrapper.like(OperationLog::getOperatorName, operatorName);
        }
        Page<OperationLog> logPage = operationLogMapper.selectPage(new Page<>(page, size), wrapper);
        Page<OperationLogVO> voPage = new Page<>(logPage.getCurrent(), logPage.getSize(), logPage.getTotal());
        voPage.setRecords(logPage.getRecords().stream().map(l -> {
            OperationLogVO vo = new OperationLogVO();
            BeanUtils.copyProperties(l, vo);
            return vo;
        }).toList());
        return voPage;
    }
}
