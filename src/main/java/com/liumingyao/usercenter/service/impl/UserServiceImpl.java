package com.liumingyao.usercenter.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liumingyao.usercenter.common.ErrorCode;
import com.liumingyao.usercenter.exception.BusinessException;
import com.liumingyao.usercenter.mapper.UserMapper;
import com.liumingyao.usercenter.model.User;

import com.liumingyao.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.liumingyao.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
* @author LiuMingyao
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2023-04-14 19:07:52
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;


    @Override
    public long userRegister(String userAccount, String userPassword, String checkUserPassword, String planetCode) {
        //1.校验.非空; 校验账户，账户不小于4位; 密码不小于8位
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkUserPassword, planetCode)){
//            return -1;
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账户过短");
        }
        if (userPassword.length() <= 8 || checkUserPassword.length() <= 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }

        if (planetCode.length() > 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }

        //密码和校验密码是否相同
        if (!userPassword.equals(checkUserPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码和校验密码不相同");
        }

        //账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        //星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号重复");
        }

        //2.加密
        // 密码加自定义盐值，确保密码安全
        String saltPwd = userPassword + "_lmy0908";
        // 生成md5值，并转为大写字母
        String md5Pwd = DigestUtils.md5Hex(saltPwd).toUpperCase();

        //3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(md5Pwd);
        user.setPlanetCode(planetCode);

        boolean saveResult = this.save(user);
        if (!saveResult){
            return -1;
        }

        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验.非空; 校验账户，账户不小于4位; 密码不小于8位
        if (StringUtils.isAnyBlank(userAccount, userPassword)){
            return null;
        }
        if (userAccount.length() < 4){
            return null;
        }
        if (userPassword.length() <= 8){
            return null;
        }

        //2.加密
        // 密码加自定义盐值，确保密码安全
        String saltPwd = userPassword + "_lmy0908";
        // 生成md5值，并转为大写字母
        String md5Pwd = DigestUtils.md5Hex(saltPwd).toUpperCase();

        //查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", md5Pwd);
        User user = userMapper.selectOne(queryWrapper);
        //用户不存在
        if (user == null) {
            log.warn("user login failed, userAccount cannot match userPassword");
            return null;
        }

        //3.用户脱敏
        User safetyUser = safetyUser(user);

        //4.记录用户的登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }

    /**
     * 用户脱敏
     * @param user
     * @return
     */
    @Override
    public User safetyUser(User user){
        if (user == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(user.getId());
        safetyUser.setUsername(user.getUsername());
        safetyUser.setUserAccount(user.getUserAccount());
        safetyUser.setAvatarUrl(user.getAvatarUrl());
        safetyUser.setGender(user.getGender());
        safetyUser.setPhone(user.getPhone());
        safetyUser.setEmail(user.getEmail());
        safetyUser.setUserStatus(0);
        safetyUser.setUserRole(user.getUserRole());
        safetyUser.setPlanetCode(user.getPlanetCode());
        safetyUser.setCreateTime(user.getCreateTime());
        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        //移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }
}




