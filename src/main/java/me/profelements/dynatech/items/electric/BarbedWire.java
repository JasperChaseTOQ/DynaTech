package me.profelements.dynatech.items.electric;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;

public class BarbedWire extends SlimefunItem implements EnergyNetComponent {

    private static final int DEFAULT_ENTITY_PUSH_FORCE = 10;
    private static final int DEFAULT_ENTITY_DETECT_RANGE = 9;
    private static final int DEFAULT_ENERGY_CONSUMPTION = 16;

    private final ItemSetting<Integer> entityPushForce = new ItemSetting<>(this, "entity-push-force", DEFAULT_ENTITY_PUSH_FORCE);
    private final ItemSetting<Integer> entityDetectRange = new ItemSetting<>(this, "entity-detect-range", DEFAULT_ENTITY_DETECT_RANGE);
    private final ItemSetting<Integer> blockEnergyConsumption = new ItemSetting<>(this, "block-energy-consumption", DEFAULT_ENERGY_CONSUMPTION);

    @ParametersAreNonnullByDefault
    public BarbedWire(ItemGroup group, SlimefunItemStack item,  RecipeType recipeType, ItemStack[] recipe) {
        super(group, item, recipeType, recipe);
        addItemSetting(entityPushForce, entityDetectRange, blockEnergyConsumption);
    }

    private List<EntityType> getEntityWhitelist() {
        ArrayList<EntityType> whitelist = new ArrayList<>();

        whitelist.add(EntityType.ARROW);
        whitelist.add(EntityType.BLAZE);
        whitelist.add(EntityType.CAVE_SPIDER);
        whitelist.add(EntityType.CREEPER);
        whitelist.add(EntityType.DRAGON_FIREBALL);
        whitelist.add(EntityType.DROWNED);
        whitelist.add(EntityType.ELDER_GUARDIAN);
        whitelist.add(EntityType.ENDER_DRAGON);
        whitelist.add(EntityType.ENDERMAN);
        whitelist.add(EntityType.ENDERMITE);
        whitelist.add(EntityType.EVOKER);
        whitelist.add(EntityType.FIREBALL);
        whitelist.add(EntityType.GHAST);
        whitelist.add(EntityType.GIANT);
        whitelist.add(EntityType.GUARDIAN);
        whitelist.add(EntityType.HOGLIN);
        whitelist.add(EntityType.HUSK);
        whitelist.add(EntityType.ILLUSIONER);
        whitelist.add(EntityType.MAGMA_CUBE);
        whitelist.add(EntityType.PHANTOM);
        whitelist.add(EntityType.PIGLIN);
        whitelist.add(EntityType.PIGLIN_BRUTE);
        whitelist.add(EntityType.PILLAGER);
        whitelist.add(EntityType.RAVAGER);
        whitelist.add(EntityType.SHULKER);
        whitelist.add(EntityType.SHULKER_BULLET);
        whitelist.add(EntityType.SILVERFISH);
        whitelist.add(EntityType.SKELETON);
        whitelist.add(EntityType.SLIME);
        whitelist.add(EntityType.SMALL_FIREBALL);
        whitelist.add(EntityType.SPIDER);
        whitelist.add(EntityType.STRAY);
        whitelist.add(EntityType.VEX);
        whitelist.add(EntityType.VINDICATOR);
        whitelist.add(EntityType.WARDEN);
        whitelist.add(EntityType.WITCH);
        whitelist.add(EntityType.WITHER);
        whitelist.add(EntityType.WITHER_SKELETON);
        whitelist.add(EntityType.WITHER_SKULL);
        whitelist.add(EntityType.ZOGLIN);
        whitelist.add(EntityType.ZOMBIE);
        whitelist.add(EntityType.ZOMBIFIED_PIGLIN);

        return whitelist;
    }

    @Override
    public int getCapacity() {
        return 128;
    }

    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    @Override
    public void preRegister() {
        super.preRegister();

        addItemHandler(new BlockTicker() {
            @Override
            public boolean isSynchronized() {
               return true;
            }

             @Override
             public void tick(Block block, SlimefunItem item, Config conf) {
                 BarbedWire.this.tick(block);
             }
        });
    }

    protected void tick(Block block) {
        int entityDetectionRange = entityDetectRange.getValue();
        int energyConsumption = blockEnergyConsumption.getValue();
        List<EntityType> entityTypeWhitelist = getEntityWhitelist();
        Location loc = block.getLocation();
        if (getCharge(loc) >= energyConsumption) {
            // Intentional. Remove it per tick whether or not it shields from mobs.
            removeCharge(loc, energyConsumption);
            for (Entity e : block.getWorld().getNearbyEntities(loc, entityDetectionRange, entityDetectionRange, entityDetectionRange)) {
                if (entityTypeWhitelist.contains(e.getType())) {

                    /// Find the Direction Vector. normailze it. multiply by the push force. apply it to the entity.
                    Vector launchDirection = loc.subtract(e.getLocation()).toVector().normalize().multiply(entityPushForce.getValue());

                    /// If any of the components are not finite just set it to zero.
                    if (NumberConversions.isFinite(launchDirection.getX()) && NumberConversions.isFinite(launchDirection.getY())
                        && NumberConversions.isFinite(launchDirection.getZ())) {
                        e.setVelocity(launchDirection);
                    } else {
                        e.setVelocity(new Vector(0, 0, 0));
                    }
                }
            }
        }
    }
}
