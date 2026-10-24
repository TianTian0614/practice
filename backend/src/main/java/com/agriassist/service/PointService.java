package com.agriassist.service;

import java.util.List;
import java.util.Map;

public interface PointService {
    void award(Long userId, int delta, String reason, String refType, Long refId);
    Map<String, Object> myPoints(Long userId);
    List<Map<String, Object>> rank(int limit);

    static int level(int points) {
        if (points >= 500) return 4;
        if (points >= 200) return 3;
        if (points >= 50)  return 2;
        return 1;
    }

    static String levelName(int points) {
        if (points >= 500) return "助农达人";
        if (points >= 200) return "资深农户";
        if (points >= 50)  return "活跃农户";
        return "新手农户";
    }
}
