package com.agriassist.service.impl;

import com.agriassist.common.ErrorCode;
import com.agriassist.entity.User;
import com.agriassist.exception.BusinessException;
import com.agriassist.repository.PointLogRepository;
import com.agriassist.repository.UserRepository;
import com.agriassist.entity.PointLog;
import com.agriassist.service.PointService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PointServiceImpl implements PointService {

    private final UserRepository userRepository;
    private final PointLogRepository pointLogRepository;

    public PointServiceImpl(UserRepository userRepository, PointLogRepository pointLogRepository) {
        this.userRepository = userRepository;
        this.pointLogRepository = pointLogRepository;
    }

    @Override
    @Transactional
    public void award(Long userId, int delta, String reason, String refType, Long refId) {
        User u = userRepository.findById(userId).orElse(null);
        if (u == null) return;
        int base = u.getPoints() == null ? 0 : u.getPoints();
        u.setPoints(base + delta);
        userRepository.save(u);

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
        List<User> users = userRepository.findTopUsers(Pageable.ofSize(Math.max(1, limit)));
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
}
