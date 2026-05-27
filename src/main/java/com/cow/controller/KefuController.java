package com.cow.controller;

import com.cow.entity.AiKefu;
import com.cow.entity.ChatMessage;
import com.cow.entity.ChatSession;
import com.cow.service.AiKefuService;
import com.cow.service.ChatSessionService;
import com.cow.util.general.WordFilter; // 導入敏感詞過濾工具
import org.springframework.beans.factory.annotation.Autowired;
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

        // 判断是否有匹配结果
        if (aiAnswer != null && !aiAnswer.trim().isEmpty()) {
            // 匹配到了正常业务问题的自动回复，由 AI (senderType=0) 发出
            chatService.sendMessage(sessionId, 0, aiAnswer);
        } else {
            // 没匹配到回复，直接下发给底層核心，底層會自動判定半小時內是否重復並阻斷入庫
            chatService.sendMessage(sessionId, 0, transferTip);
        }
    }
}