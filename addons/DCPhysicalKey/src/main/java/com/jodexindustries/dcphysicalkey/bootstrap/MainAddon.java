package com.jodexindustries.dcphysicalkey.bootstrap;


import com.jodexindustries.dcphysicalkey.commands.MainCommand;
import com.jodexindustries.dcphysicalkey.config.Config;
import com.jodexindustries.dcphysicalkey.listener.EventListener;
import com.jodexindustries.dcphysicalkey.tools.ItemManager;
import com.jodexindustries.donatecase.api.DCAPI;
import com.jodexindustries.donatecase.api.addon.InternalJavaAddon;
import com.jodexindustries.donatecase.spigot.tools.BukkitUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MainAddon extends InternalJavaAddon {

    public static final DCAPI api = DCAPI.getInstance();

    @Getter
    private Config config;
    private ItemManager itemManager;
    private EventListener eventListener;
    private MainCommand mainCommand;

    @NotNull
    public static final NamespacedKey NAMESPACED_KEY = Objects.requireNonNull(NamespacedKey.fromString("dcphysicalkey:key"));

    @Override
    public void onLoad() {
        this.config = new Config(this);
        this.itemManager = new ItemManager(this);
        this.eventListener = new EventListener(this);
        this.mainCommand = new MainCommand(this, config);
    }

    @Override
    public void onEnable() {
        mainCommand.register();
        api.getEventBus().register(eventListener);

        Bukkit.getServer().getPluginManager().registerEvents(eventListener, BukkitUtils.getDonateCase());

        // --- ПАТЧ: откладываем загрузку ключей на 1 тик, чтобы к этому моменту
        // все material-фабрики (в т.ч. Nexo, ItemsAdder, Oraxen) уже были
        // зарегистрированы в общем MaterialManager ядра. При старте сервера
        // DCPhysicalKey enable'ится раньше финальной регистрации материалов,
        // поэтому синхронная загрузка прямо тут падает с "Material not found".
        Bukkit.getScheduler().runTask(BukkitUtils.getDonateCase(), itemManager::load);
        // --- конец патча ---
    }

    @Override
    public void onDisable() {
        mainCommand.unregister();
        api.getEventBus().unregister(eventListener);
        HandlerList.unregisterAll(eventListener);
    }

}
