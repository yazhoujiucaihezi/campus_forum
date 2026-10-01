package com.box.vo;

import lombok.Data;

/** 帖子互动状态数据 */
@Data
public class TopicInteractVO {
    /** 当前用户是否点赞 */
    private Boolean like;
    /** 当前用户是否收藏 */
    private Boolean collect;
    /** 点赞总数 */
    private Integer likeCount;
    /** 收藏总数 */
    private Integer collectCount;
}