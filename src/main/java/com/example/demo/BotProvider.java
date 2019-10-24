package com.example.demo;

import im.dlg.botsdk.Bot;
import im.dlg.botsdk.BotConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Getter
@Service
public class BotProvider {
    private Bot bot;

    @PostConstruct
    private void init() {
        try {
            BotConfig botConfig = BotConfig.Builder.aBotConfig()
                    .withHost("hackathon-mob.transmit.im")
                    .withPort(443)
                    .withToken("51236c31c453ecc2ad47836c1aa460bcc1585a9f").build();
            bot = Bot.start(botConfig).get();
        } catch (Exception e) {
            log.error("Ошика при создании бота", e);
        }
    }

}
