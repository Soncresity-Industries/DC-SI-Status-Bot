package dev.soncresityindustries.StatusBot;

import dev.soncresityindustries.StatusBot.command.CommandRegistry;
import dev.soncresityindustries.StatusBot.storage.StatusStorage;
import dev.soncresityindustries.StatusBot.util.LogUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Main event listener for the PhoenixBot.
 * Handles bot initialization, command registration, and startup tasks.
 *
 * @author SkyKing_PX
 */
public class Listener extends ListenerAdapter {

    /**
     * Handles the bot ready event.
     * Registers slash commands, initializes storage systems, and restores pending tickets.
     *
     * @param event The ReadyEvent from JDA
     */
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        JDA api = event.getJDA();
        LogUtils.logInfo("Registering Commands...");

        api.updateCommands()
                .addCommands(CommandRegistry.registerCommands())
                .queue(success -> LogUtils.logInfo("Global commands updated."));

        try {
            LogUtils.logInfo("Reloading services from storage...");
            StatusStorage.getInstance().reloadServices(api);
        } catch (IOException e) {
            LogUtils.logException("Error reloading services from storage", e);
        }
        LogUtils.logInfo("Successfully reloaded services from storage.");

        LogUtils.logInfo("Bot is ready.");
    }
}