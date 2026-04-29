package com.cow.service.impl;

import com.cow.dao.AiKefuDao;
import com.cow.entity.AiKefu;
import com.cow.service.AiKefuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @email QQ550080747
 * @date 2025/04/28
 * @description 智能客服业务逻辑
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AiKefuServiceImpl implements AiKefuService {
    @Autowired
    private AiKefuDao aiKefuDao;

    @Override
    public AiKefu selectById(Integer aiId) {
        return aiKefuDao.selectById(aiId);
    }

    @Override
    public List<AiKefu> selectAll() {
        return aiKefuDao.selectAll();
    }

    @Override
    public List<String> selectAllQuestion() {
        return aiKefuDao.selectAllQuestion();
    }

    @Override
    public Boolean existsWithQuestionName(Integer aiId, String aiQuestion) {
        return aiKefuDao.existsWithQuestionName(aiId, aiQuestion);
    }

    @Override
    public Boolean insertData(AiKefu aiKefu) {
        return aiKefuDao.insertData(aiKefu);
    }

    @Override
    public Boolean updateById(AiKefu aiKefu) {
        return aiKefuDao.updateById(aiKefu);
    }

    @Override
    public Boolean deleteById(Integer aiId) {
        return aiKefuDao.deleteById(aiId);
    }
}