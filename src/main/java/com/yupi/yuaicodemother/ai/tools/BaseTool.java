package com.yupi.yuaicodemother.ai.tools;

import cn.hutool.json.JSONObject;

/**
 * 工具基类
 * 定义所有工具的通用接口
 */
public abstract class BaseTool {

    /**
     * 获取工具的英文名称（对应方法名）
     *
     * @return 工具英文名称
     */
    public abstract String getToolName();

    /**
     * 获取工具的中文显示名称
     *
     * @return 工具中文名称
     */
    public abstract String getDisplayName();

    /**
     * 生成工具请求时的返回值（显示给用户）
     *
     * @return 工具请求显示内容
     */
    public String generateToolRequestResponse() {
        return String.format("\n\n[选择工具] %s\n\n", getDisplayName());
    }

    /**
     * 生成展示给前端 / 历史消息的工具执行结果
     * 默认优先使用工具真实执行结果，避免泄漏原始参数。
     *
     * @param arguments 工具执行参数
     * @param executionResult 工具真实执行结果
     * @return 格式化后的展示内容
     */
    public String generateToolExecutedDisplay(JSONObject arguments, String executionResult) {
        return executionResult == null ? "" : executionResult;
    }
} 
