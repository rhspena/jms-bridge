package com.bpd;

import com.ibm.mq.jakarta.jms.MQXAConnectionFactory;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;

import org.apache.camel.component.jms.JmsComponent;
import org.eclipse.microprofile.config.ConfigProvider;
import org.springframework.transaction.jta.JtaTransactionManager;

import io.quarkiverse.messaginghub.pooled.jms.PooledJmsWrapper;
import io.smallrye.common.annotation.Identifier;
import jakarta.inject.Singleton;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.transaction.TransactionManager;

public class Producers {

    @Identifier("mq")
    JmsComponent ibmmq(@Identifier("mqConnectionFactory") ConnectionFactory cn, JtaTransactionManager tm) {
        System.out.println("Starting IBM MQ connection");
        JmsComponent mq = new JmsComponent();
        mq.setConnectionFactory(cn);
        mq.setTransactionManager(tm);
        return mq;
    }

    @Identifier("mqConnectionFactory")
    public ConnectionFactory createXAConnectionFactory(PooledJmsWrapper wrapper) {
        MQXAConnectionFactory mq = new MQXAConnectionFactory();
        try {
            mq.setHostName(ConfigProvider.getConfig().getValue("ibm.mq.host", String.class));
            mq.setPort(ConfigProvider.getConfig().getValue("ibm.mq.port", Integer.class));
            mq.setChannel(ConfigProvider.getConfig().getValue("ibm.mq.channel", String.class));
            mq.setQueueManager(ConfigProvider.getConfig().getValue("ibm.mq.queueManagerName", String.class));
            mq.setTransportType(WMQConstants.WMQ_CM_CLIENT);
            mq.setStringProperty(WMQConstants.USERID, ConfigProvider.getConfig().getValue("ibm.mq.user", String.class));
            mq.setStringProperty(WMQConstants.PASSWORD, ConfigProvider.getConfig().getValue("ibm.mq.password", String.class));
        } catch (JMSException e) {
            throw new RuntimeException("Unable to create IBM MQ Connection Factory", e);
        }
        return wrapper.wrapConnectionFactory(mq);
    }

    @Identifier("amq")
    JmsComponent amq(ConnectionFactory cf, JtaTransactionManager tm) {
        System.out.println("Starting Artemis MQ connection");
        JmsComponent amq = new JmsComponent();
        amq.setConnectionFactory(cf);
        amq.setTransactionManager(tm);
        return amq;
    }    

    @Singleton
    JtaTransactionManager manager(TransactionManager transactionManager) {
        return new JtaTransactionManager(transactionManager);
    }
}
