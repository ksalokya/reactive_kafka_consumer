package org.producer;

import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.background.BackgroundEventHandler;
import com.launchdarkly.eventsource.background.BackgroundEventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.sql.Time;
import java.util.concurrent.TimeUnit;

@Service
public class WikimediaChangeProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WikimediaChangeProducer.class);

    private KafkaTemplate<String, String> kafkaTemplate;

    public WikimediaChangeProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage() throws InterruptedException {
        String topic = "wikimedia_recentchange";

        BackgroundEventHandler backgroundEventHandler = new WikimediaChangeHandler(kafkaTemplate, topic);
        String url = "https://stream.wikimedia.org/v2/stream/recentchange";

        EventSource.Builder eventBuilder = new EventSource.Builder(URI.create(url));
        BackgroundEventSource.Builder sourceBuilder = new BackgroundEventSource.Builder(backgroundEventHandler, eventBuilder);
        BackgroundEventSource backgroundEventSource = sourceBuilder.build();
//        backgroundEventSource.start();
//        TimeUnit.MINUTES.sleep(10);

        Thread thread = new Thread(() -> {
            while (true) {
                backgroundEventSource.start();
                try {
                    TimeUnit.SECONDS.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
}
