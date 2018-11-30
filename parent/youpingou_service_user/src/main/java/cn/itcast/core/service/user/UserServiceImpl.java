package cn.itcast.core.service.user;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.annotation.Resource;
import javax.jms.*;

@Service
public class UserServiceImpl implements UserService {


    @Resource
    private JmsTemplate jmsTemplate;

    @Resource
    private Destination smsDestination;
    @Resource
    private RedisTemplate redisTemplate;


    @Override
    public void sendCode(final String phone) {

        // 封装的数据：手机号、随机生成验证码、签名、模板
        final String code = String.valueOf(RandomUtils.nextInt(6));

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
}
