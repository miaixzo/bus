package org.aoju.bus.trace4j.binding.jms;

import javax.jms.MessageProducer;
import javax.jms.QueueSender;
import javax.jms.TopicPublisher;

public final class TraceMessageWriter {

    private TraceMessageWriter() {
    }

    public static MessageProducer wrap(MessageProducer messageProducer) {
        return new TraceMessageProducer(messageProducer);
    }

    public static QueueSender wrap(QueueSender queueSender) {
        return new TraceQueueSender(new TraceMessageProducer(queueSender), queueSender);
    }

    public static TopicPublisher wrap(TopicPublisher topicPublisher) {
        return new TraceTopicPublisher(new TraceMessageProducer(topicPublisher), topicPublisher);
    }

}
