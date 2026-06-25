package com.myk.emotionalHole.service.impl;

import com.myk.emotionalHole.common.Result;
import com.myk.emotionalHole.entity.Content;
import com.myk.emotionalHole.entity.Hug;
import com.myk.emotionalHole.mapper.ContentMapper;
import com.myk.emotionalHole.mapper.HugMapper;
import com.myk.emotionalHole.service.HugService;
import com.myk.emotionalHole.service.RecommendationService;
import com.myk.emotionalHole.util.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抱抱服务实现类
 *
 * 提供送抱抱/取消、状态查询、批量查询功能
 * 特性：乐观更新、事务保证原子性
 */
@Service
public class HugServiceImpl implements HugService {

    @Resource
    private HugMapper hugMapper;

    @Resource
    private ContentMapper contentMapper;

    @Resource
    private RecommendationService recommendationService;

    /** 送抱抱：参数校验→查重→插入记录→原子更新抱抱数→记录推荐行为 */
    @Override
    @Transactional
    public Result<Void> addHug(Hug hug) {
        // 参数校验
        ExceptionUtils.checkNotNull(hug, "抱抱信息不能为空");
        ExceptionUtils.checkNotNull(hug.getTargetType(), "目标类型不能为空");
        ExceptionUtils.checkNotNull(hug.getTargetId(), "目标ID不能为空");
        ExceptionUtils.checkNotNull(hug.getAnonymousId(), "用户标识不能为空");
        ExceptionUtils.checkCondition(!hug.getAnonymousId().trim().isEmpty(), "用户标识不能为空");

        // 检查是否已经抱抱
        List<Hug> existingHugs = hugMapper.getHugsByTarget(hug.getTargetType(), hug.getTargetId());
        boolean alreadyHugged = existingHugs.stream()
                .anyMatch(existingHug -> existingHug.getAnonymousId().equals(hug.getAnonymousId()));
        if (alreadyHugged) {
            return Result.error(400, "已经送过抱抱了");
        }

        // 设置默认值
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        hug.setCreateTime(LocalDateTime.now().format(formatter));

        // 插入数据库
        int result = hugMapper.insertHug(hug);
        if (result <= 0) {
            throw ExceptionUtils.createServerException("抱抱失败，请重试");
        }

        // 原子更新对应内容的抱抱数
        int targetType = hug.getTargetType();
        Long targetId = hug.getTargetId();

        if (targetType == 1) {
            contentMapper.incrementHugCount(targetId, 1);
            recommendationService.recordBehavior(hug.getAnonymousId(), targetId, 4);
        }

        return Result.success();
    }

    /** 取消抱抱：删除记录→原子递减抱抱数→记录取消行为 */
    @Override
    @Transactional
    public Result<Void> removeHug(int targetType, Long targetId, String anonymousId) {
        // 参数校验
        ExceptionUtils.checkNotNull(targetId, "目标ID不能为空");
        ExceptionUtils.checkNotNull(anonymousId, "用户标识不能为空");
        ExceptionUtils.checkCondition(!anonymousId.trim().isEmpty(), "用户标识不能为空");

        // 执行删除
        int result = hugMapper.deleteHug(targetType, targetId, anonymousId);
        if (result <= 0) {
            return Result.error(400, "取消抱抱失败，可能尚未送过抱抱");
        }

        // 原子更新对应内容的抱抱数
        if (targetType == 1) {
            contentMapper.incrementHugCount(targetId, -1);
            recommendationService.recordBehavior(anonymousId, targetId, -4);
        }

        return Result.success();
    }

