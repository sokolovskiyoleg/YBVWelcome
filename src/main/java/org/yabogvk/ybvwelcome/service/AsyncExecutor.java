package org.yabogvk.ybvwelcome.service;

import org.bukkit.Bukkit;
import org.yabogvk.ybvwelcome.YBVWelcome;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class AsyncExecutor {
    private final YBVWelcome plugin;

    public AsyncExecutor(YBVWelcome plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Void> runIo(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, task -> Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    public <T> CompletableFuture<T> supplyIo(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, task -> Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    public void runSync(Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
            return;
        }

        Bukkit.getScheduler().runTask(plugin, runnable);
    }
}
