package com.example.matdongsanserver.domain.module.service;

import com.example.matdongsanserver.domain.child.repository.ChildRepository;
import com.example.matdongsanserver.domain.dashboard.entity.QuestionAnswer;
import com.example.matdongsanserver.domain.dashboard.repository.QuestionAnswerRepository;
import com.example.matdongsanserver.domain.module.exception.ModuleErrorCode;
import com.example.matdongsanserver.domain.module.exception.ModuleException;
import com.example.matdongsanserver.domain.dashboard.entity.StoryQuestion;
import com.example.matdongsanserver.domain.story.exception.StoryErrorCode;
import com.example.matdongsanserver.domain.story.exception.StoryException;
import com.example.matdongsanserver.domain.dashboard.repository.StoryQuestionRepository;
import com.example.matdongsanserver.domain.story.service.ExternalApiService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModuleService {

    @Value("${module.address}")
    private String brokerAddress;

    @Value("${module.topic}")
    private String topic;

    private IMqttClient client;
    private final StoryService storyService;
    private final StoryQuestionRepository storyQuestionRepository;
    private final ChildRepository childRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final ExternalApiService externalApiService;

    @PostConstruct
    public void init() throws MqttException {
        this.client = new MqttClient(brokerAddress, MqttClient.generateClientId());
        this.client.connect();
        log.info("Init MQTT broker");
    }

    @PreDestroy
    public void disconnect() throws MqttException {
        if (client != null && client.isConnected()) {
            client.disconnect();
            log.info("Disconnected from MQTT broker");
        }
    }

    @Transactional
    public void sendStory(String storyId) {
        String storyTTS = storyService.getOrRegisterStoryTTS(storyId);
        sendMqttMessage("only-play", storyTTS);
    }

    @Transactional
    public void sendQuestion(Long storyQuestionId, Long childId) {
        StoryQuestion storyQuestion = storyQuestionRepository.findByIdOrThrow(storyQuestionId);

        // 질문에 응답하는 자녀를 설정
        storyQuestion.updateChild(childRepository.findByIdOrThrow(childId));

        storyQuestion.getQuestionAnswers().forEach(
                qna -> sendMqttMessage("play-and-record", externalApiService.getQuestionTTS(qna.getId(), qna.getQuestion(), storyQuestion.getLanguage()))
        );
    }

    private void sendMqttMessage(String action, String content) {
        String command = action + " " + content;
        MqttMessage message = new MqttMessage(command.getBytes());
        message.setQos(1);

        try {
            client.publish(topic, message);
            log.info("Sent command: {}", command);
        } catch (MqttException e) {
            log.error("Failed to send command: {}", command, e);
            throw new ModuleException(ModuleErrorCode.FAILED_TO_CONNECT_MODULE);
        }
    }

    @Transactional
    public String uploadAnswer(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ModuleException(ModuleErrorCode.INVALID_FILE);
        }
        log.info("File name: {}", file.getOriginalFilename());

        Long questionId = Long.parseLong(Objects.requireNonNull(file.getOriginalFilename()).replace("-recorded.mp3", ""));
        QuestionAnswer questionAnswer = questionAnswerRepository.findById(questionId).orElseThrow(
                () -> new StoryException(StoryErrorCode.INVALID_FILE_NAME)
        );

        return questionAnswer.updateAnswer(externalApiService.sendSTTRequest(file));
    }
}