package com.charging.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.charging.entity.FaultRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface FaultRecordMapper extends BaseMapper<FaultRecord> {

    @Select("SELECT pile_id AS pileId, pile_no AS pileNo, COUNT(*) AS faultCount " +
            "FROM fault_record WHERE deleted = 0 GROUP BY pile_id, pile_no ORDER BY faultCount DESC")
    List<Map<String, Object>> selectFaultAnalysis();

    @Select("SELECT pile_id AS pileId, pile_no AS pileNo, COUNT(*) AS faultCount " +
            "FROM fault_record f JOIN charging_pile p ON f.pile_id = p.id " +
            "WHERE f.deleted = 0 AND p.operator_id = #{operatorId} " +
            "GROUP BY f.pile_id, f.pile_no ORDER BY faultCount DESC")
    List<Map<String, Object>> selectFaultAnalysisByOperator(@Param("operatorId") Long operatorId);
}
