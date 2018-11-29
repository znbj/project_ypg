package cn.itcast.core.listener;

import cn.itcast.core.service.staticpage.StaticPageService;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

public class PageListener implements MessageListener {

    @Resource
    private StaticPageService staticPageService;


    @Override
    public void onMessage(Message message) {

        try {
            ActiveMQTextMessage activeMQTextMessage=(ActiveMQTextMessage)message;
            String id = activeMQTextMessage.getText();
            System.out.println("service-page-id:"+id);
            staticPageService.getHtml(Long.valueOf(id));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
