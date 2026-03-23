package com.yupi.yuaicodemother.service.impl;

import com.yupi.yuaicodemother.model.entity.User;
import com.yupi.yuaicodemother.model.vo.LoginUserVO;
import com.yupi.yuaicodemother.model.vo.UserVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UserServiceImplTest {

    private final UserServiceImpl userService = new UserServiceImpl();

    @Test
    void getLoginUserVO_shouldFallbackToUserAccountWhenUserNameIsPlaceholder() {
        User user = new User();
        user.setUserAccount("demoAccount");
        user.setUserName("无名");

        LoginUserVO loginUserVO = userService.getLoginUserVO(user);

        Assertions.assertEquals("demoAccount", loginUserVO.getUserName());
    }

    @Test
    void getUserVO_shouldFallbackToUserAccountWhenUserNameIsBlank() {
        User user = new User();
        user.setUserAccount("demoAccount");
        user.setUserName("   ");

        UserVO userVO = userService.getUserVO(user);

        Assertions.assertEquals("demoAccount", userVO.getUserName());
    }

    @Test
    void getLoginUserVO_shouldKeepCustomUserName() {
        User user = new User();
        user.setUserAccount("demoAccount");
        user.setUserName("小鱼");

        LoginUserVO loginUserVO = userService.getLoginUserVO(user);

        Assertions.assertEquals("小鱼", loginUserVO.getUserName());
    }
}
