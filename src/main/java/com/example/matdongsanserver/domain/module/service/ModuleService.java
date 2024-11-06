package com.example.matdongsanserver.domain.module.service;

import com.example.matdongsanserver.domain.module.exception.ModuleErrorCode;
import com.example.matdongsanserver.domain.module.exception.ModuleException;
import com.example.matdongsanserver.domain.story.service.StoryService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModuleService {

    private final StoryService storyService;

    @Value("${module.address}")
    private String brokerAddress;

    @Value("${module.topic}")
    private String topic;

    private IMqttClient client;

    @PostConstruct
    public void init() throws MqttException {
        this.client = new MqttClient(brokerAddress, MqttClient.generateClientId());
        this.client.connect();
    }

    @Transactional
    public void sendCommand(String storyId) throws IOException {
        String command = "play-and-record " + storyService.getStoryTTS(storyId);
        MqttMessage message = new MqttMessage(command.getBytes());
        message.setQos(1); // QoS 설정 (1 = 전달 보장)

        try {
            client.publish(topic, message);
            log.info("Sent command: {}", command);
        } catch (MqttException e) {
            log.info("Failed to send command");
            throw new ModuleException(ModuleErrorCode.FAILED_TO_CONNECT_MODULE);
        }
    }

    @PreDestroy
    public void disconnect() throws MqttException {
        if (client != null && client.isConnected()) {
            client.disconnect();
            log.info("Disconnected from MQTT broker");
        }
    }
}
