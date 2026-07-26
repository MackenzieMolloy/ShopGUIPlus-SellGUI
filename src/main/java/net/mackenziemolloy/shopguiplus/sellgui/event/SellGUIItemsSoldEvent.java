package net.mackenziemolloy.shopguiplus.sellgui.event;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.brcdev.shopgui.economy.EconomyType;
import net.mackenziemolloy.shopguiplus.sellgui.objects.SoldItemEntry;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public final class SellGUIItemsSoldEvent extends PlayerEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final List<SoldItemEntry> soldItems;
    private final Map<EconomyType, Double> earnings;
    private final double totalEarnings;
    private final int totalItemCount;

    public SellGUIItemsSoldEvent(
            @NotNull Player player,
            @NotNull List<SoldItemEntry> soldItems,
            @NotNull Map<EconomyType, Double> earnings,
            double totalEarnings,
            int totalItemCount) {
        super(player);
        this.soldItems = Collections.unmodifiableList(soldItems);
        this.earnings = Collections.unmodifiableMap(new EnumMap<>(earnings));
        this.totalEarnings = totalEarnings;
        this.totalItemCount = totalItemCount;
    }

    public @NotNull List<SoldItemEntry> getSoldItems() {
        return soldItems;
    }

    public @NotNull Map<EconomyType, Double> getEarnings() {
        return earnings;
    }

    public double getTotalEarnings() {
        return totalEarnings;
    }

    public int getTotalItemCount() {
        return totalItemCount;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
