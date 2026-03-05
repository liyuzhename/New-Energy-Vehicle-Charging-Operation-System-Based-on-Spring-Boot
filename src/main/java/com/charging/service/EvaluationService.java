package com.charging.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.dto.EvaluationCreateRequest;
import com.charging.dto.EvaluationReplyRequest;
import com.charging.vo.EvaluationVO;

public interface EvaluationService {

    void create(Long userId, EvaluationCreateRequest request);

    Page<EvaluationVO> listByStation(Long stationId, int page, int size);

    void reply(Long operatorId, Long evaluationId, EvaluationReplyRequest request);

    void hide(Long evaluationId);
}
