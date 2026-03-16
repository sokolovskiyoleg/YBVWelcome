package org.yabogvk.ybvwelcome.core;

import org.bukkit.entity.Player;
import org.yabogvk.ybvwelcome.service.WelcomeService;

public class WelcomeCore {
    private final WelcomeService welcomeService;

    public WelcomeCore(WelcomeService welcomeService) {
        this.welcomeService = welcomeService;
    }

    public void loadPlayerData(Player player) {
        welcomeService.loadPlayerData(player);
    }

    public void loadAndJoin(Player player, boolean isFirstJoin) {
        welcomeService.handleJoin(player, isFirstJoin);
    }

    public void handlePlayerJoin(Player player) {
        welcomeService.handleJoin(player, false);
    }

    public void handlePlayerQuit(Player player) {
        welcomeService.handleQuit(player);
    }

    public void handlePlayerFirstJoin(Player player) {
        welcomeService.handleFirstJoin(player);
    }

    public void setPlayerWelcomeMessage(Player player, String message) {
        welcomeService.setPlayerWelcomeMessage(player, message);
    }

    public void setPlayerQuitMessage(Player player, String message) {
        welcomeService.setPlayerQuitMessage(player, message);
    }

    public void clearPlayerJoinMessage(Player player) {
        welcomeService.clearPlayerJoinMessage(player);
    }

    public void clearPlayerQuitMessage(Player player) {
        welcomeService.clearPlayerQuitMessage(player);
    }

    public void close() {
        welcomeService.close();
    }

    public void reload() {
        welcomeService.reload();
    }
}
