package cn.itcast.core.service.user;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.utils.md5.MD5Util;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.jms.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {


    @Resource
    private JmsTemplate jmsTemplate;

    @Resource
    private Destination smsDestination;
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private UserDao userDao;



    @Override
    public void sendCode(final String phone) {

        // 封装的数据：手机号、随机生成验证码、签名、模板
        final String code = String.valueOf(RandomUtils.nextInt(6));
        System.out.println("验证码:"+code);
        //验证码封装到redis
        redisTemplate.boundValueOps(phone).set(code);
        //设置过期时间 5minute
        redisTemplate.boundValueOps(phone).expire(5, TimeUnit.MINUTES);

        // 将数据发送到mq中
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("phoneNumbers", phone);
                mapMessage.setString("signName", "阮文");
                mapMessage.setString("templateCode", "SMS_140720901");
                mapMessage.setString("templateParam", "{\"code\":\""+code+"\"}");
                return mapMessage;
            }
        });

    }
    @Transactional
    @Override
    public void add(User user,String smscode) {
        String code = (String) redisTemplate.boundValueOps(user.getPhone()).get();
        if (smscode!=null&&smscode.equals(code)) {
            user.setCreated(new Date());
            user.setUpdated(new Date());
            String md5Encode = MD5Util.MD5Encode(user.getPassword(), null);
            user.setPassword(md5Encode);
            userDao.insertSelective(user);

        } else {
            throw new RuntimeException("验证码错误");
        }

    }
}
