package fun.timu.shop.user.model.DO;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName user
 */
@TableName(value ="user")
@Data
public class UserDO implements Serializable {
    /**
     * 用户ID - 使用分布式ID生成器生成
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 昵称
     */
    private String name;

    /**
     * 密码
     */
    private String pwd;

    /**
     * 头像
     */
    private String headImg;

    /**
     * 用户签名
     */
    private String slogan;

    /**
     * 0表示女，1表示男
     */
    private Integer sex;

    /**
     * 积分
     */
    private Integer points;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 邮箱
     */
    private String mail;

    /**
     * 盐，用于个人敏感信息处理
     */
    private String secret;

    /**
     * 删除标记：0->未删除；1->已删除
     */
    @TableLogic(value = "0", delval = "1")
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}