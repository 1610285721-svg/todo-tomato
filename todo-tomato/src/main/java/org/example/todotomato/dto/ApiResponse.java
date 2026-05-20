package org.example.todotomato.dto;

import lombok.Data;

/**
 * 统一响应格式 —— 所有接口返回的数据都包在这个类里
 * <p>
 * 为什么需要统一响应？
 * 前端每次收到数据，先看 code 判断成功还是失败。
 * 如果格式不统一，前端要写很多 if-else 来适配不同接口，很麻烦。
 * <p>
 * 泛型 <T>：T 可以是任何类型（Task、List<Task>、User 等），
 * 哪个接口调用就传哪个类型。比如：
 * - ApiResponse<Task>          → 返回单个任务
 * - ApiResponse<List<Task>>    → 返回任务列表
 * - ApiResponse<String>        → data 为空，只返回文字消息
 *
 * @param <T> data 字段的类型
 */
@Data  // Lombok：自动生成 getter、setter、toString、equals、hashCode（Spring 序列化 JSON 必须有 getter）
public class ApiResponse<T> {

    private int code;       // 状态码：200 成功，400 参数错误，401 未登录，500 服务器错误
    private String message; // 提示信息，如 "登录成功"、"任务标题不能为空"
    private T data;         // 实际数据，泛型 T 表示不限制类型。没有数据时填 null

    // 构造方法设为 private，强制通过静态工厂方法创建，保证格式统一
    // @Data 不会覆盖已存在的构造方法
    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ===== 以下是快捷创建方法，Service/Controller 里直接用 ApiResponse.success(xxx) 即可 =====

    /**
     * 成功（带数据）
     * 用法：ApiResponse.success(task)  或  ApiResponse.success(taskList)
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    /**
     * 成功（带自定义消息 + 数据）
     * 用法：ApiResponse.success("登录成功", loginResult)
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    /**
     * 失败
     * 用法：ApiResponse.error(400, "任务标题不能为空")
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
