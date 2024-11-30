package chylex.hee.mechanics.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import chylex.hee.entity.mob.EntityMobHomelandEnderman;
import chylex.hee.entity.technical.EntityTechnicalBiomeInteraction;
import chylex.hee.system.util.ColorUtil;
import chylex.hee.world.structure.island.biome.interaction.BiomeInteractionEnchantedIsland;

public final class HomelandEndermen {

    public enum HomelandRole {

        WORKER(227),
        ISLAND_LEADERS(58),
        GUARD(0),
        COLLECTOR(176),
        OVERWORLD_EXPLORER(141),
        BUSINESSMAN(335),
        INTELLIGENCE(275);

        public static final HomelandRole[] values = values();

        public static HomelandRole getRandomRole(Random rand) {
            HomelandRole role = HomelandRole.WORKER;

            if (rand.nextInt(10) == 0) role = HomelandRole.OVERWORLD_EXPLORER;
            else if (rand.nextInt(7) == 0) role = HomelandRole.BUSINESSMAN;
            else if (rand.nextInt(6) == 0) role = HomelandRole.COLLECTOR;
            else if (rand.nextInt(5) == 0) role = HomelandRole.INTELLIGENCE;
            else if (rand.nextInt(7) <= 2) role = HomelandRole.GUARD;

            return role;
        }

        public final float red, green, blue;

        HomelandRole(int hue) {
            float[] col = ColorUtil.hsvToRgb(hue / 359F, 0.78F, 0.78F);
            red = col[0];
            green = col[1];
            blue = col[2];
        }
    }

    public enum OvertakeGroupRole {

        LEADER,
        CHAOSMAKER,
        FIGHTER,
        TELEPORTER;

        public static final OvertakeGroupRole[] values = values();

        public static OvertakeGroupRole getRandomMember(Random rand) {
            int r = rand.nextInt(20);

            if (r < 12) return FIGHTER;
            else if (r < 17) return TELEPORTER;
            else return CHAOSMAKER;
        }
    }

    public enum EndermanTask {
        NONE,
        RECRUIT_TO_GROUP,
        LISTEN_TO_RECRUITER,
        STROLL,
        WALK,
        COMMUNICATE,
        WAIT,
        GET_TNT
    }

    public static boolean isOvertakeHappening(EntityMobHomelandEnderman source) {
        return getOvertakeGroup(source) != -1;
    }

    public static long getOvertakeGroup(EntityMobHomelandEnderman source) {
        List<EntityTechnicalBiomeInteraction> list = source.worldObj.getEntitiesWithinAABB(
                EntityTechnicalBiomeInteraction.class,
                source.boundingBox.expand(260D, 128D, 260D));

        if (!list.isEmpty()) {
            for (EntityTechnicalBiomeInteraction entity : list) {
                if (entity.getInteractionType() == BiomeInteractionEnchantedIsland.InteractionOvertake.class
                        && entity.ticksExisted > 2) {
                    return ((BiomeInteractionEnchantedIsland.InteractionOvertake) entity.getInteraction()).groupId;
                }
            }
        }

        return -1;
    }

    public static List<EntityMobHomelandEnderman> getAll(EntityMobHomelandEnderman source) {
        List<EntityMobHomelandEnderman> all = source.worldObj
                .getEntitiesWithinAABB(EntityMobHomelandEnderman.class, source.boundingBox.expand(260D, 128D, 260D));
        return all;
    }

    public static List<EntityMobHomelandEnderman> getByHomelandRole(EntityMobHomelandEnderman source,
            HomelandRole role) {
        List<EntityMobHomelandEnderman> all = getAll(source);
        List<EntityMobHomelandEnderman> filtered = new ArrayList<>();

        for (EntityMobHomelandEnderman enderman : all) {
            if (enderman.getHomelandRole() == role) filtered.add(enderman);
        }

        return filtered;
    }

    public static List<EntityMobHomelandEnderman> getInSameGroup(EntityMobHomelandEnderman source) {
        List<EntityMobHomelandEnderman> all = getAll(source);
        List<EntityMobHomelandEnderman> filtered = new ArrayList<>();

        for (EntityMobHomelandEnderman enderman : all) {
            if (enderman.isInSameGroup(source)) filtered.add(enderman);
        }

        return filtered;
    }

    public static List<EntityMobHomelandEnderman> getByGroupRole(EntityMobHomelandEnderman source,
            OvertakeGroupRole role) {
        List<EntityMobHomelandEnderman> all = getAll(source);
        List<EntityMobHomelandEnderman> filtered = new ArrayList<>();

        for (EntityMobHomelandEnderman enderman : all) {
            if (enderman.isInSameGroup(source) && enderman.getGroupRole() == role) filtered.add(enderman);
        }

        return filtered;
    }

    private HomelandEndermen() {}
}
