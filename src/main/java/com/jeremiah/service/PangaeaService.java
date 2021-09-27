package com.jeremiah.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeremiah.model.Subscription;
import com.jeremiah.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PangaeaService {

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    RestTemplate restTemplate;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public Subscription subscribe(String topic, Subscription subscription) {
        subscription.setTopic(topic);
        return subscriptionRepository.save(subscription);
    }

    public Page<Subscription> listSubscription(String topic, String url, Pageable pageable) {
        return subscriptionRepository.findAllByTopicAndUrl(topic, url, pageable);
    }

    public Optional<Subscription> getSubscription(String topic, String url) {
        return subscriptionRepository.getByTopicAndUrl(topic, url);
    }

    public void publishToTopic(String topic, Map<String, Object> body) throws JsonProcessingException {
        kafkaTemplate.send(topic, objectMapper.writeValueAsString(body));
    }

    public void consumeTopic(String topic, String message) {
        HttpHeaders headers = new HttpHeaders();
        List<Subscription> subscriptions = subscriptionRepository.findAllByTopicUnPaged(topic);
        for (Subscription subscription : subscriptions) {
            HttpEntity<String> request = new HttpEntity<>(message, headers);
            String result = restTemplate.postForObject(subscription.getUrl(), request, String.class);
        }
    }

}
