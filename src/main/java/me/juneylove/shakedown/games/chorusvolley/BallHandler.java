package me.juneylove.shakedown.games.chorusvolley;

import me.juneylove.shakedown.games.Games;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.*;
import oshi.util.tuples.Pair;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class BallHandler {

    ChorusVolleyScore score;

    ShulkerBullet bullet = null;
    BlockDisplay ball = null;

    Vector previousBulletVelocity;
    Location previousBulletLocation;

    boolean paused = false;

    // radians per tick. yep.
    Vector3f w = new Vector3f();
    final double maxW = 5.0;

    Vector pausedVelocity = null;

    // kinda dumb that these have to be smaller than the actual size of the ball but whatever
    final double innerRadius = 0.75;
    final double outerRadius = 1.0;
    final Vector3d[] vertices = new Vector3d[]{  new Vector3d(innerRadius, innerRadius, outerRadius), // 0
                                                        new Vector3d(innerRadius, outerRadius, innerRadius), // 1
                                                        new Vector3d(outerRadius, innerRadius, innerRadius), // 2
                                            
                                                        new Vector3d(innerRadius, innerRadius, -outerRadius), // 3
                                                        new Vector3d(innerRadius, outerRadius, -innerRadius), // 4
                                                        new Vector3d(outerRadius, innerRadius, -innerRadius), // 5
                                            
                                                        new Vector3d(innerRadius, -innerRadius, -outerRadius), // 6
                                                        new Vector3d(innerRadius, -outerRadius, -innerRadius), // 7
                                                        new Vector3d(outerRadius, -innerRadius, -innerRadius), // 8
                                            
                                                        new Vector3d(innerRadius, -innerRadius, outerRadius), // 9
                                                        new Vector3d(innerRadius, -outerRadius, innerRadius), // 10
                                                        new Vector3d(outerRadius, -innerRadius, innerRadius), // 11
                                            
                                                        new Vector3d( -innerRadius, innerRadius, outerRadius), // 12
                                                        new Vector3d( -innerRadius, outerRadius, innerRadius), // 13
                                                        new Vector3d( -outerRadius, innerRadius, innerRadius), // 14
                                            
                                                        new Vector3d( -innerRadius, innerRadius, -outerRadius), // 15
                                                        new Vector3d( -innerRadius, outerRadius, -innerRadius), // 16
                                                        new Vector3d( -outerRadius, innerRadius, -innerRadius), // 17
                                            
                                                        new Vector3d( -innerRadius, -innerRadius, -outerRadius), // 18
                                                        new Vector3d( -innerRadius, -outerRadius, -innerRadius), // 19
                                                        new Vector3d( -outerRadius, -innerRadius, -innerRadius), // 20
                                            
                                                        new Vector3d( -innerRadius, -innerRadius, outerRadius), // 21
                                                        new Vector3d( -innerRadius, -outerRadius, innerRadius), // 22
                                                        new Vector3d( -outerRadius, -innerRadius, innerRadius)}; // 23

    // kinda dumb that these have to be smaller than the actual size of the ball but whatever
    final double innerCollisionRadius = 0.65;
    final double outerCollisionRadius = 0.82;
    final Vector3d[] collisionVertices = new Vector3d[]{ new Vector3d(innerCollisionRadius, innerCollisionRadius, outerCollisionRadius), // 0
                                                                new Vector3d(innerCollisionRadius, outerCollisionRadius, innerCollisionRadius), // 1
                                                                new Vector3d(outerCollisionRadius, innerCollisionRadius, innerCollisionRadius), // 2
        
                                                                new Vector3d(innerCollisionRadius, innerCollisionRadius, -outerCollisionRadius), // 3
                                                                new Vector3d(innerCollisionRadius, outerCollisionRadius, -innerCollisionRadius), // 4
                                                                new Vector3d(outerCollisionRadius, innerCollisionRadius, -innerCollisionRadius), // 5
        
                                                                new Vector3d(innerCollisionRadius, -innerCollisionRadius, -outerCollisionRadius), // 6
                                                                new Vector3d(innerCollisionRadius, -outerCollisionRadius, -innerCollisionRadius), // 7
                                                                new Vector3d(outerCollisionRadius, -innerCollisionRadius, -innerCollisionRadius), // 8
        
                                                                new Vector3d(innerCollisionRadius, -innerCollisionRadius, outerCollisionRadius), // 9
                                                                new Vector3d(innerCollisionRadius, -outerCollisionRadius, innerCollisionRadius), // 10
                                                                new Vector3d(outerCollisionRadius, -innerCollisionRadius, innerCollisionRadius), // 11
        
                                                                new Vector3d( -innerCollisionRadius, innerCollisionRadius, outerCollisionRadius), // 12
                                                                new Vector3d( -innerCollisionRadius, outerCollisionRadius, innerCollisionRadius), // 13
                                                                new Vector3d( -outerCollisionRadius, innerCollisionRadius, innerCollisionRadius), // 14
        
                                                                new Vector3d( -innerCollisionRadius, innerCollisionRadius, -outerCollisionRadius), // 15
                                                                new Vector3d( -innerCollisionRadius, outerCollisionRadius, -innerCollisionRadius), // 16
                                                                new Vector3d( -outerCollisionRadius, innerCollisionRadius, -innerCollisionRadius), // 17
        
                                                                new Vector3d( -innerCollisionRadius, -innerCollisionRadius, -outerCollisionRadius), // 18
                                                                new Vector3d( -innerCollisionRadius, -outerCollisionRadius, -innerCollisionRadius), // 19
                                                                new Vector3d( -outerCollisionRadius, -innerCollisionRadius, -innerCollisionRadius), // 20
        
                                                                new Vector3d( -innerCollisionRadius, -innerCollisionRadius, outerCollisionRadius), // 21
                                                                new Vector3d( -innerCollisionRadius, -outerCollisionRadius, innerCollisionRadius), // 22
                                                                new Vector3d( -outerCollisionRadius, -innerCollisionRadius, innerCollisionRadius)}; // 23

    final List<Pair<Integer, Integer>> edges = new ArrayList<>();
    {
        edges.add(new Pair<>(0, 1));
        edges.add(new Pair<>(1, 2));
        edges.add(new Pair<>(2, 0));

        edges.add(new Pair<>(3, 4));
        edges.add(new Pair<>(4, 5));
        edges.add(new Pair<>(5, 3));

        edges.add(new Pair<>(6, 7));
        edges.add(new Pair<>(7, 8));
        edges.add(new Pair<>(8, 6));

        edges.add(new Pair<>(9, 10));
        edges.add(new Pair<>(10, 11));
        edges.add(new Pair<>(11, 9));

        edges.add(new Pair<>(12, 13));
        edges.add(new Pair<>(13, 14));
        edges.add(new Pair<>(14, 12));

        edges.add(new Pair<>(15, 16));
        edges.add(new Pair<>(16, 17));
        edges.add(new Pair<>(17, 15));

        edges.add(new Pair<>(18, 19));
        edges.add(new Pair<>(19, 20));
        edges.add(new Pair<>(20, 18));

        edges.add(new Pair<>(21, 22));
        edges.add(new Pair<>(22, 23));
        edges.add(new Pair<>(23, 21));


        edges.add(new Pair<>(2, 5)); // +x face
        edges.add(new Pair<>(5, 8));
        edges.add(new Pair<>(8, 11));
        edges.add(new Pair<>(11, 2));

        edges.add(new Pair<>(14, 17)); // -x face
        edges.add(new Pair<>(17, 20));
        edges.add(new Pair<>(20, 23));
        edges.add(new Pair<>(23, 14));

        edges.add(new Pair<>(1, 4)); // +y face
        edges.add(new Pair<>(4, 13));
        edges.add(new Pair<>(13, 16));
        edges.add(new Pair<>(16, 1));

        edges.add(new Pair<>(7, 10)); // -y face
        edges.add(new Pair<>(10, 19));
        edges.add(new Pair<>(19, 22));
        edges.add(new Pair<>(22, 7));

        edges.add(new Pair<>(0, 9)); // +z face
        edges.add(new Pair<>(9, 12));
        edges.add(new Pair<>(12, 21));
        edges.add(new Pair<>(21, 0));

        edges.add(new Pair<>(3, 6)); // -z face
        edges.add(new Pair<>(6, 15));
        edges.add(new Pair<>(15, 18));
        edges.add(new Pair<>(18, 3));
    }

    List<Material> doNotCollideTypes = List.of(Material.AIR,
                                                      Material.DEAD_BRAIN_CORAL_FAN,
                                                      Material.DEAD_BUBBLE_CORAL_FAN,
                                                      Material.DEAD_HORN_CORAL_FAN,
                                                      Material.DEAD_FIRE_CORAL_FAN,
                                                      Material.DEAD_TUBE_CORAL_FAN);

    final int arenaMinX = -18;
    final int arenaMaxX = 19;
    final int arenaMinY =  129;
    final int arenaMaxY =  142;
    final int arenaMinZ = -17;
    final int arenaMaxZ = 18;

    final double frictionCoefficient = 0.25;
    final double elasticConstant = 0.8;
    final double gravityConstant = 0.01;

    final double punchMultiplier = 0.4;
    final double sprintPunchMultiplier = 0.7;
    final double arrowHitMultiplier = 0.6;

    final double maxVelocity = 1.0;
    final double penetrationPerimeterThreshold = 5.0;
    final double perimeterModifierMultiplier = 0.5;

    final double playerReachDistance = 4.6;

    final Vector3d ballTranslation = new Vector3d(-1.0, -1.125, -1.0);

    public void assignBall(ShulkerBullet shulkerBullet, BlockDisplay blockDisplay) {
        bullet = shulkerBullet;
        ball = blockDisplay;
    }

    protected void assignScore(ChorusVolleyScore score1) {
        score = score1;
    }

    public void deleteBall() {
        if (bullet != null) bullet.remove();
        if (ball != null) ball.remove();
        bullet = null;
        ball = null;
    }

    public void spawnBall() {

        World world = Games.CURRENT_GAME.currentRound.worldSettings.get(0).getWorld();
        Location spawnLocation = new Location(world,(arenaMinX + arenaMaxX)/2.0, (arenaMinY + arenaMaxY)/2.0, (arenaMinZ + arenaMaxZ)/2.0);

        ShulkerBullet bullet = (ShulkerBullet) world.spawnEntity(spawnLocation, EntityType.SHULKER_BULLET);
        BlockDisplay blockDisplay = (BlockDisplay) world.spawnEntity(spawnLocation, EntityType.BLOCK_DISPLAY);
        blockDisplay.setBlock(Material.CHORUS_FLOWER.createBlockData());

        Transformation transformation = blockDisplay.getTransformation();
        transformation.getScale().set(2.0, 2.0, 2.0);
        transformation.getTranslation().set(ballTranslation);
        blockDisplay.setTransformation(transformation);

        bullet.addPassenger(blockDisplay);
        bullet.setGravity(false);
        bullet.setVelocity(new Vector());

        assignBall(bullet, blockDisplay);

        score.assignBall(bullet);

    }

    public void pauseBall() {
        pausedVelocity = bullet.getVelocity();
        bullet.setVelocity(new Vector());
        paused = true;
    }

    public void resumeBall() {
        bullet.setVelocity(pausedVelocity);
        paused = false;
    }

    public void ballTicker() {

        if (paused || bullet == null) return;

        if (bullet.isDead()) {
            bullet.removePassenger(ball);
            bullet = (ShulkerBullet) previousBulletLocation.getWorld().spawnEntity(previousBulletLocation, EntityType.SHULKER_BULLET);
            bullet.setVelocity(previousBulletVelocity);
            bullet.setGravity(false);
            bullet.addPassenger(ball);

            score.assignBall(bullet);
        }

        applyPhysics();

        previousBulletVelocity = bullet.getVelocity();
        previousBulletLocation = bullet.getLocation();

    }

    protected void onPlayerPunch(Player player) {

        if (paused) return;

        Vector3d p = player.getEyeLocation().toVector().toVector3d(); // player's eye location
        Vector3d pu = player.getEyeLocation().getDirection().toVector3d().normalize(); // unit LOS vector

        if (determineLineBallCollision(p, pu, playerReachDistance)) {

            double forceMultiplier;
            if (player.isSprinting()) {
                forceMultiplier = sprintPunchMultiplier;
                player.playSound(Sound.sound(Key.key("entity.player.attack.knockback"), Sound.Source.WEATHER, 1.0f, 1.0f));
            } else {
                forceMultiplier = punchMultiplier;
                player.playSound(Sound.sound(Key.key("entity.player.attack.strong"), Sound.Source.WEATHER, 1.0f, 1.0f));
            }

            impartForceOnBall(p, pu, forceMultiplier, true);

        }

    }

    protected void arrowTick(Arrow arrow) {

        if (paused) return;

        Vector3d p = arrow.getLocation().toVector().toVector3d();
        Vector3d pu = arrow.getVelocity().toVector3d().normalize();
        double vel = arrow.getVelocity().length();

        if (determineLineBallCollision(p, pu, vel)) {
            if (arrow.getShooter() instanceof Player player) player.playSound(Sound.sound(Key.key("entity.arrow.hit_player"), Sound.Source.WEATHER, 1.0f, 0.5f));
            impartForceOnBall(p, pu, arrowHitMultiplier, true);
            arrow.remove();
        }

    }

    protected void fishHookTick(FishHook fishHook) {

        if (paused) return;

        Vector3d p = fishHook.getLocation().toVector().toVector3d();
        Vector3d pu = fishHook.getVelocity().toVector3d().normalize();
        double vel = fishHook.getVelocity().length();

        if (determineLineBallCollision(p, pu, vel)) {
            if (fishHook.getShooter() instanceof Player player) player.playSound(Sound.sound(Key.key("entity.arrow.hit_player"), Sound.Source.WEATHER, 1.0f, 0.75f));
            fishHook.setHookedEntity(bullet);
        }

    }

    private Location ballCenter() {
        return bullet.getLocation();
    }

    private boolean determineLineBallCollision(Vector3d p, Vector3d pu, double maxDistance) {

        boolean intersects = true;

        Vector3d c = ballCenter().toVector().toVector3d(); // ball center

        List<Vector3d> transformedVertices = new ArrayList<>();
        for (Vector3d vertex : vertices) {
            Vector3d transformedVertex = ball.getTransformation().getRightRotation().transform(new Vector3d(vertex));
            transformedVertices.add(transformedVertex.add(c));
        }

        // for each polyhedron edge AB, where P is player's eye position:
        //
        //                                       --/
        //                                  Q --/
        //                                 --|
        //                              --/  |
        //                           --/     |
        //              A ----------/--------|-------------B
        //                     --/           R
        //                  --/
        //               --/
        //             P
        //
        // vector QR is shortest distance between player's LOS vector P and polyhedron edge AB
        // so QR direction is given by Pu cross ABu
        //
        // if distance |PQ| is greater than input maxDistance, we override Q to be maxDistance
        // from P, then again find the shortest distance to line AB (R may change as well)
        //
        // notes:
        // - u denotes unit vector in a direction
        // - single letters (a, b, p, q, etc) represent location in space
        // - multiple letters represent directions/distances between two points, irrespective of location

        for (Pair<Integer, Integer> edge : edges) {

            Vector3d a = transformedVertices.get(edge.getA());
            Vector3d b = transformedVertices.get(edge.getB());
            Vector3d abu = new Vector3d();
            a.sub(b, abu);
            abu.normalize(); // unit vector from a to b

            Vector3d pa = new Vector3d(); // vector PA
            a.sub(p, pa);

            Vector3d qru = new Vector3d(); // unit vector of direction QR = pu cross abu
            pu.cross(abu, qru);

            // get point Q = P + (PA dot PU) * PU
            double paDotPu = pa.dot(pu);
            Vector3d q = new Vector3d();
            Vector3d pq = new Vector3d();

            if (paDotPu < maxDistance) {

                pu.mul(paDotPu, pq);
                p.add(pq, q); // now we have location of point Q

            } else {

                // override location of q and then find new qru
                pu.mul(maxDistance, pq);
                p.add(pq, q);

                Vector3d aq = new Vector3d();
                q.sub(a, aq);
                double aqDotAbu = aq.dot(abu);
                Vector3d ar = new Vector3d();
                abu.mul(aqDotAbu, ar);

                Vector3d qr = new Vector3d();
                aq.sub(ar, qr);
                qr.normalize(qru);

            }

            Vector3d qc = new Vector3d();
            c.sub(q, qc); // vector from Q to center of ball

            // want QR (qru) pointing inwards towards center, so flip if opposite QC vector
            if (qc.dot(qru) < 0) {
                qru.mul(-1.0);
            }

            // ok NOW finally, see if all ball vertices are *in front* of the vector qru
            // again, there's a qru for each ball edge
            // if ANY qru exists with ALL ball vertices *in front* of qru, there is no intersection
            boolean allInFront = true;

            for (Vector3d v : transformedVertices) {

                // call this vertex V
                Vector3d qv = new Vector3d();
                v.sub(q, qv);

                if (qv.dot(qru) < 0) {
                    allInFront = false;
                    break;
                }

            }

            if (allInFront) {
                intersects = false;
                break;
            }

        }

        return intersects;

    }

    public void impartForceOnBall(Vector3d location, Vector3d direction, double multiplier) {
        impartForceOnBall(location, direction, multiplier, false);
    }

    public void impartForceOnBall(Vector3d location, Vector3d direction, double multiplier, boolean respawnBullet) {

        if (respawnBullet) { // used for removing fish hooks when ball is punched or hit with an arrow
            previousBulletVelocity = bullet.getVelocity();
            previousBulletLocation = bullet.getLocation();
            bullet.remove();
            bullet.removePassenger(ball);
            bullet = (ShulkerBullet) previousBulletLocation.getWorld().spawnEntity(previousBulletLocation, EntityType.SHULKER_BULLET);
            bullet.setVelocity(previousBulletVelocity);
            bullet.setGravity(false);
            bullet.addPassenger(ball);

            score.assignBall(bullet);
        }

        Vector3f locationFloat = new Vector3f();
        location.get(locationFloat);
        Vector3f directionFloat = new Vector3f();
        direction.get(directionFloat);

        applyTorque(locationFloat, directionFloat, multiplier);

        Vector addVelocity = Vector.fromJOML(direction).multiply(multiplier);
        bullet.setVelocity(bullet.getVelocity().add(addVelocity));

    }

    private void applyTorque(Vector3f forceLocation, Vector3f forceDirection, double multiplier) {

        Vector3f forcePointToBallCenter = bullet.getLocation().toVector().toVector3f().sub(forceLocation);
        Vector3f forceDirectionUnit = forceDirection.normalize();
        float offsetAngle = forcePointToBallCenter.angle(forceDirectionUnit);
        float distanceToTorquePoint = (float) Math.cos(offsetAngle) * forcePointToBallCenter.length(); // distance from forceLocation to point of moment arm
        Vector3f forceDirectionVector = new Vector3f();
        forceDirectionUnit.mul(distanceToTorquePoint, forceDirectionVector);
        Vector3f torqueRadialVector = forceDirectionVector.sub(forcePointToBallCenter);
        Vector3f torqueVector = new Vector3f();
        torqueRadialVector.cross(forceDirectionUnit, torqueVector);
        ball.getTransformation().getRightRotation().transformInverse(torqueVector);
        w.add(torqueVector.mul((float)multiplier*0.25f));

    }

    private void applyPhysics() {

        if (bullet.getVelocity().length() > maxVelocity) {
            bullet.setVelocity(bullet.getVelocity().normalize().multiply(maxVelocity));
        }

        double minX = ballCenter().x();
        double maxX = ballCenter().x();
        double minY = ballCenter().y();
        double maxY = ballCenter().y();
        double minZ = ballCenter().z();
        double maxZ = ballCenter().z();

        Transformation transform = ball.getTransformation();

        if (w.length() > 0.00001) {

            if (w.length() > maxW) {
                w.normalize((float) maxW);
            }

            Vector3f rotationAxis = new Vector3f();
            w.normalize(rotationAxis);
            Quaternionf angularVel = new Quaternionf(new AxisAngle4f(w.length(), rotationAxis));

            Quaternionf rotation = new Quaternionf();
            transform.getRightRotation().mul(angularVel, rotation);
            transform.getRightRotation().set(rotation);
            Vector3d translation = rotation.transform(new Vector3d(ballTranslation));
            transform.getTranslation().set(translation);

            ball.setTransformation(transform);

            ball.setInterpolationDelay(0);
            ball.setInterpolationDuration(1);

            w.mul(0.95f);

        }

        for (Vector3d corner : collisionVertices) {

            Vector3d newCorner = transform.getRightRotation().transform(new Vector3d(corner)).add(bullet.getVelocity().toVector3d());

            minX = Math.min(minX, ballCenter().x() + newCorner.x);
            maxX = Math.max(maxX, ballCenter().x() + newCorner.x);
            minY = Math.min(minY, ballCenter().y() + newCorner.y);
            maxY = Math.max(maxY, ballCenter().y() + newCorner.y);
            minZ = Math.min(minZ, ballCenter().z() + newCorner.z);
            maxZ = Math.max(maxZ, ballCenter().z() + newCorner.z);

        }

        List<BlockVector> potentialCollisionBlocks = new ArrayList<>();
        for (int x = (int) Math.floor(minX); x <= maxX; x++) {
            for (int y = (int) Math.floor(minY); y <= maxY; y++) {
                for (int z = (int) Math.floor(minZ); z <= maxZ; z++) {

                    if (!doNotCollideTypes.contains(bullet.getWorld().getBlockAt(x, y, z).getType())) {
                        potentialCollisionBlocks.add(new BlockVector(x, y, z));
                    }

                }
            }
        }

        Vector3d c = ballCenter().toVector().toVector3d(); // ball center

        List<Vector3d> transformedVertices = new ArrayList<>();
        for (Vector3d vertex : collisionVertices) {
            Vector3d transformedVertex = ball.getTransformation().getRightRotation().transform(new Vector3d(vertex));
            transformedVertices.add(transformedVertex.add(c).add(bullet.getVelocity().toVector3d()));
        }

        Vector3d ballVelocity = new Vector3d(bullet.getVelocity().toVector3d());
        Vector3f angularVelocity = new Vector3f(w);

        List<Vector3d> queuedForceLocations = new ArrayList<>();
        List<Vector3d> queuedForceDirections = new ArrayList<>();
        List<Double> queuedForceMultipliers = new ArrayList<>();

        // evaluate potential collision blocks for collisions
        for (BlockVector bv : potentialCollisionBlocks) {

            int x = bv.getBlockX();
            int y = bv.getBlockY();
            int z = bv.getBlockZ();

            List<Vector3d> contactPoints = new ArrayList<>();

            for (Pair<Integer, Integer> edge : edges) {

                Vector3d a = transformedVertices.get(edge.getA());
                Vector3d b = transformedVertices.get(edge.getB());
                Vector3d ab = new Vector3d();
                b.sub(a, ab);

                double xmin = Math.min(a.x, b.x);
                double xmax = Math.max(a.x, b.x);
                double ymin = Math.min(a.y, b.y);
                double ymax = Math.max(a.y, b.y);
                double zmin = Math.min(a.z, b.z);
                double zmax = Math.max(a.z, b.z);

                if (xmin < x && xmax > x) {

                    // check -x face
                    double x1;
                    double x2;
                    double y1;
                    double y2;
                    double z1;
                    double z2;

                    if (a.x < b.x) {
                        x1 = a.x;
                        y1 = a.y;
                        z1 = a.z;
                        x2 = b.x;
                        y2 = b.y;
                        z2 = b.z;
                    } else {
                        x1 = b.x;
                        y1 = b.y;
                        z1 = b.z;
                        x2 = a.x;
                        y2 = a.y;
                        z2 = a.z;
                    }

                    double fraction = Math.abs((x-x1) / (x2-x1));
                    double yIntersect = fraction * (y2-y1) + y1;
                    double zIntersect = fraction * (z2-z1) + z1;
                    if (y < yIntersect && yIntersect < y+1
                            && z < zIntersect && zIntersect < z+1) {
                        contactPoints.add(new Vector3d(x, yIntersect, zIntersect));
                    }

                }
                if (xmin < x+1 && xmax > x+1) {

                    // check +x face
                    double x1;
                    double x2;
                    double y1;
                    double y2;
                    double z1;
                    double z2;

                    if (a.x < b.x) {
                        x1 = a.x;
                        y1 = a.y;
                        z1 = a.z;
                        x2 = b.x;
                        y2 = b.y;
                        z2 = b.z;
                    } else {
                        x1 = b.x;
                        y1 = b.y;
                        z1 = b.z;
                        x2 = a.x;
                        y2 = a.y;
                        z2 = a.z;
                    }

                    double fraction = Math.abs(((x+1)-x1) / (x2-x1));
                    double yIntersect = fraction * (y2-y1) + y1;
                    double zIntersect = fraction * (z2-z1) + z1;
                    if (y < yIntersect && yIntersect < y+1
                            && z < zIntersect && zIntersect < z+1) {
                        contactPoints.add(new Vector3d(x+1, yIntersect, zIntersect));
                    }

                }

                if (ymin < y && ymax > y) {

                    // check -y face
                    double x1;
                    double x2;
                    double y1;
                    double y2;
                    double z1;
                    double z2;

                    if (a.y < b.y) {
                        x1 = a.x;
                        y1 = a.y;
                        z1 = a.z;
                        x2 = b.x;
                        y2 = b.y;
                        z2 = b.z;
                    } else {
                        x1 = b.x;
                        y1 = b.y;
                        z1 = b.z;
                        x2 = a.x;
                        y2 = a.y;
                        z2 = a.z;
                    }

                    double fraction = Math.abs((y-y1) / (y2-y1));
                    double xIntersect = fraction * (x2-x1) + x1;
                    double zIntersect = fraction * (z2-z1) + z1;
                    if (x < xIntersect && xIntersect < x+1
                            && z < zIntersect && zIntersect < z+1) {
                        contactPoints.add(new Vector3d(xIntersect, y, zIntersect));
                    }

                }
                if (ymin < y+1 && ymax > y+1) {

                    // check +y face
                    double x1;
                    double x2;
                    double y1;
                    double y2;
                    double z1;
                    double z2;

                    if (a.y < b.y) {
                        x1 = a.x;
                        y1 = a.y;
                        z1 = a.z;
                        x2 = b.x;
                        y2 = b.y;
                        z2 = b.z;
                    } else {
                        x1 = b.x;
                        y1 = b.y;
                        z1 = b.z;
                        x2 = a.x;
                        y2 = a.y;
                        z2 = a.z;
                    }

                    double fraction = Math.abs(((y+1)-y1) / (y2-y1));
                    double xIntersect = fraction * (x2-x1) + x1;
                    double zIntersect = fraction * (z2-z1) + z1;
                    if (x < xIntersect && xIntersect < x+1
                            && z < zIntersect && zIntersect < z+1) {
                        contactPoints.add(new Vector3d(xIntersect, y+1, zIntersect));
                    }

                }

                if (zmin < z && zmax > z) {

                    // check -z face
                    double x1;
                    double x2;
                    double y1;
                    double y2;
                    double z1;
                    double z2;

                    if (a.z < b.z) {
                        x1 = a.x;
                        y1 = a.y;
                        z1 = a.z;
                        x2 = b.x;
                        y2 = b.y;
                        z2 = b.z;
                    } else {
                        x1 = b.x;
                        y1 = b.y;
                        z1 = b.z;
                        x2 = a.x;
                        y2 = a.y;
                        z2 = a.z;
                    }

                    double fraction = Math.abs((z-z1) / (z2-z1));
                    double xIntersect = fraction * (x2-x1) + x1;
                    double yIntersect = fraction * (y2-y1) + y1;
                    if (x < xIntersect && xIntersect < x+1
                            && y < yIntersect && yIntersect < y+1) {
                        contactPoints.add(new Vector3d(xIntersect, yIntersect, z));
                    }

                }
                if (zmin < z+1 && zmax > z+1) {

                    // check +z face
                    double x1;
                    double x2;
                    double y1;
                    double y2;
                    double z1;
                    double z2;

                    if (a.z < b.z) {
                        x1 = a.x;
                        y1 = a.y;
                        z1 = a.z;
                        x2 = b.x;
                        y2 = b.y;
                        z2 = b.z;
                    } else {
                        x1 = b.x;
                        y1 = b.y;
                        z1 = b.z;
                        x2 = a.x;
                        y2 = a.y;
                        z2 = a.z;
                    }

                    double fraction = Math.abs(((z+1)-z1) / (z2-z1));
                    double xIntersect = fraction * (x2-x1) + x1;
                    double yIntersect = fraction * (y2-y1) + y1;
                    if (x < xIntersect && xIntersect < x+1
                            && y < yIntersect && yIntersect < y+1) {
                        contactPoints.add(new Vector3d(xIntersect, yIntersect, z+1));
                    }

                }

            }

            // now we have contact points, apply resulting force
            if (contactPoints.size() == 2) {

                // edge-to-edge collision, two contact points

                // first: get direction between contact points and force location
                Vector3d cp1 = contactPoints.get(0);
                Vector3d cp2 = contactPoints.get(1);

                Vector3d cp12 = new Vector3d();
                cp2.sub(cp1, cp12);

                Vector3d forceLocation = new Vector3d((cp1.x+cp2.x)/2.0, (cp1.y+cp2.y)/2.0, (cp1.z+cp2.z)/2.0);

                // second: determine which block edge the ball is colliding with
                // (the edge straddled by the two contact points)

                // face 0: -x
                // face 1: +x
                // face 2: -y
                // face 3: +y
                // face 4: -z
                // face 5: +z

                // find face of each contact point
                int face1 = 0;

                if (cp1.x == bv.getBlockX()+1) face1 = 1;
                if (cp1.y == bv.getBlockY()) face1 = 2;
                if (cp1.y == bv.getBlockY()+1) face1 = 3;
                if (cp1.z == bv.getBlockZ()) face1 = 4;
                if (cp1.z == bv.getBlockZ()+1) face1 = 5;

                int face2 = 0;

                if (cp2.x == bv.getBlockX()+1) face2 = 1;
                if (cp2.y == bv.getBlockY()) face2 = 2;
                if (cp2.y == bv.getBlockY()+1) face2 = 3;
                if (cp2.z == bv.getBlockZ()) face2 = 4;
                if (cp2.z == bv.getBlockZ()+1) face2 = 5;

                // find edge straddled by these two faces
                Vector3d edgeUnitVector;

                int lowFace = Math.min(face1, face2);
                int highFace = Math.max(face1, face2);

                if (lowFace == 0) {

                    edgeUnitVector = switch (highFace) {
                        case 0, 1 -> new Vector3d(1, 0, 0);
                        case 2, 3 -> new Vector3d(0, 0, 1);
                        default -> new Vector3d(0, 1, 0); // 4 or 5
                    };

                } else if (lowFace == 1) {

                    edgeUnitVector = switch (highFace) {
                        case 2, 3 -> new Vector3d(0, 0, 1);
                        default -> new Vector3d(0, 1, 0); // 4 or 5
                    };

                } else if (lowFace == 2) {

                    edgeUnitVector = switch (highFace) {
                        case 2, 3 -> new Vector3d(0, 1, 0);
                        default -> new Vector3d(1, 0, 0); // 4 or 5
                    };

                } else if (lowFace == 3) {

                    edgeUnitVector = new Vector3d(1, 0, 0);

                } else { // lowFace = 4 or 5

                    edgeUnitVector = new Vector3d(0, 0, 1);

                }

                // now: force direction is cross product of cp12 and block edge
                Vector3d forceDirection = new Vector3d();
                cp12.cross(edgeUnitVector, forceDirection);

                // point force towards center of ball
                Vector3d fc = new Vector3d(); // force location to center of ball
                c.sub(forceLocation, fc);

                if (forceDirection.dot(fc) < 0) {
                    forceDirection.mul(-1.0);
                }
                forceDirection.normalize();

                // now need to find force magnitude (velocity multiplier)
                // normal force magnitude = (1+E) * |ball velocity (dot) force unit vector|
                double normalForceMultiplier = (1+elasticConstant) * Math.abs(ballVelocity.dot(forceDirection));

                queuedForceLocations.add(forceLocation);
                queuedForceDirections.add(forceDirection);
                queuedForceMultipliers.add(normalForceMultiplier);

                // end edge-edge collision case


            } else if (contactPoints.size() > 2) {

                // general collision (corner-edge, corner-face, corner-corner)

                // first, find force location = centroid of contact points
                double forceX = 0;
                double forceY = 0;
                double forceZ = 0;
                for (Vector3d contactPoint : contactPoints) {
                    forceX += contactPoint.x;
                    forceY += contactPoint.y;
                    forceZ += contactPoint.z;
                }
                forceX /= contactPoints.size();
                forceY /= contactPoints.size();
                forceZ /= contactPoints.size();

                Vector3d forceLoc = new Vector3d(forceX, forceY, forceZ);

                // now, find direction of force from cross product of two vectors joining contact points
                // for polarity, point towards center of ball
                Vector3d p1 = new Vector3d(contactPoints.get(0));
                Vector3d p2 = new Vector3d(contactPoints.get(1));
                Vector3d p3 = new Vector3d(contactPoints.get(2));

                Vector3d v1 = p1.sub(p2);
                Vector3d v2 = p2.sub(p3);
                Vector3d v3 = new Vector3d();
                v1.cross(v2, v3);

                Vector3d fc = new Vector3d(); // force location to center of ball
                c.sub(forceLoc, fc);

                if (v3.dot(fc) < 0) {
                    v3.mul(-1.0);
                }
                v3.normalize();

                // now need to find force magnitude (velocity multiplier)
                // normal force magnitude = (1+E) * |ball velocity (dot) force unit vector|
                double normalForceMultiplier = (1+elasticConstant) * Math.abs(ballVelocity.dot(v3));

                // also, find the perimeter of the polygon formed by the contact points
                // this correlates with penetration depth, although not a direct measurement
                // if this crosses a certain threshold, we can increase the force multiplier
                // to help push the ball out of whatever block it's pushing into
                double perimeter = 0;
                for (int i=0; i<contactPoints.size(); i++) {
                    Vector3d side = new Vector3d();
                    Vector3d point1 = contactPoints.get(i);
                    Vector3d point2 = contactPoints.get((i+1)%contactPoints.size());
                    point1.sub(point2, side);
                    perimeter += side.length();
                }

                if (perimeter > penetrationPerimeterThreshold) {
                    normalForceMultiplier += (perimeter-penetrationPerimeterThreshold) * perimeterModifierMultiplier;
                }

                queuedForceLocations.add(forceLoc);
                queuedForceDirections.add(v3);
                queuedForceMultipliers.add(normalForceMultiplier);

            }

        }

        for (int i=0; i<queuedForceLocations.size(); i++) {

            Vector3d location = queuedForceLocations.get(i);
            Vector3d direction = queuedForceDirections.get(i);
            double multiplier = queuedForceMultipliers.get(i) / queuedForceMultipliers.size();

            if (direction.isFinite()) {
                impartForceOnBall(location, direction, multiplier);
            }

            // now frictional force: coefficient * magnitude of normal force
            // direction: tangential velocity (normalized)
            Vector3d velPerpendicular = new Vector3d();
            direction.mul(ballVelocity.dot(direction), velPerpendicular);
            Vector3d velTangential = new Vector3d();
            ballVelocity.sub(velPerpendicular, velTangential);

            // find local velocity of force location
            Vector3d cf = new Vector3d();
            location.sub(c, cf); // vector from center of ball to force location
            Vector3d vRelative = new Vector3d(); // vector of force location relative to center
            Vector3d wd = new Vector3d(angularVelocity.x, angularVelocity.y, angularVelocity.z);
            Vector3d vf = new Vector3d();

            if (wd.length() > 0) {

                wd.cross(cf, vRelative);
                ballVelocity.add(vRelative, vf);

                // i'm lazy about signs/polarity so just make it point opposite local velocity
                if (vf.dot(velTangential) > 0) {
                    velTangential.mul(-1.0);
                }

            } else {

                // i'm lazy about signs/polarity so just make it point opposite local velocity
                if (ballVelocity.dot(velTangential) > 0) {
                    velTangential.mul(-1.0);
                }

            }
            velTangential.normalize();

            if (velTangential.length() > 0) {
                impartForceOnBall(location, velTangential, frictionCoefficient * multiplier);
            }

        }

        // Apply gravity
        impartForceOnBall(ballCenter().toVector().toVector3d(), new Vector3d(0, -1, 0), gravityConstant);

        List<Vector3d> verticesOnGround = new ArrayList<>();
        for (Vector3d vertex : transformedVertices) {

            // oppose gravity if on ground
            if (vertex.y < arenaMinY+0.001) {
                verticesOnGround.add(vertex);
            }

        }

        for (Vector3d vertex : verticesOnGround) {

            Vector3d direction = new Vector3d(0, 1, 0);

            impartForceOnBall(vertex, direction, gravityConstant/verticesOnGround.size());

            // copy pasted from above
            // now frictional force: coefficient * magnitude of normal force
            // direction: tangential velocity (normalized)
            Vector3d velPerpendicular = new Vector3d();
            direction.mul(ballVelocity.dot(direction), velPerpendicular);
            Vector3d velTangential = new Vector3d();
            ballVelocity.sub(velPerpendicular, velTangential);

            // find local velocity of force location
            Vector3d cf = new Vector3d();
            vertex.sub(c, cf); // vector from center of ball to force location
            Vector3d vRelative = new Vector3d(); // vector of force location relative to center
            Vector3d wd = new Vector3d(angularVelocity.x, angularVelocity.y, angularVelocity.z);
            Vector3d vf = new Vector3d();

            if (wd.length() > 0) {

                wd.cross(cf, vRelative);
                ballVelocity.add(vRelative, vf);

                // i'm lazy about signs/polarity so just make it point opposite local velocity
                if (vf.dot(velTangential) > 0) {
                    velTangential.mul(-1.0);
                }

            } else {

                // i'm lazy about signs/polarity so just make it point opposite local velocity
                if (ballVelocity.dot(velTangential) > 0) {
                    velTangential.mul(-1.0);
                }

            }
            velTangential.normalize();

            if (velTangential.length() > 0) {
                impartForceOnBall(vertex, velTangential, frictionCoefficient * gravityConstant/verticesOnGround.size());
            }

        }

        // rest if velocities are low enough
        if (bullet.getVelocity().length() < 0.005 && w.length() < 0.01) {
            bullet.setVelocity(bullet.getVelocity().multiply(0.5));
            w = w.mul(0.5f);
        }

    }

    public void resetVelocity() {
        bullet.setVelocity(new Vector());
        w = new Vector3f();
    }

    public void onGoalScore() {

        bullet.getWorld().playEffect(bullet.getLocation(), Effect.ENDER_DRAGON_DEATH, 0);

    }
}
