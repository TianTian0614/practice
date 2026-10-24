package com.agriassist.service.impl;

import com.agriassist.common.ErrorCode;
import com.agriassist.entity.PointLog;
import com.agriassist.entity.User;
import com.agriassist.exception.BusinessException;
import com.agriassist.repository.PointLogRepository;
import com.agriassist.repository.UserRepository;
import com.agriassist.service.PointService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PointServiceImpl implements PointService {

    private final UserRepository userRepository;
    private final PointLogRepository pointLogRepository;
    private final StringRedisTemplate redisTemplate;

    // 积分排行榜在 Redis 中的 ZSet key；member=userId，score=积分
    private static final String RANK_KEY = "agri:rank:points";

    public PointServiceImpl(UserRepository userRepository, PointLogRepository pointLogRepository, StringRedisTemplate redisTemplate) {
        this.userRepository = userRepository;
        this.pointLogRepository = pointLogRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
    public void award(Long userId, int delta, String reason, String refType, Long refId) {
        User u = userRepository.findById(userId).orElse(null);
        if (u == null) return;
        int base = u.getPoints() == null ? 0 : u.getPoints();
        u.setPoints(base + delta);
        userRepository.save(u);

        // 同步更新 Redis 排行榜积分（原子递增；失败不影响主流程）
        try {
            redisTemplate.opsForZSet().incrementScore(RANK_KEY, String.valueOf(userId), delta);
        } catch (Exception ignored) {
            // Redis 不可用时静默降级，下次查询会从 DB 预热
        }

        PointLog log = new PointLog();
        log.setUserId(userId);
        log.setDelta(delta);
        log.setReason(reason);
        log.setRefType(refType);
        log.setRefId(refId);
        pointLogRepository.save(log);
    }

    @Override
    public Map<String, Object> myPoints(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
        int p = u.getPoints() == null ? 0 : u.getPoints();
        List<PointLog> logs = pointLogRepository.findByUserIdOrderByCreateTimeDesc(userId);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("points", p);
        m.put("level", PointService.level(p));
        m.put("levelName", PointService.levelName(p));
        m.put("logs", logs);
        return m;
    }

    @Override
    public List<Map<String, Object>> rank(int limit) {
        limit = Math.max(1, limit);
        try {
            // 优先走 Redis ZSet：按积分倒序取 Top-N
            Set<ZSetOperations.TypedTuple<String>> tuples =
                    redisTemplate.opsForZSet().reverseRangeWithScores(RANK_KEY, 0, limit - 1);
            if (tuples == null || tuples.isEmpty()) {
                // 首次或缓存为空：从 DB 全量预热（用户量小，可接受）
                warmUpFromDb();
                tuples = redisTemplate.opsForZSet().reverseRangeWithScores(RANK_KEY, 0, limit - 1);
            }
            if (tuples != null && !tuples.isEmpty()) {
                List<Long> ids = new ArrayList<>();
                Map<Long, Integer> scoreMap = new HashMap<>();
                for (ZSetOperations.TypedTuple<String> t : tuples) {
                    Long id = Long.valueOf(t.getValue());
                    ids.add(id);
                    scoreMap.put(id, t.getScore() == null ? 0 : t.getScore().intValue());
                }
                Map<Long, User> userMap = userRepository.findAllById(ids).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));
                List<Map<String, Object>> res = new ArrayList<>();
                for (Long id : ids) {
                    User u = userMap.get(id);
                    if (u == null) continue;
                    int p = scoreMap.get(id);
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", u.getId());
                    m.put("nickname", u.getNickname());
                    m.put("region", u.getRegion());
                    m.put("points", p);
                    m.put("level", PointService.level(p));
                    m.put("levelName", PointService.levelName(p));
                    res.add(m);
                }
                return res;
            }
        } catch (Exception e) {
            // Redis 不可用：降级到数据库查询
        }
        // 降级路径：直接查数据库
        List<User> users = userRepository.findTopUsers(Pageable.ofSize(limit));
        List<Map<String, Object>> res = new ArrayList<>();
        for (User u : users) {
            int p = u.getPoints() == null ? 0 : u.getPoints();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("nickname", u.getNickname());
            m.put("region", u.getRegion());
            m.put("points", p);
            m.put("level", PointService.level(p));
            m.put("levelName", PointService.levelName(p));
            res.add(m);
        }
        return res;
    }

    /** 从数据库全量把积分写入 Redis ZSet（预热/重建缓存） */
    private void warmUpFromDb() {
        try {
            List<User> all = userRepository.findAll();
            for (User u : all) {
                int p = u.getPoints() == null ? 0 : u.getPoints();
                redisTemplate.opsForZSet().add(RANK_KEY, String.valueOf(u.getId()), p);
            }
        } catch (Exception ignored) {
        }
    }
}
