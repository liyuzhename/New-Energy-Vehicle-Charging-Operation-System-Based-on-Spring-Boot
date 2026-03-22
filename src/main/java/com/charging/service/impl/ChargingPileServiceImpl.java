package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.exception.BusinessException;
import com.charging.dto.GunCreateRequest;
import com.charging.dto.PileCreateRequest;
import com.charging.dto.PileStatusRequest;
import com.charging.dto.PileUpdateRequest;
import com.charging.entity.ChargingGun;
import com.charging.entity.ChargingPile;
import com.charging.entity.ChargingStation;
import com.charging.mapper.ChargingGunMapper;
import com.charging.mapper.ChargingPileMapper;
import com.charging.mapper.ChargingStationMapper;
import com.charging.service.ChargingPileService;
import com.charging.vo.GunVO;
import com.charging.vo.PileVO;
import com.charging.vo.PileWithGunsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChargingPileServiceImpl implements ChargingPileService {

    private final ChargingPileMapper chargingPileMapper;
    private final ChargingGunMapper chargingGunMapper;
    private final ChargingStationMapper chargingStationMapper;

    private static final List<String> ALLOWED_STATUS = Arrays.asList("IDLE", "FAULT", "OFFLINE");

    @Override
    public List<PileWithGunsVO> listByStation(Long stationId) {
        List<ChargingPile> piles = chargingPileMapper.selectList(
                new LambdaQueryWrapper<ChargingPile>()
                        .eq(ChargingPile::getStationId, stationId)
                        .orderByAsc(ChargingPile::getPileNo)
        );
        return piles.stream().map(pile -> {
            PileWithGunsVO vo = new PileWithGunsVO();
            BeanUtils.copyProperties(pile, vo);
            vo.setGuns(listGuns(pile.getId()));
            return vo;
        }).toList();
    }

    @Override
    public Page<PileVO> listForOperator(Long operatorId, Long stationId, String keyword, int page, int size) {
        Page<ChargingPile> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ChargingPile> wrapper = new LambdaQueryWrapper<ChargingPile>()
                .eq(ChargingPile::getOperatorId, operatorId)
                .orderByDesc(ChargingPile::getCreateTime);
        if (stationId != null) {
            wrapper.eq(ChargingPile::getStationId, stationId);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(ChargingPile::getPileNo, keyword)
                    .or().like(ChargingPile::getPileType, keyword));
        }
        Page<ChargingPile> pilePage = chargingPileMapper.selectPage(pageParam, wrapper);

        Page<PileVO> voPage = new Page<>(pilePage.getCurrent(), pilePage.getSize(), pilePage.getTotal());
        voPage.setRecords(pilePage.getRecords().stream().map(p -> {
            PileVO vo = new PileVO();
            BeanUtils.copyProperties(p, vo);
            if (p.getStationId() != null) {
                ChargingStation station = chargingStationMapper.selectById(p.getStationId());
                if (station != null) {
                    vo.setStationName(station.getName());
                }
            }
            return vo;
        }).toList());
        return voPage;
    }

    @Override
    public void create(Long operatorId, PileCreateRequest request) {
        long count = chargingPileMapper.selectCount(
                new LambdaQueryWrapper<ChargingPile>().eq(ChargingPile::getPileNo, request.getPileNo())
        );
        if (count > 0) {
            throw new BusinessException(400, "桩编号已存在");
        }
        ChargingPile pile = new ChargingPile();
        BeanUtils.copyProperties(request, pile);
        pile.setOperatorId(operatorId);
        // 若前端传入了合法的初始状态则使用，否则默认 IDLE
        String initStatus = request.getStatus();
        if (initStatus != null && ALLOWED_STATUS.contains(initStatus)) {
            pile.setStatus(initStatus);
        } else {
            pile.setStatus("IDLE");
        }
        chargingPileMapper.insert(pile);
    }

    @Override
    public void update(Long operatorId, Long pileId, PileUpdateRequest request) {
        ChargingPile pile = getOwnedPile(operatorId, pileId);
        if (request.getPileNo() != null && !request.getPileNo().equals(pile.getPileNo())) {
            long count = chargingPileMapper.selectCount(
                    new LambdaQueryWrapper<ChargingPile>().eq(ChargingPile::getPileNo, request.getPileNo())
            );
            if (count > 0) {
                throw new BusinessException(400, "桩编号已存在");
            }
            pile.setPileNo(request.getPileNo());
        }
        if (request.getStationId() != null && !request.getStationId().equals(pile.getStationId())) {
            ChargingStation station = chargingStationMapper.selectById(request.getStationId());
            if (station == null) {
                throw new BusinessException(404, "目标充电站不存在");
            }
            if (!station.getOperatorId().equals(operatorId)) {
                throw new BusinessException(403, "无权将充电桩移至该充电站");
            }
            pile.setStationId(request.getStationId());
        }
        if (request.getPileType() != null) pile.setPileType(request.getPileType());
        if (request.getPower() != null) pile.setPower(request.getPower());
        chargingPileMapper.updateById(pile);
    }

    @Override
    public void delete(Long operatorId, Long pileId) {
        getOwnedPile(operatorId, pileId);
        chargingPileMapper.deleteById(pileId);
    }

    @Override
    public void updateStatus(Long operatorId, Long pileId, PileStatusRequest request) {
        if (!ALLOWED_STATUS.contains(request.getStatus())) {
            throw new BusinessException(400, "只允许切换为 IDLE / FAULT / OFFLINE 状态");
        }
        ChargingPile pile = getOwnedPile(operatorId, pileId);
        pile.setStatus(request.getStatus());
        chargingPileMapper.updateById(pile);
    }

    @Override
    public void addGun(Long operatorId, GunCreateRequest request) {
        ChargingPile pile = getOwnedPile(operatorId, request.getPileId());
        ChargingGun gun = new ChargingGun();
        BeanUtils.copyProperties(request, gun);
        gun.setStatus("IDLE");
        chargingGunMapper.insert(gun);
    }

    @Override
    public void deleteGun(Long operatorId, Long gunId) {
        ChargingGun gun = chargingGunMapper.selectById(gunId);
        if (gun == null) {
            throw new BusinessException(404, "充电枪不存在");
        }
        getOwnedPile(operatorId, gun.getPileId());
        chargingGunMapper.deleteById(gunId);
    }

    @Override
    public List<GunVO> listGuns(Long pileId) {
        List<ChargingGun> guns = chargingGunMapper.selectList(
                new LambdaQueryWrapper<ChargingGun>()
                        .eq(ChargingGun::getPileId, pileId)
                        .orderByAsc(ChargingGun::getGunNo)
        );
        return guns.stream().map(g -> {
            GunVO vo = new GunVO();
            BeanUtils.copyProperties(g, vo);
            return vo;
        }).toList();
    }

    private ChargingPile getOwnedPile(Long operatorId, Long pileId) {
        ChargingPile pile = chargingPileMapper.selectById(pileId);
        if (pile == null) {
            throw new BusinessException(404, "充电桩不存在");
        }
        if (!pile.getOperatorId().equals(operatorId)) {
            throw new BusinessException(403, "无权操作该充电桩");
        }
        return pile;
    }
}
