package dev.soncresityindustries.StatusBot.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.soncresityindustries.StatusBot.StatusUpdateManager;
import net.dv8tion.jda.api.JDA;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON-based storage system for managing service statuses.
 * Thread-safe and persistent.
 *
 * @author SkyKing_PX
 */
public class StatusStorage {

    private static StatusStorage instance;

    private final File file = new File("status.json");
    private final ObjectMapper mapper = new ObjectMapper();
    private ObjectNode root;
    private final Map<String, Service> cache = new HashMap<>();

    private StatusStorage() throws IOException {
        if (!file.exists()) {
            root = mapper.createObjectNode();
            save();
        } else {
            root = (ObjectNode) mapper.readTree(file);
            root.fields().forEachRemaining(entry -> {
                try {
                    Service s = mapper.treeToValue(entry.getValue(), Service.class);
                    cache.put(entry.getKey(), s);
                } catch (Exception ignored) {}
            });
        }
    }

    public static synchronized StatusStorage getInstance() {
        if (instance == null) {
            try {
                instance = new StatusStorage();
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize StatusStorage", e);
            }
        }
        return instance;
    }

    public synchronized void addService(Service service, JDA jda) throws IOException {
        cache.put(service.getServiceId(), service);
        root.set(service.getServiceId(), mapper.valueToTree(service));
        save();
        reloadServices(jda);
    }

    public synchronized void removeService(String serviceId, JDA jda) throws IOException {
        cache.remove(serviceId);
        root.remove(serviceId);
        save();
        reloadServices(jda);
    }

    public synchronized void updateService(String serviceId, String newStatus, String newDescription, String newOutageDescription, JDA jda) throws IOException {
        Service existing = cache.get(serviceId);
        if (existing != null) {
            Service updated = existing.withUpdatedStatus(newStatus, newDescription, newOutageDescription);
            cache.put(serviceId, updated);
            root.set(serviceId, mapper.valueToTree(updated));
            save();
            reloadServices(jda);
        }
    }

    public synchronized void reloadServices(JDA jda) throws IOException {
        StatusUpdateManager.updateStatusMessages(jda, this);
    }

    public synchronized Service getService(String serviceId) {
        return cache.get(serviceId);
    }

    public synchronized Collection<Service> getAllServices() {
        return cache.values();
    }

    private synchronized void save() throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
    }
}