    /** 查询当前用户是否已对目标送过抱抱 */
    @Override
    @Transactional(readOnly = true)
    public Result<Boolean> checkHugStatus(int targetType, Long targetId, String anonymousId) {
        try {
            // 参数校验
            ExceptionUtils.checkNotNull(targetId, "目标ID不能为空");
            ExceptionUtils.checkNotNull(anonymousId, "用户标识不能为空");
            ExceptionUtils.checkCondition(!anonymousId.trim().isEmpty(), "用户标识不能为空");

            // 查询抱抱记录
            List<Hug> hugs = hugMapper.getHugsByTarget(targetType, targetId);
            boolean hugged = hugs.stream()
                    .anyMatch(hug -> hug.getAnonymousId().equals(anonymousId));

            return Result.success(hugged);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /** 获取目标内容的抱抱总数 */
    @Override
    @Transactional(readOnly = true)
    public Result<Integer> getHugCount(int targetType, Long targetId) {
        try {
            // 参数校验
            ExceptionUtils.checkNotNull(targetId, "目标ID不能为空");

            // 查询抱抱记录并统计数量
            List<Hug> hugs = hugMapper.getHugsByTarget(targetType, targetId);
            int count = hugs.size();

            return Result.success(count);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /** 分页获取当前用户送过抱抱的内容列表 */
    @Override
    @Transactional(readOnly = true)
    public Result<List<Content>> getMyHugs(int page, int pageSize, String anonymousId) {
        try {
            // 参数校验
            ExceptionUtils.checkNotNull(anonymousId, "用户标识不能为空");
            ExceptionUtils.checkCondition(!anonymousId.trim().isEmpty(), "用户标识不能为空");
            ExceptionUtils.checkCondition(page > 0, "页码必须大于0");
            ExceptionUtils.checkCondition(pageSize > 0 && pageSize <= 100, "每页大小必须在1-100之间");

            // 计算偏移量
            int offset = (page - 1) * pageSize;

            // 查询用户抱抱的内容列表
            List<Content> contentList = hugMapper.getMyHugs(offset, pageSize, anonymousId);

            return Result.success(contentList);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /** 批量查询多个目标的抱抱状态，减少列表页API调用次数 */
    @Override
    @Transactional(readOnly = true)
    public Result<Map<Long, Boolean>> batchCheckHugStatus(int targetType, List<Long> targetIds, String anonymousId) {
        try {
            // 参数校验
            ExceptionUtils.checkNotNull(targetIds, "目标ID列表不能为空");
            ExceptionUtils.checkCondition(!targetIds.isEmpty(), "目标ID列表不能为空");
            ExceptionUtils.checkCondition(targetIds.size() <= 50, "单次查询最多50个目标");
            ExceptionUtils.checkNotNull(anonymousId, "用户标识不能为空");
            ExceptionUtils.checkCondition(!anonymousId.trim().isEmpty(), "用户标识不能为空");

            // 批量查询抱抱记录
            List<Hug> hugs = hugMapper.batchGetHugStatus(targetType, targetIds, anonymousId);

            // 构建结果映射
            Map<Long, Boolean> resultMap = new HashMap<>();
            for (Long targetId : targetIds) {
                resultMap.put(targetId, false);
            }
            for (Hug hug : hugs) {
                resultMap.put(hug.getTargetId(), true);
            }

            return Result.success(resultMap);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

    /** 批量获取多个目标的抱抱数量，用于列表页聚合展示 */
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Result<Map<Long, Integer>> batchGetHugCount(int targetType, List<Long> targetIds) {
        try {
            // 参数校验
            ExceptionUtils.checkNotNull(targetIds, "目标ID列表不能为空");
            ExceptionUtils.checkCondition(!targetIds.isEmpty(), "目标ID列表不能为空");
            ExceptionUtils.checkCondition(targetIds.size() <= 50, "单次查询最多50个目标");

            // 批量查询抱抱数量（MyBatis返回List<Map>）
            List<Map<String, Object>> resultList = hugMapper.batchGetHugCount(targetType, targetIds);

            // 构建结果映射
            Map<Long, Integer> hugCountMap = new HashMap<>();
            for (Long targetId : targetIds) {
                hugCountMap.put(targetId, 0);
            }
            
            // 遍历查询结果，填充数量
            for (Map<String, Object> row : resultList) {
                Long targetId = ((Number) row.get("targetId")).longValue();
                Integer count = ((Number) row.get("hugCount")).intValue();
                hugCountMap.put(targetId, count);
            }

            return Result.success(hugCountMap);
        } catch (Exception e) {
            return ExceptionUtils.toResult(e);
        }
    }

}
