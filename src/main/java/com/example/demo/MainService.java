package com.example.demo;

import com.example.demo.stash.util.HttpRequestType;
import com.example.demo.stash.util.RestCallConfiguration;
import com.example.demo.stash.util.RestCallService;
import im.dlg.botsdk.Bot;
import im.dlg.botsdk.BotConfig;
import lombok.RequiredArgsConstructor;
import org.asynchttpclient.Response;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class MainService {
    private final RestCallService restCallService;

    @PostConstruct
    public void main() throws InterruptedException, ExecutionException {
        BotConfig botConfig = BotConfig.Builder.aBotConfig()
                .withHost("hackathon-mob.transmit.im")
                .withPort(443)
                .withToken("d33eadd7fa9460116631493b5076c2965d614443").build();

        Bot bot = Bot.start(botConfig).get();

        bot.messaging().onMessage(
                message -> {
                    RestCallConfiguration configuration = RestCallConfiguration.builder()
                            .username("kirekov")
                            .password("1234")
                            .requestType(HttpRequestType.GET)
                            .path("/rest/api/1.0/projects")
                            .build();
                    Future<Response> response = restCallService.call(configuration);
                    String result = null;
                    try {
                        result = response.get().getResponseBody();
                    } catch (Exception e) {
                        result = "Ошибка. Все плохо";
                    }
                    bot.messaging().sendText(message.getPeer(), result);
                }
        );

        bot.await();
    }
}
