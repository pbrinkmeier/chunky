package de.pbrinkmeier.mc.chunky;

import java.util.Arrays;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public final class Chunky extends JavaPlugin implements Listener {
    // Last known location of a player. Represents the chunk they belong to.
    // If unset, the player belongs to the chunk they are currently in.
    // Suffixed with player world.
    // A player "owns" a distinct chunk in every world.
    private static String METADATA_CHUNKY_LOC = "chunky:loc:";

    private final Chunky plugin;

    public Chunky() {
        this.plugin = this;
    }

    @Override
    public void onEnable() {
        // Register as listener
        plugin.getServer().getPluginManager().registerEvents(this, this);

        // Initialize player locations
        for (Player player: getServer().getOnlinePlayers()) {
            plugin.saveOldPosition(player);
        }

        // Every second, keep people from leaving their designated chunk :>
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (Player player: getServer().getOnlinePlayers()) {
                if (plugin.hasSavedOldPostion(player)) {
                    Location oldLocation = plugin.getSavedOldPosition(player);

                    if (!oldLocation.getChunk().equals(player.getLocation().getChunk())) {
                        player.teleport(oldLocation);
                        // Play sound
                        player.playSound(oldLocation, Sound.ENTITY_VILLAGER_AMBIENT, 1.F, 1.F);
                    }
                }
                plugin.saveOldPosition(player);
            }
        }, 0, 20);

        // Add plugin recipes
        addRecipes();
    }

    private void addRecipes() {
        List<Material> fuels = Arrays.asList(
            Material.COAL,
            Material.CHARCOAL
        );
        List<Material> greens = Arrays.asList(
            Material.WHEAT_SEEDS,
            Material.VINE
        );
        List<Material> specials = Arrays.asList(
            Material.SPIDER_EYE,
            Material.GUNPOWDER,
            Material.BONE,
            Material.FLINT,
            Material.QUARTZ,
            Material.ROTTEN_FLESH,
            Material.REDSTONE_BLOCK,
            Material.IRON_NUGGET,
            Material.COPPER_INGOT,
            Material.PUFFERFISH,
            Material.PUMPKIN,
            Material.APPLE,
            Material.LAPIS_LAZULI
        );

        NamespacedKey recipeKey = new NamespacedKey(plugin, "ender_pearl");
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, new ItemStack(Material.ENDER_PEARL));

        recipe.shape("CGC", "GSG", "CGC");
        recipe.setIngredient('C', new RecipeChoice.MaterialChoice(fuels));
        recipe.setIngredient('G', new RecipeChoice.MaterialChoice(greens));
        recipe.setIngredient('S', new RecipeChoice.MaterialChoice(specials));

        plugin.getServer().addRecipe(recipe);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // Allow teleports
        plugin.saveOldPosition(event.getPlayer(), event.getTo());
    }

    private void saveOldPosition(Player player) {
        plugin.saveOldPosition(player, player.getLocation());
    }

    private void saveOldPosition(Player player, Location location) {
        player.removeMetadata(METADATA_CHUNKY_LOC + location.getWorld().getName(), plugin);
        player.setMetadata(METADATA_CHUNKY_LOC + location.getWorld().getName(), new FixedMetadataValue(plugin, location));
    }

    private boolean hasSavedOldPostion(Player player) {
        return player.hasMetadata(METADATA_CHUNKY_LOC + player.getWorld().getName());
    }

    private Location getSavedOldPosition(Player player) {
        return (Location) player.getMetadata(METADATA_CHUNKY_LOC + player.getWorld().getName()).get(0).value();
    }
}