package com.example.demo.csm.service;

import com.example.demo.csm.data.entity.Cluster;
import com.example.demo.csm.data.entity.system.MetaInf;
import com.example.demo.csm.data.entity.wildfly.Module;
import com.example.demo.csm.data.entity.wildfly.WildFlyGroup;
import com.example.demo.csm.data.entity.wildfly.WildFlyNode;
import com.example.demo.csm.data.management.Command;
import com.example.demo.csm.data.management.CommandModule;
import com.example.demo.csm.data.management.CommandRestartNode;
import com.example.demo.csm.data.management.CommandResult;
import com.example.demo.interactive.model.Entity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import im.dlg.botsdk.Bot;
import im.dlg.botsdk.domain.Message;
import im.dlg.botsdk.domain.Peer;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
@Service
public class CsmRequestService {
    private HttpClient client = HttpClientBuilder.create().build();
    private final ObjectMapper om = new ObjectMapper();
    private String csmApiConnector = "localhost:8080/csm";
    private List<CommandResult> results;
    private Object resultsSync = new Object();
    private Map<UUID, Peer> watcher;
    private Map<UUID, Command> storage;
    @Getter
    private List<Entity> printNames;
    @Setter
    private Bot bot;
    private ApplicationContext context;

    @Autowired
    public CsmRequestService(ApplicationContext context) {
        this.context = context;
        watcher = new ConcurrentHashMap<>();
        storage = new ConcurrentHashMap<>();
        printNames = new ArrayList<>();
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

        ses.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                HttpResponse response = null;
                try {
                    response = doCommand("command/performed", null);

                    if (response.getStatusLine().getStatusCode() == 200) {
                        List<CommandResult> resultsFromCsm = om.readValue(EntityUtils.toString(response.getEntity()), om.getTypeFactory().constructCollectionType(List.class, CommandResult.class));
                        synchronized (resultsSync) {
                            results = resultsFromCsm;
                        }

                        for (CommandResult singleResult : results) {
                            if (!singleResult.getStatus().equals("pending")) {
                                List<UUID> removeWatch = new ArrayList<>();
                                for (Map.Entry<UUID, Peer> entry : watcher.entrySet()) {
                                    if (entry.getKey().equals(singleResult.getCommand().getCommandUUID())) {
                                        String res = "";
                                        if (storage.get(entry.getKey()) instanceof CommandModule) {
                                            CommandModule casted = (CommandModule) storage.get(entry.getKey());
                                            if (casted.isCascade()) {
                                                res = "Команда на рестарт [ \"" + casted.getDeploymentName() + "\" ] на сервере [\""
                                                        + casted.getDomainName() + ":" + casted.getPort() + "\" ] завершена со статусом [ \""
                                                        + singleResult.getStatus() + "\" ] за [ \"" + singleResult.getTime() + "ms \" ]";
                                            } else {
                                                res = "Команда" + casted.getCommand() + " на [ \"" + casted.getDeploymentName() + "\" ] на сервере [ \""
                                                        + casted.getDomainName() + ":" + casted.getPort() + "\" ] завершена со статусом [ \""
                                                        + singleResult.getStatus() + "\" ] за [ \"" + singleResult.getTime() + "ms \" ]";
                                            }
                                        }

                                        if (storage.get(entry.getKey()) instanceof CommandRestartNode) {
                                            CommandRestartNode casted = (CommandRestartNode) storage.get(entry.getKey());
                                            if (casted.isRestart()) {
                                                res = "Команда на рестарт сервера [ \"" + casted.getDomainName() + ":" + casted.getPort() + "\" ] завершена со статусом [ \""
                                                        + singleResult.getStatus() + "\" ] за [\"" + singleResult.getTime() + "ms \" ]";
                                            } else {
                                                res = "Команда на остановку сервера [ \"" + casted.getDomainName() + ":" + casted.getPort() + "\" ] завершена со статусом [ \""
                                                        + singleResult.getStatus() + "\" ] за [ \"" + singleResult.getTime() + "ms \" ]";
                                            }
                                        }
                                        bot.messaging().sendText(entry.getValue(), res);
                                        removeWatch.add(entry.getKey());
                                    }
                                }
                                for (UUID toRemove : removeWatch) {
                                    watcher.remove(toRemove);
                                    storage.remove(toRemove);
                                }
                            }
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 5, 5, TimeUnit.SECONDS);

        ScheduledExecutorService sesForServers = Executors.newSingleThreadScheduledExecutor();

        sesForServers.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                HttpResponse response = null;
                try {
                    response = doCommand("getData", null);

                    if (response.getStatusLine().getStatusCode() == 200) {
                        Cluster resultsFromCsm = om.readValue(EntityUtils.toString(response.getEntity()), Cluster.class);
                        List<Entity> newEntities = new ArrayList<>();
                        for (WildFlyGroup group : resultsFromCsm.getGroups()) {
                            for (WildFlyNode n : group.getNodes()) {
                                newEntities.add(new Entity(n.getDomainName() + ":" + n.getPort(), n.getPrintName(), false));
                            }
                        }
                        printNames = newEntities;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }


    private HttpResponse doCommand(String path, Map<String, String> params) throws IOException {
        HttpPost request = new HttpPost(String.format("http://%s/%s", csmApiConnector, path));
        MultipartEntity multipartEntity = new MultipartEntity();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                try {
                    multipartEntity.addPart(entry.getKey(), new StringBody(entry.getValue()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        request.setEntity(multipartEntity);
        HttpResponse
                httpResponse = client.execute(request);
        return httpResponse;
    }

    public String getCsmStatus() {
        HttpResponse response = null;
        try {
            response = doCommand("getMetainformation", null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response.getStatusLine().getStatusCode() == 200) {
            try {
                MetaInf metaInf = om.readValue(EntityUtils.toString(response.getEntity()), MetaInf.class);
                if (metaInf != null) {
                    return "Csm доступен, версия приложения [ \"" + metaInf.getBuildNumber() + "\" ]";
                }
            } catch (IOException e) {
                return "CSM недоступен =(";
            }
        } else {
            return "CSM недоступен =(";
        }
        return "CSM недоступен =(";
    }

    public String getAllServers() {
        HttpResponse response = null;
        try {
            response = doCommand("getData", null);

            if (response.getStatusLine().getStatusCode() == 200) {
                Cluster cluster = om.readValue(EntityUtils.toString(response.getEntity()), Cluster.class);
                if (cluster != null) {
                    String res = "";
                    for (WildFlyGroup group : cluster.getGroups()) {
                        for (WildFlyNode node : group.getNodes()) {
                            res += String.format("[ WF \"%s:%d\" ] [ из группы: \"%s\" ] [ запущен: \"%b\" ] [ завис: \"%b\" ] [ данные получены за: \"%d\" мс ] [ онлайн: \"%d sec\" ] [ память: \"%d of %d mb\" ]\n",
                                    node.getDomainName(), node.getPort(), group.getName(), node.isReachable(),
                                    node.isPending(), node.getLastPing(), node.getUptime() / 1000,
                                    node.getHeapUsed() / 1000 / 1000, node.getHeapMax() / 1000 / 1000);
                        }
                    }
                    return res;
                }
            } else {
                return "CSM недоступен =(";
            }
        } catch (IOException e) {
            return "CSM недоступен =(";
        }
        return "CSM недоступен =(";
    }

    public String getSingleHostData(String hostPort, Boolean withModules) {
        WildFlyNode node = getWfNodeObject(hostPort);
        String res = "";
        if (node != null) {
            res += String.format("[ WF \"%s:%d\" ] [ запущен: \"%b\" ] [ завис: \"%b\" ] [ данные получены за: \"%d\" мс ] [ онлайн: \"%d sec\" ] [ память: \"%d of %d mb\" ]\n",
                    node.getDomainName(), node.getPort(), node.isReachable(),
                    node.isPending(), node.getLastPing(), node.getUptime() / 1000,
                    node.getHeapUsed() / 1000 / 1000, node.getHeapMax() / 1000 / 1000);
            if (withModules) {
                int i = 0;
                for (Module m : node.getModules()) {
                    i++;
                    res += "\t" + String.format("[%d] [ Имя деплоймента: \"%s\" ] [ имя исполнявмого файла: \"%s\" ] [запущен: \"%s\" ] [ статус: \"%s\" ]\n", i, m.getName(), m.getRuntimeName(), m.getEnabled(), m.getStatus());
                }
            }
            return res;
        } else {
            return "Нет такого сервера " + hostPort;
        }

    }

    public String getModules(String hostPort) {
        WildFlyNode node = getWfNodeObject(hostPort);
        String res = "";
        if (node != null) {
            int i = 0;
            for (Module m : node.getModules()) {
                i++;
                res += String.format("[%d] [ Имя деплоймента: \"%s\" ] [ имя исполнявмого файла: \"%s\" ] [ запущен: \"%s\" ] [ статус: \"%s\" ]\n", i, m.getName(), m.getRuntimeName(), m.getEnabled(), m.getStatus());
            }
            return res;
        } else {
            return "Нет такого сервера " + hostPort;
        }

    }

    public String getSingleModule(String hostPort, String deploymentName) {
        WildFlyNode node = getWfNodeObject(hostPort);
        String res = "";
        if (node != null) {
            boolean found = false;
            for (Module m : node.getModules()) {
                if (m.getName().equals(deploymentName)) {
                    res += String.format("[ Имя деплоймента: \"%s\" ] [ имя исполнявмого файла: \"%s\" ] [ запущен: \"%s\" ] [ статус: \"%s\" ]\n",  m.getName(), m.getRuntimeName(), m.getEnabled(), m.getStatus());
                    found = true;
                    break;
                }
            }
            if (!found) {
                res += String.format("На сервере [\"%s\"] нет [ деплоймента: \"%s\" ] ", hostPort, deploymentName);
            }
            return res;
        } else {
            return "Нет такого сервера " + hostPort;
        }

    }

    private WildFlyNode getWfNodeObject(String hostPort) {
        Map<String, String> params = new HashMap<>();
        String[] hostPortArr = hostPort.split(":");
        WildFlyNode node = null;
        params.put("host", hostPortArr[0]);
        if (hostPortArr.length > 1) {
            params.put("port", hostPortArr[1]);
        } else {
            params.put("port", "9990");
        }
        HttpResponse response = null;
        try {
            response = doCommand("getSingleHostData", params);

            if (response.getStatusLine().getStatusCode() == 200) {
                node = om.readValue(EntityUtils.toString(response.getEntity()), WildFlyNode.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return node;
    }

    public String getServerOptions(String hostPort) {
        WildFlyNode node = getWfNodeObject(hostPort);
        String res = "";
        if (node != null) {
            for (Map.Entry<String, String> entry : node.getInputArgs().entrySet()) {
                if (entry.getValue().equals("{presented}")) {
                    res += entry.getKey() + "=" + entry.getValue() + "\n";
                } else {
                    res += entry.getKey() + "\n";
                }
            }
            for (Map.Entry<String, String> entry : node.getProcessedProps().entrySet()) {
                if (entry.getValue().equals("{presented}")) {
                    res += entry.getKey() + "=" + entry.getValue() + "\n";
                } else {
                    res += entry.getKey() + "\n";
                    ;
                }
            }
        } else res = "Нет такого сервера " + hostPort;
        return res;
    }

    private String provideModules(String hostPort, String deploymentName, String commandType, Boolean cascade, Peer peer) {
        WildFlyNode node = getWfNodeObject(hostPort);
        if (node != null) {
            boolean moduleFound = false;
            for (Module m : node.getModules()) {
                if (m.getName().equals(deploymentName)) {
                    moduleFound = true;
                    break;
                }
            }
            if (!moduleFound) {
                return "Модуля с именем [ \"" + deploymentName + "\" ] нет на узле " + hostPort;
            }
            List<CommandModule> cms = new ArrayList<>();
            CommandModule cm = new CommandModule();
            cm.setCascade(cascade);
            cm.setDeploymentName(deploymentName);
            cm.setCommand(commandType);
            cm.setNodeUUID(node.getUuid());
            cm.setDomainName(node.getDomainName());
            cm.setPort(node.getPort());
            cms.add(cm);
            Map<String, String> params = new HashMap<>();
            params.put("mode", "module");
            try {
                params.put("commandSet", om.writeValueAsString(cms));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            HttpResponse response = null;
            try {
                response = doCommand("command", params);

                if (response.getStatusLine().getStatusCode() == 200) {
                    if (cascade) {
                        commandType = "restart";
                    }
                    List<UUID> resultsFromCsm = null;
                    resultsFromCsm = om.readValue(EntityUtils.toString(response.getEntity()), om.getTypeFactory().constructCollectionType(List.class, UUID.class));
                    if (resultsFromCsm != null)
                        for (UUID fromCsm : resultsFromCsm) {
                            watcher.put(fromCsm, peer);
                            storage.put(fromCsm, cm);
                        }
                    return "Операция по остановке модуля [ \"" + deploymentName + "\" ] на сервере [ \"" + hostPort + "\" ] начата";
                } else if (response.getStatusLine().getStatusCode() == 404) {
                    return "CSM недоступен =(";
                } else {
                    return "Операция по остановке модуля [ \"" + deploymentName + "\" ] на сервере [ \"" + hostPort + "\" ] не начата";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "CSM недоступен =(";
        } else return "Нет такого сервера " + hostPort;
    }

    public String manageNode(String hostPort, Boolean restart, Peer peer) {
        WildFlyNode node = getWfNodeObject(hostPort);
        if (node != null) {

            List<CommandRestartNode> cms = new ArrayList<>();
            CommandRestartNode cm = new CommandRestartNode();
            cm.setRestart(restart);
            cm.setNodeUUID(node.getUuid());
            cm.setCommand("node-restart");
            cm.setDomainName(node.getDomainName());
            cm.setPort(node.getPort());
            cms.add(cm);
            Map<String, String> params = new HashMap<>();
            params.put("mode", "restart-wf");
            try {
                params.put("commandSet", om.writeValueAsString(cms));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            HttpResponse response = null;
            try {
                response = doCommand("command", params);

                if (response.getStatusLine().getStatusCode() == 200) {
                    String commandType;
                    if (restart) {
                        commandType = "restart";
                    } else {
                        commandType = "stop";
                    }
                    List<UUID> resultsFromCsm = null;
                    resultsFromCsm = om.readValue(EntityUtils.toString(response.getEntity()), om.getTypeFactory().constructCollectionType(List.class, UUID.class));
                    if (resultsFromCsm != null)
                        for (UUID fromCsm : resultsFromCsm) {
                            watcher.put(fromCsm, peer);
                            storage.put(fromCsm, cm);
                        }
                    return "Операция [ \"" + commandType + "\" ] на сервере " + hostPort + " начата";
                } else if (response.getStatusLine().getStatusCode() == 404) {
                    return "CSM is unavaliable";
                } else {
                    return "Операция [ \"stop\" ] на сервере " + hostPort + " не начата";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "Операция [\"stop\"] на сервере " + hostPort + " не начата";
        } else return "Нет такого сервера " + hostPort;
    }

    public String stopModule(String hostPort, String deploymentName, Peer peer) {
        return provideModules(hostPort, deploymentName, "stop", false, peer);
    }

    public String startModule(String hostPort, String deploymentName, Peer peer) {
        return provideModules(hostPort, deploymentName, "start", false, peer);
    }

    public String restartModule(String hostPort, String deploymentName, Peer peer) {
        return provideModules(hostPort, deploymentName, "start", true, peer);
    }

}

