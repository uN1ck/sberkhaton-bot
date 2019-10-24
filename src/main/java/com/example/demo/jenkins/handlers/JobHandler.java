package com.example.demo.jenkins.handlers;

import com.example.demo.BotProvider;
import com.example.demo.interactive.PeerHandler;
import com.example.demo.jenkins.provider.JenkinsProviderImpl;
import com.example.demo.jenkins.subscriptions.CommonEventSubscription;
import com.example.demo.jenkins.subscriptions.service.SubscriptionService;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.offbytwo.jenkins.model.QueueReference;
import im.dlg.botsdk.domain.Peer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobHandler {
    private final JenkinsProviderImpl jenkinsProvider;
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
                    return subscribeHandler(newTail, false, peerHandler.getPeer());
                } else if (operator.equals(CommandList.SUB)) {
                    return subscribeHandler(newTail, true, peerHandler.getPeer());
                } else if (operator.equals(CommandList.START_LAST)) {
                    return startLastHandler(newTail, peerHandler.getPeer());
                } else if (operator.equals(CommandList.LOG)) {
                    return logHandler(newTail, peerHandler.getPeer());
                } else if (operator.equals(CommandList.START)) {
                    //  return startLastHandler(newTail, peerHandler.getPeer());
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
            log.error("Не удалось получить состояние джобы", e);
            return "Неизвестно, проверьте статус Jenkins :(";

        }
    }

    private String subscribeHandler(String jobName, boolean subscribe, Peer sender) {
        try {
            if (subscribe) {
                JobWithDetails job = jenkinsProvider.getJob(jobName).details();
                subscriptionService.subscribe(sender, new CommonEventSubscription(job));
                return String.format("Подписка на `%s` оформлена", jobName);
            } else {
                subscriptionService.unsubscribe(sender, jobName);
                return String.format("Подписка на `%s` прекращена", jobName);
            }
        } catch (Exception e) {
            log.error("Ошибка при запуске задачи", e);
            return "Не удалось изменить состояние подписки :(";
        }
    }

    private String startLastHandler(String jobName, Peer sender) {
        try {
            JobWithDetails jobWithDetails = jenkinsProvider.getJob(jobName).details();
            Build b = jobWithDetails.getLastBuild();
            if (b.equals(Build.BUILD_HAS_NEVER_RUN)) {
                return String.format("Задача `%s` никогда не запускалась и не может быть перезапущена", jobName);
            } else {
                subscribeHandler(jobName, true, sender);
                QueueReference q = jobWithDetails.build(jobWithDetails.getLastBuild().details().getParameters(), true);
                return String.format("Задача `%s` запущена успешно", jobName);
            }
        } catch (Exception e) {
            log.error("Ошибка при запуске задачи " + jobName, e);
            return "Не удалось запустить :(";
        }
    }

    private String logHandler(String jobName, Peer sender) {
        try {
            String log = jenkinsProvider.getJob(jobName).details().getLastBuild().details().getConsoleOutputText();
            File f = File.createTempFile("temp-logs-out", ".log");
            try (FileWriter fw = new FileWriter(f)) {
                fw.write(log);
            }
            botProvider.getBot().messaging().sendFile(sender, f);
            return "Выгрузка лога скоро начнется";
        } catch (Exception e) {
            log.error("Не удалось получить лог для job " + jobName, e);
            return "Не удалось выгрузить файл лога послденего билда " + jobName;
        }

    }

}
