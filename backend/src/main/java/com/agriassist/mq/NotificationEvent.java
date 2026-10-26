package com.agriassist.mq;

import java.io.Serializable;

/**
 * 系统通知事件：作为消息体在「审核/互动」等流程与「通知落库」之间解耦。
 * 通过 MQ 异步投递，无 broker 时由 {@link AsyncNotificationHandler} 降级到本地线程池执行。
 */
public class NotificationEvent implements Serializable {

    private Long userId;
    private String type;
    private String title;
    private String content;
    private String link;

    public NotificationEvent() {
    }

    public NotificationEvent(Long userId, String type, String title, String content, String link) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.content = content;
        this.link = link;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
