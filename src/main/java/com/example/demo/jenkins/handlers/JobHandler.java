package com.example.demo.jenkins.handlers;

import com.example.demo.BotProvider;
import com.example.demo.interactive.PeerHandler;
import com.example.demo.interactive.model.Entity;
import com.example.demo.jenkins.JenkinsProvider;
import com.example.demo.jenkins.subscriptions.CommonEventSubscription;
import com.example.demo.jenkins.subscriptions.service.SubscriptionService;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.SettableFuture;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.offbytwo.jenkins.model.QueueReference;
import im.dlg.botsdk.domain.Peer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobHandler {
    private final JenkinsProvider jenkinsProvider;
    private final SubscriptionService subscriptionService;
    private final BotProvider botProvider;

    public String handle(String tail, PeerHandler peerHandler) {
        for (String operator : Arrays.asList(CommandList.STATUS,
                                             CommandList.UNSUB,
                                             CommandList.SUB,
                                             CommandList.START,
                                             CommandList.START_LAST,
                                             CommandList.LOG)) {
            String regex = ".*" + operator + "$";
            if (tail.matches(regex)) {
                String newTail = tail.replace(operator, "").trim();

                if (operator.equals(CommandList.STATUS)) {
                    return statusHandler(newTail);
                } else if (operator.equals(CommandList.UNSUB)) {
                    return unsubscribeHandler(newTail, peerHandler.getPeer());
                } else if (operator.equals(CommandList.SUB)) {
                    return subscribeHandler(newTail, peerHandler.getPeer());
                } else if (operator.equals(CommandList.START_LAST)) {
                    return startLastHandler(newTail, peerHandler.getPeer());
                } else if (operator.equals(CommandList.LOG)) {
                    return logHandler(newTail, peerHandler.getPeer());
                } else if (operator.equals(CommandList.START)) {
                    return startHandler(newTail, peerHandler);
                }
            }
        }
        return "NOT";
    }

    private String statusHandler(String jobName) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

            BuildWithDetails lastBuild = jenkinsProvider.getJob(jobName).details().getLastBuild().details();
            String lastBuildString = "Никогда не запускался";
            if (!lastBuild.equals(BuildWithDetails.BUILD_HAS_NEVER_RUN))
                lastBuildString = String.format("Последняя `%s` [%s]",
                                                dateFormat.format(new Date(lastBuild.getTimestamp())),
                                                lastBuild.getResult());

            BuildWithDetails lastSuccessfulBuild = jenkinsProvider.getJob(jobName).details().getLastSuccessfulBuild().details();
            String lastSuccessfulBuildString = "Нет успешных сборок";
            if (!lastSuccessfulBuild.equals(BuildWithDetails.BUILD_HAS_NEVER_RUN))
                lastSuccessfulBuildString = String.format("Последняя успешная [%s]",
                                                          dateFormat.format(new Date(lastSuccessfulBuild.getTimestamp())));

            BuildWithDetails lastFailedBuild = jenkinsProvider.getJob(jobName).details().getLastFailedBuild().details();
            String lastFailedBuildString = "Нет неуспешных сборок";
            if (!lastFailedBuild.equals(BuildWithDetails.BUILD_HAS_NEVER_RUN))
                lastFailedBuildString = String.format("Последняя провальная [%s]",
                                                      dateFormat.format(new Date(lastFailedBuild.getTimestamp())));
            return String.format("JOB `%s`\n%s\n%s\n%s",
                                 jobName,
                                 lastBuildString,
                                 lastSuccessfulBuildString,
                                 lastFailedBuildString);
        } catch (Exception e) {
            String errorString = String.format("Состояние для `%s` неизвестно, проверьте статус Jenkins :(", jobName);
            log.error(errorString, e);
            return errorString;
        }
    }

    private String subscribeHandler(String jobName, Peer sender) {
        try {
            JobWithDetails job = jenkinsProvider.getJob(jobName).details();
            subscriptionService.subscribe(sender, new CommonEventSubscription(job));
            return String.format("Подписка на `%s` оформлена", jobName);
        } catch (Exception e) {
            String errorString = String.format("Не удалось изменить состояние подписки для `%s` :(", jobName);
            log.error(errorString, e);
            return errorString;
        }
    }

    private String unsubscribeHandler(String jobName, Peer sender) {
        try {
            subscriptionService.unsubscribe(sender, jobName);
            return String.format("Подписка на `%s` прекращена", jobName);
        } catch (Exception e) {
            String errorString = String.format("Не удалось изменить состояние подписки для `%s` :(", jobName);
            log.error(errorString, e);
            return errorString;
        }
    }

    private String startLastHandler(String jobName, Peer sender) {
        try {
            JobWithDetails jobWithDetails = jenkinsProvider.getJob(jobName).details();
            Build b = jobWithDetails.getLastBuild();
            if (b.equals(Build.BUILD_HAS_NEVER_RUN)) {
                return String.format("Задача `%s` никогда не запускалась и не может быть перезапущена", jobName);
            } else {
                subscribeHandler(jobName, sender);
                QueueReference q = jobWithDetails.build(jobWithDetails.getLastBuild().details().getParameters(), true);
                return String.format("Задача `%s` запущена успешно", jobName);
            }
        } catch (Exception e) {
            String errorString = String.format("Не удалось перезапустить задачу `%s`с параметрами последнего запуска :(", jobName);
            log.error(errorString, e);
            return errorString;
        }
    }

    private String logHandler(String jobName, Peer sender) {
        try {
            Build lastBuild = jenkinsProvider.getJob(jobName).details().getLastBuild();
            if (lastBuild.equals(Build.BUILD_HAS_NEVER_RUN))
                return String.format("Эта Job `%s` не имеет запусков и логов", jobName);

            File tempFile = File.createTempFile("temp-logs-out", ".log");
            try (FileWriter tempFileWriter = new FileWriter(tempFile)) {
                tempFileWriter.write(jenkinsProvider.getJob(jobName).details().getLastBuild().details().getConsoleOutputText());
            }
            botProvider.getBot().messaging().sendFile(sender, tempFile);
            return "Выгрузка лога скоро начнется";
        } catch (Exception e) {
            String errorString = String.format("Не удалось получить лог для последнего билда задачи `%s` :(", jobName);
            log.error(errorString, e);
            return errorString;
        }

    }

    private String startByAllArgsHandler(String jobName, String allArgs, Peer sender) {
        return startHandler(jobName, parseParameters(allArgs), sender);
    }

    private String startHandler(String jobName, Map<String, String> args, Peer sender) {
        try {
            subscribeHandler(jobName, sender);
            jenkinsProvider.getJob(jobName).build(args, true);
            return String.format("Задача `%s` запущена успешно", jobName);
        } catch (Exception e) {
            String errorString = String.format("Не удалось запустить job `%s` со списком аргументов `%s` :(",
                                               jobName,
                                               args);
            log.error(errorString, e);
            return errorString;
        }
    }

    private Map<String, String> parseParameters(String params) {
        return Arrays.stream(params.split("\n"))
                     .map(s -> s.split(":"))
                     .collect(Collectors.toMap(o -> o[0], o -> o[1]));
    }

    private String startByParameterRequest(String jobName, PeerHandler peerHandler) {
        try {
            Map<String, String> args = jenkinsProvider.getJob(jobName)
                                                      .details()
                                                      .getLastBuild()
                                                      .details()
                                                      .getParameters();
            Thread t = new Thread(() -> {
                for (String argKey : args.keySet()) {
                    args.put(argKey, requireTextSyncronized(peerHandler, argKey));
                }
                peerHandler.sendMessage(startHandler(jobName, args, peerHandler.getPeer()));
            });
            t.start();
            return PeerHandler.DELAYED_COMMAND;
        } catch (Exception e) {
            String errorString = String.format("Не удалось запустить job `%s` с подсказкой аргументов :(", jobName);
            log.error(errorString, e);
            return errorString;
        }
    }

    private String startHandler(String jobName, PeerHandler peerHandler) {
        try {
            jenkinsProvider.getJob(jobName).details();
            List<Entity> entities = ImmutableList.of(
                    new Entity("no args", "Без аргументов"),
                    new Entity("args", "Ручной ввод"),
                    new Entity("args auto", "Запрашивать")
            );
            peerHandler.requestSelect("Выбеорите процесс запуска",
                                      entities,
                                      identifier -> {
                                          if (identifier.equals("no args")) {
                                              peerHandler.sendMessage(startHandler(jobName,
                                                                                   Collections.emptyMap(),
                                                                                   peerHandler.getPeer()));
                                          } else if (identifier.equals("args")) {
                                              peerHandler.requestText("Введите аргументы запуска <имя:значение>",
                                                                      input -> {
                                                                          String result =
                                                                                  startByAllArgsHandler(jobName,
                                                                                                        input,
                                                                                                        peerHandler.getPeer());
                                                                          peerHandler.sendMessage(result);

                                                                      });
                                          } else if (identifier.equals("args auto")) {
                                              String s = startByParameterRequest(jobName, peerHandler);
                                              if (!s.equals(PeerHandler.DELAYED_COMMAND))
                                                  peerHandler.sendMessage(s);
                                          } else {
                                              peerHandler.sendMessage(
                                                      "А как ты вообще сюда попал? Оо " + identifier);
                                          }
                                      });

            return PeerHandler.DELAYED_COMMAND;
        } catch (Exception e) {
            String errorString = String.format("Не удалось запустить job `%s` :(", jobName);
            log.error(errorString, e);
            return errorString;
        }
    }

    @SneakyThrows
    public String requireTextSyncronized(PeerHandler peerHandler, String arg) {
        SettableFuture<String> stringSettableFuture = SettableFuture.create();
        peerHandler.requestText(String.format("Введите значение аргумента %s", arg),
                                s -> stringSettableFuture.set(s));
        return stringSettableFuture.get();
    }
}
