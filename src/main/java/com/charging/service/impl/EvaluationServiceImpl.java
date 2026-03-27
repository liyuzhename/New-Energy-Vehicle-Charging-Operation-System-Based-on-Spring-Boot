package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.exception.BusinessException;
import com.charging.dto.EvaluationCreateRequest;
import com.charging.dto.EvaluationReplyRequest;
import com.charging.entity.ChargingOrder;
import com.charging.entity.ChargingStation;
import com.charging.entity.Evaluation;
import com.charging.entity.User;
import com.charging.mapper.ChargingOrderMapper;
import com.charging.mapper.ChargingStationMapper;
import com.charging.mapper.EvaluationMapper;
import com.charging.mapper.UserMapper;
import com.charging.service.EvaluationService;
import com.charging.vo.EvaluationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationMapper evaluationMapper;
    private final ChargingOrderMapper orderMapper;
    private final ChargingStationMapper stationMapper;
    private final UserMapper userMapper;

    @Override
    public void create(Long userId, EvaluationCreateRequest request) {
        ChargingOrder order = orderMapper.selectById(request.getOrderId());
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (!order.getUserId().equals(userId)) throw new BusinessException(403, "无权评价该订单");
        if (!"PAID".equals(order.getPayStatus())) throw new BusinessException(400, "仅已支付订单可发表评价");

        long existing = evaluationMapper.selectCount(
                new LambdaQueryWrapper<Evaluation>().eq(Evaluation::getOrderId, request.getOrderId()));
        if (existing > 0) throw new BusinessException(400, "该订单已评价过");

        Evaluation evaluation = new Evaluation();
        evaluation.setUserId(userId);
        evaluation.setOrderId(request.getOrderId());
        evaluation.setStationId(order.getStationId());
        evaluation.setRating(request.getRating());
        evaluation.setContent(request.getContent());
        evaluation.setIsHidden(0);
        evaluationMapper.insert(evaluation);
    }

    @Override
    public Page<EvaluationVO> listByStation(Long stationId, int page, int size) {
        Page<Evaluation> pageResult = evaluationMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Evaluation>()
                        .eq(Evaluation::getStationId, stationId)
                        .eq(Evaluation::getIsHidden, 0)
                        .orderByDesc(Evaluation::getCreateTime));
        Double avgRating = evaluationMapper.selectAvgRating(stationId);
        Page<EvaluationVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        voPage.setRecords(pageResult.getRecords().stream().map(e -> {
            EvaluationVO vo = new EvaluationVO();
            BeanUtils.copyProperties(e, vo);
            vo.setAvgRating(avgRating != null ? avgRating : 0.0);
            // 关联查询用户昵称和头像
            if (e.getUserId() != null) {
                User user = userMapper.selectById(e.getUserId());
                if (user != null) {
                    vo.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                    vo.setUserAvatar(user.getAvatar());
                }
            }
            return vo;
        }).toList());
        return voPage;
    }

    @Override
    public Page<EvaluationVO> listAll(Integer rating, Integer isHidden, Long stationId, int page, int size) {
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<Evaluation>()
                .orderByDesc(Evaluation::getCreateTime);
        if (rating != null) wrapper.eq(Evaluation::getRating, rating);
        if (isHidden != null) wrapper.eq(Evaluation::getIsHidden, isHidden);
        if (stationId != null) wrapper.eq(Evaluation::getStationId, stationId);
        Page<Evaluation> pageResult = evaluationMapper.selectPage(new Page<>(page, size), wrapper);
        Page<EvaluationVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        voPage.setRecords(pageResult.getRecords().stream().map(e -> {
            EvaluationVO vo = new EvaluationVO();
            BeanUtils.copyProperties(e, vo);
            // 关联查询用户昵称和头像
            if (e.getUserId() != null) {
                User user = userMapper.selectById(e.getUserId());
                if (user != null) {
                    vo.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                    vo.setUserAvatar(user.getAvatar());
                }
            }
            // 关联查询充电站名称
            if (e.getStationId() != null) {
                ChargingStation station = stationMapper.selectById(e.getStationId());
                if (station != null) vo.setStationName(station.getName());
            }
            return vo;
        }).toList());
        return voPage;
    }

    @Override
    public void reply(Long operatorId, Long evaluationId, EvaluationReplyRequest request) {
        Evaluation evaluation = evaluationMapper.selectById(evaluationId);
        if (evaluation == null) throw new BusinessException(404, "评价不存在");
        ChargingStation station = stationMapper.selectById(evaluation.getStationId());
        if (station == null || !station.getOperatorId().equals(operatorId)) {
            throw new BusinessException(403, "无权回复该评价");
        }
        evaluation.setReply(request.getReply());
        evaluationMapper.updateById(evaluation);
    }

    @Override
    public void hide(Long evaluationId) {
        Evaluation evaluation = evaluationMapper.selectById(evaluationId);
        if (evaluation == null) throw new BusinessException(404, "评价不存在");
        evaluation.setIsHidden(1);
        evaluationMapper.updateById(evaluation);
    }
}
