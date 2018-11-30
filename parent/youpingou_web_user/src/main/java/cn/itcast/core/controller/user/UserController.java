package cn.itcast.core.controller.user;

import cn.itcast.core.entity.Result;
import cn.itcast.core.service.user.UserService;
import cn.itcast.core.utils.checkphone.PhoneFormatCheckUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Reference
    private UserService userService;

    @RequestMapping("/sendCode.do")
    public Result sendCode(String phone) {
        try {
            if (!PhoneFormatCheckUtils.isPhoneLegal(phone)) {
                return new Result(false, "手机号码不合法");
            }
            userService.sendCode(phone);
            return new Result(true, "验证码发送成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "验证码发送失败");
        }

    }
}
