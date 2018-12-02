package cn.itcast.core.service.user;

import cn.itcast.core.pojo.user.User;

public interface UserService {

    public void sendCode(String phone);

    /**
     * 新增
     * @param user
     */
    public void add(User user,String smscode);
}
