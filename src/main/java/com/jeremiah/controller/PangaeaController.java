package com.jeremiah.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jeremiah.model.MessageResponse;
import com.jeremiah.model.Subscription;
import com.jeremiah.service.PangaeaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@ControllerAdvice
@RequestMapping("/")
public class PangaeaController {

    @Autowired
    PangaeaService pangaeaService;

    @RequestMapping(value = "/subscribe", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listSubscription(Pageable pageable) {
        return new ResponseEntity<>(pangaeaService.listSubscription("", "", pageable), HttpStatus.OK);
    }

    @RequestMapping(value = "/subscribe/{topic}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> subscribe(@PathVariable String topic,
                                       @RequestBody @Validated Subscription subscription) {

        if (pangaeaService.getSubscription(topic, subscription.getUrl()).isPresent()) {
            return new ResponseEntity<>(new MessageResponse("The url is subscribed to the specified topic"),
                    HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(pangaeaService.subscribe(topic, subscription), HttpStatus.OK);
    }

    @RequestMapping(value = "/publish/{topic}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> publish(@PathVariable String topic, @RequestBody Map<String, Object> body)
            throws JsonProcessingException {
        pangaeaService.publishToTopic(topic, body);
        return new ResponseEntity<>(new MessageResponse("Successfully published to the topic"), HttpStatus.OK);
    }

    @KafkaListener(topicPattern = "/*")
    public void listenAnyTopic(@Header(KafkaHeaders.RECEIVED_TOPIC) String topic, @Payload String message) {
        System.out.println("COM.JEREMIAH.PANGAEA"  + "Received Message in group foo: " + message);
        pangaeaService.consumeTopic(topic, message);
    }

    @ResponseBody
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Object handleBindException(BindException ex) {
        ex.printStackTrace();
        return new MessageResponse(ex.getAllErrors().get(0).getDefaultMessage());
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Object handleException(Exception ex) {
        ex.printStackTrace();
        return new MessageResponse(ex.getMessage());
    }

}
