package net.daechler.domainbungeeserver;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;

public class DomainBungeeServer extends Plugin implements Listener {

    private JsonObject config;

    @Override
    public void onEnable() {
        getProxy().getConsole().sendMessage(ChatColor.GREEN + "DomainBungeeServer has been enabled!");
        getProxy().getPluginManager().registerListener(this, this);
        loadConfig();
    }

    @Override
    public void onDisable() {
        getProxy().getConsole().sendMessage(ChatColor.RED + "DomainBungeeServer has been disabled!");
    }

    private void loadConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File configFile = new File(getDataFolder(), "config.json");
        if (!configFile.exists()) {
            try (InputStream in = getResourceAsStream("config.json")) {
                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Could not save config to " + configFile, e);
            }
        }

        try (FileReader reader = new FileReader(configFile)) {
            config = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load config from " + configFile, e);
        }
    }

    @EventHandler
    public void onPlayerConnect(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        String domain = player.getPendingConnection().getVirtualHost().getHostName();
        getLogger().info("Player connected from domain: " + domain);

        if (config != null) {
            JsonObject domains = config.getAsJsonObject("domains");
            String serverName = domains.has(domain) ? domains.get(domain).getAsString() : null;

            if (serverName != null) {
                getLogger().info("Player should be moved to server: " + serverName);
                ServerInfo target = ProxyServer.getInstance().getServerInfo(serverName);
                if (target != null) {
                    player.connect(target);
                } else {
                    getLogger().info("Server " + serverName + " not found.");
                }
            } else {
                getLogger().info("No server mapping found for domain: " + domain);
            }
        }
    }
}
