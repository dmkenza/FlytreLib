package net.flytre.flytre_lib.api.base.util;

import net.flytre.flytre_lib.api.config.reference.block.ConfigBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class EntityUtils {

    private EntityUtils() {
        throw new AssertionError();
    }

    public static HitResult raycastNoFluid(Entity entity, double maxDistance) {
        Vec3d origin = new Vec3d(entity.getX(), entity.getY() + entity.getEyeHeight(entity.getPose()), entity.getZ());
        return entity.getEntityWorld().raycast(new RaycastContext(origin, origin.add(entity.getRotationVector().normalize().multiply(maxDistance)), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, entity));
    }

    /**
     * @param entity
     * @param maxDistance
     * @param thruBlocks  These blocks are ignored by the raycast, it will go through them.
     * @return
     */
    public static HitResult raycastNoFluid(Entity entity, double maxDistance, Set<ConfigBlock> thruBlocks) {
        Vec3d origin = new Vec3d(entity.getX(), entity.getY() + entity.getEyeHeight(entity.getPose()), entity.getZ());
        Set<Block> filtered = ConfigBlock.values(thruBlocks, entity.getEntityWorld());
        return entity.getEntityWorld().raycast(new FilteredRaycastContext(origin, origin.add(entity.getRotationVector().normalize().multiply(maxDistance)), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, entity, filtered));
    }

    /**
     * @return ALL entities that the player is currently looking at (a line out from the crosshair would intersect with the entity)
     */
    public static Set<Entity> getEntitiesLookedAt(Entity looker, double maxDistance, Set<ConfigBlock> thruBlocks) {
        return getEntityLookedAtHelper(looker, maxDistance, thruBlocks).all;
    }

    /**
     * @return The entity that the player's crosshair is directly over
     */
    public static Entity getEntityLookedAt(Entity looker, double maxDistance, Set<ConfigBlock> thruBlocks) {
        return getEntityLookedAtHelper(looker, maxDistance, thruBlocks).main;
    }

    /**
     * @return ALL entities that the player is currently looking at (a line out from the crosshair would intersect with the entity)
     */
    public static Set<Entity> getEntitiesLookedAt(Entity looker, double maxDistance) {
        return getEntityLookedAtHelper(looker, maxDistance, null).all;
    }

    /**
     * @return The entity that the player's crosshair is directly over
     */
    public static Entity getEntityLookedAt(Entity looker, double maxDistance) {
        return getEntityLookedAtHelper(looker, maxDistance, null).main;
    }

    private static LookedAtEntities getEntityLookedAtHelper(Entity looker, double maxDistance, @Nullable Set<ConfigBlock> thruBlocks) {

        HitResult hitResult = EntityUtils.raycastNoFluid(looker, maxDistance);
        Vec3d lookerPosition = looker.getPos().add(0, looker.getEyeHeight(looker.getPose()), 0);

        double distance = hitResult == null ? maxDistance : hitResult.getPos().distanceTo(lookerPosition);


        Vec3d length = new Vec3d(looker.getRotationVector().x * maxDistance, looker.getRotationVector().y * maxDistance, looker.getRotationVector().z * maxDistance);
        Vec3d end = lookerPosition.add(length);


        Entity selectedEntity = null;
        Entity foundEntity = null;
        Set<Entity> foundEntities = new HashSet<>();
        double distanceToSelected = distance;

        for (Entity entity : looker.getEntityWorld().getOtherEntities(looker, looker.getBoundingBox().stretch(length).expand(1.0F))) {
            if (entity.isCollidable()) {
                Box collisionBox = entity.getVisibilityBoundingBox();
                Optional<Vec3d> intercept = collisionBox.raycast(lookerPosition, end);
                if (collisionBox.contains(lookerPosition)) {
                    if (distanceToSelected >= 0) {
                        selectedEntity = entity;
                        distanceToSelected = 0;
                    }
                } else if (intercept.isPresent()) {
                    double currDist = lookerPosition.distanceTo(intercept.get());
                    if (currDist < distanceToSelected || distanceToSelected == 0) {
                        selectedEntity = entity;
                        distanceToSelected = currDist;
                    }
                }
            }
            if (selectedEntity != null && (distanceToSelected < distance || hitResult == null)) {
                foundEntity = selectedEntity;
                foundEntities.add(selectedEntity);

            }
        }
        return new LookedAtEntities(foundEntity, foundEntities);
    }

    /**
     * @return the yaw and pitch of the entity needed to face directly towards location
     */
    public static Vec2f getFacingAngle(Entity entity, Vec3d location) {
        Vec3d vec3d = entity.getPos();
        double d = location.x - vec3d.x;
        double e = location.y - vec3d.y;
        double f = location.z - vec3d.z;
        double g = Math.sqrt(d * d + f * f);
        float h = MathHelper.wrapDegrees((float) (-(MathHelper.atan2(e, g) * 57.2957763671875D)));
        float i = MathHelper.wrapDegrees((float) (MathHelper.atan2(f, d) * 57.2957763671875D) - 90.0F);
        return new Vec2f(h, i);
    }


    private record LookedAtEntities(Entity main, Set<Entity> all) {

    }


}
