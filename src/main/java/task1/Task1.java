package task1;

import java.io.IOException;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * Класс реализует передачу сообщения из одной очереди Apache ActiveMQ в две другие, в каждую по одной копии. 
 * Добавлена обработка ошибок при получении пустого сообщения в виде вывода текста ошибки в консоль Java. 
 * Пустым сообщением считается сообщение, не содержащее ни одного символа.
 * @author Прилуков П.А.
 *
 */
public class Task1 {
	
	public static void main(String[] args) throws JMSException, IOException {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		Connection connection = connectionFactory.createConnection();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
		MessageConsumer sourceQueue = session.createConsumer(session.createQueue("source.queue"));
		final MessageProducer targetQueue1 = session.createProducer(session.createQueue("target1.queue"));
		final MessageProducer targetQueue2 = session.createProducer(session.createQueue("target2.queue"));
		
		sourceQueue.setMessageListener(new MsgDispatcher(targetQueue1, targetQueue2));
		
		connection.start();
		
		System.out.println("Input something to terminate JVM\n");
		System.in.read();
		
		sourceQueue.close();
		targetQueue1.close();
		targetQueue2.close();
        session.close();
        connection.close();
	}
	
	/**
	 * Класс реализует обработку принятых сообщений.
	 *
	 */
	private static class MsgDispatcher implements MessageListener {

		private MessageProducer targetQueue1 = null;
		private MessageProducer targetQueue2 = null;
		
		/**
		 * 
		 * @param targetQueue1 - Очередь-приемник №1
		 * @param targetQueue2 - Очередь-приемник №2
		 */
		public MsgDispatcher(MessageProducer targetQueue1, MessageProducer targetQueue2) {
			this.targetQueue1 = targetQueue1;
			this.targetQueue2 = targetQueue2;
		}
		
		@Override
		public void onMessage(Message message) {
			if (message instanceof TextMessage) {
				try {
					String txt = ((TextMessage) message).getText();
					if (!txt.isEmpty()) {
						targetQueue1.send(message);
						targetQueue2.send(message);
					} else {
						System.out.println("Message is empty!");
					}
				} catch (JMSException e) {
					e.printStackTrace();
				}
				
			} else {
				System.out.println("Is not text message!");
			}			
		}		
	}
	
}
