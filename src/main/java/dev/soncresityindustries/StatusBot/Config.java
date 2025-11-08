package dev.soncresityindustries.StatusBot;

import dev.soncresityindustries.StatusBot.util.LogUtils;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration management class for the PhoenixBot application.
 * Uses YAML format for configuration storage with automatic default generation.
 * Implements singleton pattern for global configuration access.
 * 
 * @author SkyKing_PX
 */
public class Config {
    /** Path to the configuration file */
    private static final Path CONFIG_PATH = Paths.get("config.yml");
    /** Singleton instance of the configuration */
    private static Config instance;

    /** Bot configuration settings */
    private Bot bot;
    /** Logging configuration */
    private Logging logging;
    /** Status configuration */
    private Status status;
    /** Embed system configuration */
    private Embeds embeds;

    /**
     * Gets the singleton instance of the configuration.
     * Creates and loads the configuration if it doesn't exist.
     * 
     * @return The configuration instance
     * @throws IOException If there is an error loading the configuration file
     */
    public static Config get() throws IOException {
        if (instance == null) {
            instance = new Config();
            instance.load();
        }
        return instance;
    }

    /**
     * Loads the configuration from the YAML file.
     * Creates a default configuration if none exists.
     * 
     * @throws IOException If there is an error reading or parsing the configuration file
     */
    private void load() throws IOException {
        if (Files.notExists(CONFIG_PATH)) {
            createDefaultConfig();
        }

        try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
            LoaderOptions options = new LoaderOptions();
            Constructor constructor = new Constructor(Config.class, options);
            Yaml yaml = new Yaml(constructor);
            Config loaded = yaml.loadAs(input, Config.class);

            this.bot = loaded.bot;
            this.logging = loaded.logging;
            this.status = loaded.status;
            this.embeds = loaded.embeds;
        }
    }

    /**
     * Reloads the configuration from the YAML file.
     * Creates a new instance and replaces the current singleton.
     * 
     * @throws IOException If there is an error loading the configuration file
     */
    public static void reload() throws IOException {
        instance = new Config();
        instance.load();
    }

    /**
     * Creates a default configuration file with template values.
     * 
     * @throws IOException If there is an error writing the configuration file
     */
    private void createDefaultConfig() throws IOException {
        String defaultConfig = """
            bot:
              token: "YOUR_BOT_TOKEN"
              activity: ""
              administratorRoleIDs: ["DISCORD_ROLE_ID"]
              guild_id: "0000000000000000000"
            
            status:
              statusChannelId: "0000000000000000000"
              operationalChannelName: "「🟢」status"
              partialOutageChannelName: "「🟡」status"
              majorOutageChannelName: "「🔴」status"
              maintenanceChannelName: "「🔵」status"

            logging:
              logChannelId: "0000000000000000000"
              fatalLogChannelId: "0000000000000000000"
            
            embeds:
              defaultColor: "#2073cb"
              successColor: "#00ff33"
              errorColor: "#ff0000"
              warningColor: "#ff9900"
              infoColor: "#ffcc33"
              footerText: "SI: Status Bot | Developed by SkyKing_PX"
            """;

        Files.writeString(CONFIG_PATH, defaultConfig);
        LogUtils.logConfig("Created default config file at: " + CONFIG_PATH.toAbsolutePath());
    }

    // --- Configuration Getters and Setters ---

    /** @return Bot configuration */
    public Bot getBot() { return bot; }
    /** @param bot Bot configuration to set */
    public void setBot(Bot bot) { this.bot = bot; }

    /** @return Logging configuration */
    public Logging getLogging() { return logging; }
    /** @param logging Logging configuration to set */
    public void setLogging(Logging logging) { this.logging = logging; }

    /** @return Status configuration */
    public Status getStatus() { return status; }
    /** @param status Logging configuration to set */
    public void setStatus(Status status) { this.status = status; }

    /** @return Embed system configuration */
    public Embeds getEmbeds() { return embeds; }
    /** @param embeds Embed configuration to set */
    public void setEmbeds(Embeds embeds) { this.embeds = embeds; }

    /**
     * Bot-specific configuration settings.
     */
    public static class Bot {
        /** Discord bot token for authentication */
        private String token;
        /** Activity status displayed by the bot */
        private String activity;
        /** Role ID of the people who may operate the bot */
        private String[] administratorRoleIDs;
        /** Discord guild ID where the bot operates */
        private String guild_id;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public String getActivity() { return activity; }
        public void setActivity(String activity) { this.activity = activity; }

        public String[] getAdministratorRoleIDs() { return administratorRoleIDs; }
        public void setAdministratorRoleIDs(String[] administratorRoleIDs) { this.administratorRoleIDs = administratorRoleIDs; }

        public String getGuild_id() { return guild_id; }
        public void setGuild_id(String guild_id) { this.guild_id = guild_id; }
    }

    /**
     * Logging configuration settings.
     */
    public static class Logging {
        /** Discord channel ID for bot logs */
        private String logChannelId;
        private String fatalLogChannelId;
        /** @return Channel ID for logs */
        public String getLogChannelId() { return logChannelId; }
        /** @param logChannelId Channel ID to set for logs */
        public void setLogChannelId(String logChannelId) { this.logChannelId = logChannelId; }
        /** @return Channel ID for fatal bot errors */
        public String getFatalLogChannelId() { return fatalLogChannelId; }
        /** @param fatalLogChannelId Channel ID to set for fatal bot errors */
        public void setFatalLogChannelId(String fatalLogChannelId) { this.fatalLogChannelId = fatalLogChannelId; }
    }

    public static class Status {
        private String statusChannelId;
        private String operationalChannelName;
        private String partialOutageChannelName;
        private String majorOutageChannelName;
        private String maintenanceChannelName;
        public String getStatusChannelId() { return statusChannelId; }
        public void setStatusChannelId(String statusChannelId) { this.statusChannelId = statusChannelId; }

        public String getOperationalChannelName() { return operationalChannelName; }
        public void setOperationalChannelName(String operationalChannelName) { this.operationalChannelName = operationalChannelName; }

        public String getPartialOutageChannelName() { return partialOutageChannelName; }
        public void setPartialOutageChannelName(String partialOutageChannelName) { this.partialOutageChannelName = partialOutageChannelName; }

        public String getMajorOutageChannelName() { return majorOutageChannelName; }
        public void setMajorOutageChannelName(String majorOutageChannelName) { this.majorOutageChannelName = majorOutageChannelName; }

        public String getMaintenanceChannelName() { return maintenanceChannelName; }
        public void setMaintenanceChannelName(String maintenanceChannelName) { this.maintenanceChannelName = maintenanceChannelName; }
    }

    /**
     * Embed system configuration.
     */
    public static class Embeds {
        /** Embed colors */
        private String defaultColor;
        private String successColor;
        private String errorColor;
        private String warningColor;
        private String infoColor;
        /** Embed footer text */
        private String footerText;
        /** @return Default embed color in HEX format */
        public String getDefaultColor() { return defaultColor; }
        /** @param defaultColor Sets default embed color in HEX format */
        public void setDefaultColor(String defaultColor) { this.defaultColor = defaultColor; }
        /** @return Success embed color in HEX format */
        public String getSuccessColor() { return successColor; }
        /** @param successColor Sets success embed color in HEX format */
        public void setSuccessColor(String successColor) { this.successColor = successColor; }
        /** @return Error embed color in HEX format */
        public String getErrorColor() { return errorColor; }
        /** @param errorColor Sets error embed color in HEX format */
        public void setErrorColor(String errorColor) { this.errorColor = errorColor; }
        /** @return Warning embed color in HEX format */
        public String getWarningColor() { return warningColor; }
        /** @param warningColor Sets warning embed color in HEX format */
        public void setWarningColor(String warningColor) { this.warningColor = warningColor; }
        /** @return Info embed color in HEX format */
        public String getInfoColor() { return infoColor; }
        /** @param infoColor Sets info embed color in HEX format */
        public void setInfoColor(String infoColor) { this.infoColor = infoColor; }
        /** @return Default embed Footer Text ({Version} - Bot Version) */
        public String getFooterText() { return footerText; }
        /** @param footerText Sets default embed Footer Text */
        public void setFooterText(String footerText) { this.footerText = footerText; }
    }
}
