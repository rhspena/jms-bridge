package com.bpd;

import org.apache.camel.builder.RouteBuilder;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Routes extends RouteBuilder {

    @Override
    public void configure() {

        // PRODUCERS
        rest()
                .post("/mq/produce/message")
                .id("mq")
                .to("direct:sendibmmq");

        from("direct:sendibmmq")
                .transacted()
                .log("Enviando mensaje a IBM MQ: ${body}")
                .to("mq:queue:{{ibm.mq.queue}}?disableReplyTo=true");

        rest()
                .post("/amq/produce/message")
                .id("amq")
                .to("direct:sendamq");

        from("direct:sendamq")
                .transacted()
                .log("Enviando mensaje a Active MQ: ${body}")
                .to("amq:queue:{{amq.queue}}?disableReplyTo=true");

        // JMS BRIDGES
        from("mq:queue:{{ibm.mq.queue}}")
                .id("ibmmq-amq")
                .transacted()
                .log("Consumiendo un mensaje de IBM MQ para enviarlo a Active MQ: ${body}")
                .to("amq:queue:{{amq.queue.from-mq}}");

        from("amq:queue:{{amq.queue}}")
                .id("amq-ibmmq")
                .transacted()
                .log("Consumiendo un mensaje de Active MQ para enviarlo a IBM MQ: ${body}")
                .to("mq:queue:{{ibm.mq.queue.from-amq}}");

        // CONSUMERS
        from("amq:queue:{{amq.queue.from-mq}}")                
                .id("amqc")
                .log("Mensaje recibido de Active MQ: ${body}");

        from("mq:queue:{{ibm.mq.queue.from-amq}}") 
                .id("mqc")
                .log("Mensaje recibido de IBM MQ: ${body}");
    }

}
