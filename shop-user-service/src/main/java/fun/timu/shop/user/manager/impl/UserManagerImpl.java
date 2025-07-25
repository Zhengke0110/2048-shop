package fun.timu.shop.user.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import fun.timu.shop.user.manager.UserManager;
import fun.timu.shop.user.mapper.UserMapper;
import fun.timu.shop.user.model.DO.UserDO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserManagerImpl implements UserManager {
    private final UserMapper userMapper;

    public UserManagerImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public boolean checkUnique(String mail) {

        QueryWrapper queryWrapper = new QueryWrapper<UserDO>().eq("mail", mail);

        List<UserDO> list = userMapper.selectList(queryWrapper);

        return list.size() > 0 ? false : true;

    }

    @Override
    public int insert(UserDO userDO) {
        return userMapper.insert(userDO);
    }

    @Override
    public List<UserDO> selectList(String mail) {
        List<UserDO> userDOList = userMapper.selectList(new QueryWrapper<UserDO>().eq("mail", mail));
        return userDOList.isEmpty() ? null : userDOList;
    }

}
