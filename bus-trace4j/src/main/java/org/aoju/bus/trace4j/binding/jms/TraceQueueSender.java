package org.aoju.bus.trace4j.binding.jms;


import javax.jms.*;

public final class TraceQueueSender implements QueueSender {

	private final TraceMessageProducer messageProducer;
	private final QueueSender delegate;

	public TraceQueueSender(TraceMessageProducer messageProducer, QueueSender delegate) {
		this.messageProducer = messageProducer;
		this.delegate = delegate;
	}

	@Override
	public Queue getQueue() throws JMSException {
		return delegate.getQueue();
	}

	@Override
	public void send(Queue queue, Message message) throws JMSException {
		messageProducer.writeTraceContextToMessage(message);
		delegate.send(queue, message);
	}

	@Override
	public void send(Queue queue, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
		messageProducer.writeTraceContextToMessage(message);
		delegate.send(queue, message, deliveryMode, priority, timeToLive);
	}

	@Override
	public void send(Message message) throws JMSException {
		messageProducer.send(message);
	}

	@Override
	public void send(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
		messageProducer.send(message, deliveryMode, priority, timeToLive);
	}

	@Override
	public void send(Destination destination, Message message) throws JMSException {
		messageProducer.send(destination, message);
	}

	@Override
	public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
		messageProducer.send(destination, message, deliveryMode, priority, timeToLive);
	}

	@Override
	public void setDisableMessageID(boolean value) throws JMSException {
		messageProducer.setDisableMessageID(value);
	}

	@Override
	public boolean getDisableMessageID() throws JMSException {
		return messageProducer.getDisableMessageID();
	}

	@Override
	public void setDisableMessageTimestamp(boolean value) throws JMSException {
		messageProducer.setDisableMessageTimestamp(value);
	}

	@Override
	public boolean getDisableMessageTimestamp() throws JMSException {
		return messageProducer.getDisableMessageTimestamp();
	}

	@Override
	public void setDeliveryMode(int deliveryMode) throws JMSException {
		messageProducer.setDeliveryMode(deliveryMode);
	}

	@Override
	public int getDeliveryMode() throws JMSException {
		return messageProducer.getDeliveryMode();
	}

	@Override
	public void setPriority(int defaultPriority) throws JMSException {
		messageProducer.setPriority(defaultPriority);
	}

	@Override
	public int getPriority() throws JMSException {
		return messageProducer.getPriority();
	}

	@Override
	public void setTimeToLive(long timeToLive) throws JMSException {
		messageProducer.setTimeToLive(timeToLive);
	}

	@Override
	public long getTimeToLive() throws JMSException {
		return messageProducer.getTimeToLive();
	}

	@Override
	public Destination getDestination() throws JMSException {
		return messageProducer.getDestination();
	}

	@Override
	public void close() throws JMSException {
		messageProducer.close();
	}

}
