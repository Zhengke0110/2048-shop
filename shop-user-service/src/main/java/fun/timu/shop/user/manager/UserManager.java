package fun.timu.shop.user.manager;

import fun.timu.shop.user.model.DO.UserDO;

import java.util.List;

public interface UserManager {
    boolean checkUnique(String mail);

    int insert(UserDO userDO);

    List<UserDO> selectList(String mail);

    UserDO selectOne(Long id);
}
