package com.cow.controller;

import com.cow.entity.AiKefu;
import com.cow.entity.ChatMessage;
import com.cow.entity.ChatSession;
import com.cow.service.AiKefuService;
import com.cow.service.ChatSessionService;
import com.cow.util.general.WordFilter; // 導入敏感詞過濾工具
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate; // 導入 Redis 模板
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/kefu")
public class KefuController {

    @Autowired
    private AiKefuService aiKefuService;

    @Autowired
    private ChatSessionService chatService;

    @Autowired
    private StringRedisTemplate redisTemplate; // 注入 Redis 模板

    // ================== AI 客服相關 ==================
    @PostMapping("/ai/list")
    public Map<String, Object> aiList() {
        Map<String, Object> map = new HashMap<>();
        List<AiKefu> list = aiKefuService.selectAll();
        map.put("code", 200);
        map.put("data", list);
        return map;
    }

    @PostMapping("/ai/add")
    public Map<String, Object> aiAdd(@RequestBody AiKefu aiKefu) {
        Map<String, Object> map = new HashMap<>();
        Boolean result = aiKefuService.insertData(aiKefu);
        if (result) {
            map.put("code", 200);
        } else {
            map.put("code", 500);
            map.put("message", "添加失败");
        }
        return map;
    }

    @PostMapping("/ai/update")
    public Map<String, Object> aiUpdate(@RequestBody AiKefu aiKefu) {
        Map<String, Object> map = new HashMap<>();
        Boolean result = aiKefuService.updateById(aiKefu);
        if (result) {
            map.put("code", 200);
        } else {
            map.put("code", 500);
            map.put("message", "修改失败");
        }
        return map;
    }

    @PostMapping("/ai/delete")
    public Map<String, Object> aiDelete(Integer aiId) {
        Map<String, Object> map = new HashMap<>();
        Boolean result = aiKefuService.deleteById(aiId);
        if (result) {
            map.put("code", 200);
        } else {
            map.put("code", 500);
            map.put("message", "删除失败");
        }
        return map;
    }

    @PostMapping("/ai/updateStatus")
    public Map<String, Object> updateStatus(Integer aiId, Integer status) {
        Map<String, Object> map = new HashMap<>();
        AiKefu aiKefu = new AiKefu();
        aiKefu.setAiId(aiId);
        aiKefu.setStatus(status == 1);
        Boolean result = aiKefuService.updateById(aiKefu);
        if (result) {
            map.put("code", 200);
        } else {
            map.put("code", 500);
            map.put("message", "状态更新失败");
        }
        return map;
    }

    // ================== 人工客服聊天接口 ==================
    @GetMapping("/chat/sessionList")
    public Map<String, Object> sessionList() {
        Map<String, Object> map = new HashMap<>();
        List<ChatSession> list = chatService.getSessionList();
        map.put("code", 200);
        map.put("data", list);
        return map;
    }

    @GetMapping("/chat/msgList")
    public Map<String, Object> msgList(@RequestParam Long sessionId) {
        Map<String, Object> map = new HashMap<>();
        List<ChatMessage> list = chatService.getMessageList(sessionId);
        map.put("code", 200);
        map.put("data", list);
        return map;
    }

    @PostMapping("/chat/bindAdmin")
    public Map<String, Object> bindAdmin(@RequestBody Map<String, Object> param) {
        Long sessionId = Long.valueOf(param.get("sessionId").toString());
        chatService.bindAdmin(sessionId);
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("message", "绑定成功");
        return map;
    }

    @PostMapping("/chat/sendMsg")
    public Map<String, Object> sendMsg(@RequestBody Map<String, Object> param) {
        Long sessionId = Long.valueOf(param.get("sessionId").toString());
        Integer senderType = (Integer) param.get("senderType");
        String content = param.get("content").toString();

        // 【终极第一重拦截】：如果接收到的普通消息内容里本身就带有“转人工”字样，说明是前端刷出来的幽灵重放请求！
        // 这一步能从最上游拦截住前端错误传递 senderType 的大 Bug，保护底层 Service 不被无限透支
        if (content != null && content.contains("将转人工客服")) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", 200);
            map.put("message", "幽灵请求，静默过滤");
            return map;
        }

        // 整合修改：如果是使用者發送的訊息(senderType == 1)，自動將敏感詞替換為 *
        if (senderType != null && senderType == 1) {
            content = WordFilter.replaceWords(content);
        }

        // 1. 发送并保存当前用户发送的消息
        chatService.sendMessage(sessionId, senderType, content);

        // 2. 如果是用户发送的消息(senderType == 1)，进一步匹配 AI 自动回复
        if (senderType != null && senderType == 1) {
            tryTriggerAiReply(sessionId, content);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("message", "发送成功");
        return map;
    }

    @PostMapping("/chat/getOrCreateSession")
    public Map<String, Object> getOrCreateSession(@RequestBody Map<String, Object> param) {
        Map<String, Object> map = new HashMap<>();
        Long userId = Long.valueOf(param.get("userId").toString());
        Long sessionId = chatService.getOrCreateSession(userId);
        map.put("code", 200);
        map.put("data", sessionId);
        return map;
    }

    // ================== AI 自动回复分配 ==================
    private void tryTriggerAiReply(Long sessionId, String userContent) {
        String transferTip = "抱歉，这个问题我暂时不会，将转人工客服~";
        String redisLockKey = "lock:ai_kefu:transfer:session:" + sessionId;

        // 【终极第二重拦截】：在调知识库匹配前，先看看当前会话半小时内有没有锁
        // 如果半小时内发过转人工，这里直接 return 拦截，保持沉默，杜绝多线程时差穿透
        Boolean hasHitLock = redisTemplate.hasKey(redisLockKey);
        if (hasHitLock != null && hasHitLock) {
            return;
        }

        // 从知识库模糊匹配 AI 回复
        String aiAnswer = null;
        List<AiKefu> activeAiRules = aiKefuService.selectAll();
        if (activeAiRules != null) {
            for (AiKefu rule : activeAiRules) {
                if (rule.getStatus() != null && rule.getStatus() && userContent.contains(rule.getAiQuestion())) {
                    aiAnswer = rule.getAiAnswer();
                    break;
                }
            }
        }
        // 在 KefuController.java 的 tryTriggerAiReply 方法中
        if (aiAnswer != null && !aiAnswer.trim().isEmpty()) {
            chatService.sendMessage(sessionId, 0, aiAnswer); // 0代表系统回复
        } else {
            chatService.sendMessage(sessionId, 0, transferTip); // 0代表系统回复
        }
    }
}