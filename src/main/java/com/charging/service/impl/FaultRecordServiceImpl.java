package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.exception.BusinessException;
import com.charging.dto.FaultCreateRequest;
import com.charging.dto.FaultHandleRequest;
import com.charging.entity.ChargingPile;
import com.charging.entity.FaultRecord;
import com.charging.mapper.ChargingPileMapper;
import com.charging.mapper.FaultRecordMapper;
import com.charging.service.FaultRecordService;
import com.charging.vo.FaultRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FaultRecordServiceImpl implements FaultRecordService {

    private final FaultRecordMapper faultRecordMapper;
    private final ChargingPileMapper pileMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Long userId, FaultCreateRequest request) {
        ChargingPile pile = pileMapper.selectOne(
                new LambdaQueryWrapper<ChargingPile>().eq(ChargingPile::getPileNo, request.getPileNo()));
        if (pile == null) throw new BusinessException(404, "充电桩编号不存在");

        FaultRecord record = new FaultRecord();
        record.setUserId(userId);
        record.setPileId(pile.getId());
        record.setPileNo(request.getPileNo());
        record.setDescription(request.getDescription());
        record.setStatus("PENDING");
        faultRecordMapper.insert(record);

        pile.setStatus("FAULT");
        pileMapper.updateById(pile);
    }

    @Override
    public List<FaultRecordVO> listMy(Long userId) {
        return faultRecordMapper.selectList(
                new LambdaQueryWrapper<FaultRecord>()
                        .eq(FaultRecord::getUserId, userId)
                        .orderByDesc(FaultRecord::getCreateTime))
                .stream().map(this::toVO).toList();
    }

    @Override
    public Page<FaultRecordVO> listForOperator(Long operatorId, String status, LocalDate startDate, LocalDate endDate, int page, int size) {
        List<Long> pileIds = pileMapper.selectList(
                new LambdaQueryWrapper<ChargingPile>().eq(ChargingPile::getOperatorId, operatorId))
                .stream().map(ChargingPile::getId).toList();
        if (pileIds.isEmpty()) return new Page<>(page, size);

        LambdaQueryWrapper<FaultRecord> wrapper = new LambdaQueryWrapper<FaultRecord>()
                .in(FaultRecord::getPileId, pileIds)
                .orderByDesc(FaultRecord::getCreateTime);
        if (status != null && !status.isEmpty()) wrapper.eq(FaultRecord::getStatus, status);
        if (startDate != null) wrapper.ge(FaultRecord::getCreateTime, startDate.atStartOfDay());
        if (endDate != null) wrapper.le(FaultRecord::getCreateTime, endDate.atTime(23, 59, 59));
        return toVoPage(faultRecordMapper.selectPage(new Page<>(page, size), wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(Long operatorId, Long faultId, FaultHandleRequest request) {
        FaultRecord record = faultRecordMapper.selectById(faultId);
        if (record == null) throw new BusinessException(404, "故障单不存在");
        if (!Arrays.asList("PROCESSING", "REPAIRED").contains(request.getStatus())) {
            throw new BusinessException(400, "无效的处理状态");
        }
        ChargingPile pile = pileMapper.selectById(record.getPileId());
        if (pile == null || !pile.getOperatorId().equals(operatorId)) {
            throw new BusinessException(403, "无权处理该故障单");
        }
        record.setStatus(request.getStatus());
        if (request.getHandleNote() != null) record.setHandleNote(request.getHandleNote());
        faultRecordMapper.updateById(record);

        if ("REPAIRED".equals(request.getStatus())) {
            pile.setStatus("IDLE");
            pileMapper.updateById(pile);
        }
    }

    @Override
    public Page<FaultRecordVO> listForAdmin(String status, LocalDate startDate, LocalDate endDate, int page, int size) {
        LambdaQueryWrapper<FaultRecord> wrapper = new LambdaQueryWrapper<FaultRecord>()
                .orderByDesc(FaultRecord::getCreateTime);
        if (status != null && !status.isEmpty()) wrapper.eq(FaultRecord::getStatus, status);
        if (startDate != null) wrapper.ge(FaultRecord::getCreateTime, startDate.atStartOfDay());
        if (endDate != null) wrapper.le(FaultRecord::getCreateTime, endDate.atTime(23, 59, 59));
        return toVoPage(faultRecordMapper.selectPage(new Page<>(page, size), wrapper));
    }

    private Page<FaultRecordVO> toVoPage(Page<FaultRecord> page) {
        Page<FaultRecordVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    private FaultRecordVO toVO(FaultRecord r) {
        FaultRecordVO vo = new FaultRecordVO();
        BeanUtils.copyProperties(r, vo);
        return vo;
    }
}
