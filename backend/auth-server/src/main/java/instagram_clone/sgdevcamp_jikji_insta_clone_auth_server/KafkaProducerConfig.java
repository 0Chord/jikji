package instagram_clone.sgdevcamp_jikji_insta_clone_auth_server;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import instagram_clone.sgdevcamp_jikji_insta_clone_auth_server.kafka.dto.UserInfo;

@Configuration
@EnableKafka
public class KafkaProducerConfig {

	@Value("${spring.kafka.bootstrap-servers}")
	private String servers;

	@Bean
	public ProducerFactory<String, UserInfo> producerFactory() {
		Map<String, Object> config = new HashMap<>();
		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		return new DefaultKafkaProducerFactory<>(config);
	}

	@Bean
	public KafkaTemplate<String, UserInfo> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}
}
