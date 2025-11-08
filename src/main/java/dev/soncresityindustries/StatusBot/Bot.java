package dev.soncresityindustries.StatusBot;

import dev.soncresityindustries.StatusBot.command.StatusCommand;
import dev.soncresityindustries.StatusBot.util.LogUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.IOException;

public class Bot {
    /**
     * Current version of the bot
     */
    public static final String VERSION = "1.0.0";

    /**
     * Main entry point for the application.
     * Initializes storage, configures JDA, and registers all event listeners.
     *
     * @param args Command line arguments (not used)
     * @throws Exception If any error occurs during initialization
     */
    public static void main(String[] args) throws Exception {
        String activity = "Incorrect Configuration";
        try {
            activity = Config.get().getBot().getActivity();
            activity = activity.replace("{Version}", VERSION);
        } catch (IOException e) {
            LogUtils.logException("Error loading Activity from Config. It may be corrupted", e);
        }

        JDA api = JDABuilder.createDefault(Config.get().getBot().getToken())
                .addEventListeners(
                        new StatusCommand(),
                        new Listener())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.playing(activity))
                .setStatus(OnlineStatus.ONLINE)
                .build();
    }
}
