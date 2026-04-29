package com.cow.util.recommend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RecommendationService {

    /**
     * 为用户推荐商品
     *
     * @param userId 用户ID
     * @param topN   推荐商品的数量
     * @param userItemMatrix 用户-商品交互矩阵
     * @return 推荐的商品列表
     */
    public List<String> recommendItemsForUser(String userId, int topN, Map<String, Map<String, Double>> userItemMatrix) {
        // 计算目标用户与其他用户的相似度
        Map<String, Double> userSimilarities = calculateUserSimilarities(userId, userItemMatrix);

        // 找到最相似的K个用户
        List<String> similarUsers = findTopKSimilarUsers(userSimilarities, topN);

        // 获取推荐商品
        List<String> recommendedItems = getRecommendedItems(userId, similarUsers, userItemMatrix);

        return recommendedItems;
    }

    /**
     * 计算用户相似度（基于余弦相似度）
     *
     * @param userId        目标用户ID
     * @param userItemMatrix 用户-商品交互矩阵
     * @return 用户相似度映射表
     */
    private Map<String, Double> calculateUserSimilarities(String userId, Map<String, Map<String, Double>> userItemMatrix) {
        Map<String, Double> similarities = new HashMap<>();
        Map<String, Double> targetUserInteractions = userItemMatrix.getOrDefault(userId, new HashMap<>());

        for (Map.Entry<String, Map<String, Double>> entry : userItemMatrix.entrySet()) {
            String otherUserId = entry.getKey();
            if (otherUserId.equals(userId)) continue; // 跳过目标用户

            Map<String, Double> otherUserInteractions = entry.getValue();
            double similarity = cosineSimilarity(targetUserInteractions, otherUserInteractions);
            similarities.put(otherUserId, similarity);
        }

        return similarities;
    }

    /**
     * 计算余弦相似度
     *
     * @param user1Interactions 用户1的交互数据
     * @param user2Interactions 用户2的交互数据
     * @return 余弦相似度值
     */
    private double cosineSimilarity(Map<String, Double> user1Interactions, Map<String, Double> user2Interactions) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        // 计算点积和范数
        for (Map.Entry<String, Double> entry : user1Interactions.entrySet()) {
            String itemId = entry.getKey();
            if (user2Interactions.containsKey(itemId)) {
                dotProduct += entry.getValue() * user2Interactions.get(itemId);
            }
            norm1 += Math.pow(entry.getValue(), 2);
        }

        for (Double value : user2Interactions.values()) {
            norm2 += Math.pow(value, 2);
        }

        // 计算余弦相似度
        if (norm1 == 0 || norm2 == 0) return 0.0;
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 找到最相似的K个用户
     *
     * @param userSimilarities 用户相似度映射表
     * @param topN             相似用户的数量
     * @return 最相似的K个用户的ID列表
     */
    private List<String> findTopKSimilarUsers(Map<String, Double> userSimilarities, int topN) {
        return userSimilarities.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 获取推荐商品
     *
     * @param userId           目标用户ID
     * @param similarUsers     相似用户列表
     * @param userItemMatrix   用户-商品交互矩阵
     * @return 推荐的商品列表
     */
    private List<String> getRecommendedItems(String userId, List<String> similarUsers, Map<String, Map<String, Double>> userItemMatrix) {
        Map<String, Double> recommendedItems = new HashMap<>();
        Map<String, Double> targetUserInteractions = userItemMatrix.getOrDefault(userId, new HashMap<>());

        for (String similarUserId : similarUsers) {
            Map<String, Double> similarUserInteractions = userItemMatrix.getOrDefault(similarUserId, new HashMap<>());

            for (Map.Entry<String, Double> entry : similarUserInteractions.entrySet()) {
                String itemId = entry.getKey();
                if (!targetUserInteractions.containsKey(itemId)) { // 排除用户已经交互过的商品（已经交互过的商品：都买过的商品）
                    recommendedItems.put(itemId, recommendedItems.getOrDefault(itemId, 0.0) + entry.getValue());
                }
            }
        }

        // 按推荐分数排序并返回前N个商品
        return recommendedItems.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());
    }
}