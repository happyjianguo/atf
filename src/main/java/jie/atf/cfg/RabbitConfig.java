package jie.atf.cfg;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:rabbitmq-cfg.properties")
public class RabbitConfig {
	/**
	 * 配置RabbitMQ连接工厂
	 * 
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @param virtualHost
	 * @param connectionTimeout
	 * @return
	 */
	@Bean
	public ConnectionFactory connectionFactory(@Value("${spring.rabbitmq.host}") String host,
			@Value("${spring.rabbitmq.port}") String port, @Value("${spring.rabbitmq.username}") String username,
			@Value("${spring.rabbitmq.password}") String password,
			@Value("${spring.rabbitmq.virtual-host}") String virtualHost,
			@Value("${spring.rabbitmq.connection-timeout}") String connectionTimeout) {
		CachingConnectionFactory ret = new CachingConnectionFactory();
		ret.setHost(host);
		ret.setPort(Integer.parseInt(port));
		ret.setUsername(username);
		ret.setPassword(password);
		ret.setVirtualHost(virtualHost);
		ret.setConnectionTimeout(Integer.parseInt(connectionTimeout));
		return ret;
	}

	/**
	 * 声明队列
	 * 
	 * @param name
	 * @return
	 */
	@Bean
	public Queue queueAtfEvent(@Value("${atf.queue.event}") String name) {
		return new Queue(name, true, false, false);
	}

	/**
	 * 声明交换器
	 * 
	 * @param name
	 * @return
	 */
	@Bean
	public DirectExchange exchangeAtfEvent(@Value("${atf.exchange.event}") String name) {
		return new DirectExchange(name, true, false);
	}

	/**
	 * 声明绑定
	 * 
	 * @param queue
	 * @param exchange
	 * @param routingKey
	 * @return
	 */
	@Bean
	public Binding binding(@Qualifier("queueAtfEvent") Queue queue,
			@Qualifier("exchangeAtfEvent") DirectExchange exchange,
			@Value("${atf.routingkey.event}") String routingKey) {
		return BindingBuilder.bind(queue).to(exchange).with(routingKey);
	}

	/**
	 * 配置RabbitTemplate
	 * 
	 * @param connectionFactory
	 * @return
	 */
	@Bean
	public RabbitTemplate rabbitTemplate(@Qualifier("connectionFactory") ConnectionFactory connectionFactory) {
		RabbitTemplate ret = new RabbitTemplate(connectionFactory);
		// Jackson2JsonMessageConverter消息转换器
		ret.setMessageConverter(new Jackson2JsonMessageConverter());
		return ret;
	}

	/**
	 * 配置ContainerFactory
	 * 
	 * @param connectionFactory
	 * @return
	 */
	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
			@Qualifier("connectionFactory") ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory ret = new SimpleRabbitListenerContainerFactory();
		ret.setConnectionFactory(connectionFactory);
		// Jackson2JsonMessageConverter消息转换器
		ret.setMessageConverter(new Jackson2JsonMessageConverter());
		return ret;
	}

	// === 延迟队列 ===
	/**
	 * 声明30s延迟队列
	 * 
	 * @param name
	 * @return
	 */
	@Bean
	public Queue queueDelay30s(@Value("${atf.queue.delay.30s}") String name) {
		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put("x-message-ttl", 30 * 1000); // Message TTL = 30s
		arguments.put("x-max-length", 1000000); // Max length = 1million
		// 死信路由到死信交换器DLX
		arguments.put("x-dead-letter-exchange", "atf.exchange.delay.dlx");
		arguments.put("x-dead-letter-routing-key", "atf.routingkey.delay.dlk");
		return new Queue(name, true, false, false, arguments);
	}

	/**
	 * 声明死信队列DLQ
	 * 
	 * @param name
	 * @return
	 */
	@Bean
	public Queue queueDelayDlq(@Value("${atf.queue.delay.dlq}") String name) {
		return new Queue(name, true, false, false);
	}

	/**
	 * 声明延迟交换器
	 * 
	 * @param name
	 * @return
	 */
	@Bean
	public DirectExchange exchangeDelay(@Value("${atf.exchange.delay}") String name) {
		DirectExchange ret = new DirectExchange(name, true, false);
		ret.setInternal(false);
		return ret;
	}

	/**
	 * 声明死信交换器DLX
	 * 
	 * @param name
	 * @return
	 */
	@Bean
	public DirectExchange exchangeDelayDlx(@Value("${atf.exchange.delay.dlx}") String name) {
		return new DirectExchange(name, true, false);
	}

	/**
	 * 绑定延迟交换器和30s延迟队列
	 * 
	 * @param queue
	 * @param exchange
	 * @param routingKey
	 * @return
	 */
	@Bean
	public Binding bindingDelay(@Qualifier("queueDelay30s") Queue queue,
			@Qualifier("exchangeDelay") DirectExchange exchange,
			@Value("${atf.routingkey.delay.30s}") String routingKey) {
		return BindingBuilder.bind(queue).to(exchange).with(routingKey);
	}

	/**
	 * 绑定死信交换器DLX和死信队列DLQ
	 * 
	 * @param queue
	 * @param exchange
	 * @param routingKey
	 * @return
	 */
	@Bean
	public Binding bindingDelayConsume(@Qualifier("queueDelayDlq") Queue queue,
			@Qualifier("exchangeDelayDlx") DirectExchange exchange,
			@Value("${atf.routingkey.delay.dlk}") String routingKey) {
		return BindingBuilder.bind(queue).to(exchange).with(routingKey);
	}
}
