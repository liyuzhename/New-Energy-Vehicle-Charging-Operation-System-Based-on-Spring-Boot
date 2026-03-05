package com.charging.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.dto.FaultCreateRequest;
import com.charging.dto.FaultHandleRequest;
import com.charging.vo.FaultRecordVO;

import java.util.List;

public interface FaultRecordService {

    void create(Long userId, FaultCreateRequest request);

    List<FaultRecordVO> listMy(Long userId);

    Page<FaultRecordVO> listForOperator(Long operatorId, String status, int page, int size);

    void handle(Long operatorId, Long faultId, FaultHandleRequest request);

    Page<FaultRecordVO> listForAdmin(String status, int page, int size);
}
