package cn.itcast.core.listener;

import cn.itcast.core.service.search.ItemSearchService;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

public class ItemDeleteListener implements MessageListener {

    @Resource
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {

        try {
            ActiveMQTextMessage activeMQTextMessage = (ActiveMQTextMessage) message;
            String id = activeMQTextMessage.getText();
            System.out.println("service-search-id:"+id);
            itemSearchService.deleteItemFromSolr(Long.valueOf(id));
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
