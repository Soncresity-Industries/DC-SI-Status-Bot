package dev.soncresityindustries.StatusBot.command;

import dev.soncresityindustries.StatusBot.Config;
import dev.soncresityindustries.StatusBot.storage.Service;
import dev.soncresityindustries.StatusBot.storage.StatusStorage;
import dev.soncresityindustries.StatusBot.util.EmbedUtils;
import dev.soncresityindustries.StatusBot.util.LogUtils;
import dev.soncresityindustries.StatusBot.util.MessageHandler;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatusCommand extends ListenerAdapter {

    private final StatusStorage storage = StatusStorage.getInstance();

    public StatusCommand() {
    }

    private static String evalStatus(String status) {
        return switch (status) {
            case "operational" -> "🟢 Operational";
            case "degraded" -> "🟡 Degraded Performance";
            case "partial_outage" -> "🟡 Partial Outage";
            case "major_outage" -> "🔴 Major Outage";
            case "maintenance" -> "🔵 Maintenance";
            default -> "⚪ Unknown Status";
        };
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("status")) return;
        event.deferReply(true).queue();

        try {
            String[] adminRoleIds = Config.get().getBot().getAdministratorRoleIDs();
            boolean hasAdminRole = false;

            var member = event.getMember();
            if (member != null) {
                for (String adminRoleId : adminRoleIds) {
                    if (member.getRoles().stream().anyMatch(role -> role.getId().equals(adminRoleId))) {
                        hasAdminRole = true;
                        break;
                    }
                }
            }

            if (!hasAdminRole) {
                event.getHook().sendMessageEmbeds(EmbedUtils.createSimpleError("❌ You are not authorized to use this command."))
                        .setEphemeral(true).queue();
                return;
            }
        } catch (Exception e) {
            LogUtils.logException("Error checking admin roles", e);
            return;
        }

        String sub = event.getSubcommandName();
        MessageEmbed embed;

        try {
            switch (sub) {
                case "add" -> {
                    String displayName = event.getOption("displayname").getAsString();
                    String id = event.getOption("serviceid").getAsString();
                    String status = "🟢 Operational";
                    String outageDescription = "";
                    String description = event.getOption("description").getAsString();
                    String parentId = event.getOption("parentid") != null
                            ? event.getOption("parentid").getAsString()
                            : null;

                    if (parentId != null && storage.getService(parentId) == null) {
                        embed = EmbedUtils.createErrorEmbed("Error", "Parent service with ID `" + parentId + "` not found.");
                        break;
                    }

                    Service service = new Service(displayName, id, status, outageDescription, description, parentId);
                    storage.addService(service, event.getJDA());

                    embed = EmbedUtils.createSuccess()
                            .setTitle("Service added successfully")
                            .setDescription("✅ Service added: " + displayName +
                                    (parentId != null ? " (child of `" + parentId + "`)" : ""))
                            .build();
                }

                case "update" -> {
                    String id = event.getOption("serviceid").getAsString();
                    String status = evalStatus(event.getOption("status").getAsString());
                    String description = "";
                    try {
                        description = event.getOption("description").getAsString();
                    } catch (NullPointerException ignored) {}
                    String outageDescription = "";
                    try {
                        outageDescription = event.getOption("outage-description").getAsString();
                    } catch (NullPointerException ignored) {}

                    if (description.isEmpty()) {
                        description = storage.getService(id).getDescription();
                    }

                    if (outageDescription.isEmpty()) {
                        outageDescription = storage.getService(id).getOutageDescription();
                    }

                    storage.updateService(id, status, description, outageDescription, event.getJDA());
                    embed = EmbedUtils.createSuccess().setTitle("Service updated successfully")
                            .setDescription("✅ Updated service `" + id + "`").build();
                }
                case "remove" -> {
                    String id = event.getOption("serviceid").getAsString();
                    storage.removeService(id, event.getJDA());
                    embed = EmbedUtils.createSuccess().setTitle("Service removed successfully")
                            .setDescription("🗑️ Removed service `" + id + "`").build();
                }
                case "list" -> {
                    String description = buildHierarchicalList(storage.getAllServices());
                    embed = EmbedUtils.createDefault()
                            .setTitle("📋 Registered Services")
                            .setDescription(description)
                            .build();
                }
                default -> embed = EmbedUtils.createErrorEmbed("Error", "Unknown subcommand");
            }

        } catch (Exception e) {
            embed = EmbedUtils.createErrorEmbed("Error", e.getMessage());
        }

        MessageHandler.sendPreparedMessage(event, embed);
    }

    private String buildHierarchicalList(Collection<Service> services) {
        if (services.isEmpty()) return "No services found.";

        // Group by parent ID
        Map<String, List<Service>> grouped = services.stream()
                .collect(Collectors.groupingBy(s -> s.hasParent() ? s.getParentId() : "root"));

        StringBuilder sb = new StringBuilder();
        List<Service> roots = grouped.getOrDefault("root", List.of());

        for (Service root : roots) {
            appendService(sb, root, grouped, 0);
        }

        return sb.toString();
    }

    private void appendService(StringBuilder sb, Service service, Map<String, List<Service>> grouped, int depth) {
        String indent = "  ".repeat(depth);
        sb.append(indent)
                .append("• **").append(service.getDisplayName()).append("**")
                .append(" (**ID:** `").append(service.getServiceId()).append("` | **Status:** `").append(service.getStatus()).append("`)")
                .append("\n")
                .append(indent).append("  ").append(service.getDescription()).append("\n");

        List<Service> children = grouped.getOrDefault(service.getServiceId(), List.of());
        for (Service child : children) {
            appendService(sb, child, grouped, depth + 1);
        }
    }
}
