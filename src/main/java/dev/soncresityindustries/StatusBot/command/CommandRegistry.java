package dev.soncresityindustries.StatusBot.command;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.List;

/**
 * Registry for all Discord slash commands supported by StatusBot.
 * Centralizes command definition and configuration.
 *
 * @author SkyKing_PX
 */
public class CommandRegistry {

    /**
     * Registers and configures all slash commands for the bot.
     *
     * @return List of CommandData objects to register with Discord
     */
    public static List<CommandData> registerCommands() {
        OptionData statusOption = new OptionData(OptionType.STRING, "status", "Service status", true)
                .addChoice("Operational", "operational")
                .addChoice("Degraded Performance", "degraded")
                .addChoice("Partial Outage", "partial_outage")
                .addChoice("Major Outage", "major_outage")
                .addChoice("Maintenance", "maintenance");

        CommandData status = Commands.slash("status", "Manage or view service statuses")
                .addSubcommands(
                        new SubcommandData("add", "Add a new service")
                                .addOption(OptionType.STRING, "displayname", "Display name of the service", true)
                                .addOption(OptionType.STRING, "serviceid", "Unique ID of the service", true)
                                .addOption(OptionType.STRING, "description", "Short description of the service", true)
                                .addOption(OptionType.STRING, "parentid", "Optional parent service ID", false),

                        new SubcommandData("update", "Update the status of an existing service")
                                .addOption(OptionType.STRING, "serviceid", "Service ID to update", true)
                                .addOptions(statusOption)
                                .addOption(OptionType.STRING, "description", "New description", false)
                                .addOption(OptionType.STRING, "outage-description", "Outage description (Use \"Remove Outage Description\" to clear it)", false)
                                .addOption(OptionType.BOOLEAN, "remove-outage-description", "Remove the current outage description", false),

                        new SubcommandData("remove", "Remove a service from the list")
                                .addOption(OptionType.STRING, "serviceid", "Service ID to remove", true),

                        new SubcommandData("list", "List all registered services and their statuses")
                );

        return List.of(status);
    }
}