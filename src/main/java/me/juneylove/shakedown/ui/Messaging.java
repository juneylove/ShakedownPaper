package me.juneylove.shakedown.ui;

import me.juneylove.shakedown.games.Games;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class Messaging {

    public static void deathMessage(String lastDamager, String targetIgn, World world, EntityDamageEvent.DamageCause cause) {

        TextComponent str = TextFormat.FormatIgn(targetIgn).append(Component.text(" "));
        boolean appendLastDamager = false;

        switch (cause) {

            case CONTACT:

                if (lastDamager == null) {
                    str = str.append(Component.text("was pricked to death"));
                } else {
                    str = str.append(Component.text("was pricked to death while battling "));
                    appendLastDamager = true;
                }
                break;

            case ENTITY_ATTACK:
            case ENTITY_SWEEP_ATTACK:
                str = str.append(Component.text("was defeated in combat by "));
                appendLastDamager = true;
                break;

            case PROJECTILE:
                str = str.append(Component.text("was shot by "));
                break;

            case SUFFOCATION:

                if (lastDamager == null) {
                    str = str.append(Component.text("suffocated in a wall"));
                } else {
                    str = str.append(Component.text("suffocated in a wall while fighting "));
                    appendLastDamager = true;
                }
                break;

            case FALL:

                if (lastDamager == null) {
                    str = str.append(Component.text("fell to their death"));
                } else {
                    str = str.append(Component.text("was knocked off a ledge by "));
                    appendLastDamager = true;
                }
                break;

            case FIRE:
            case FIRE_TICK:

                if (lastDamager == null) {
                    str = str.append(Component.text("burned to death"));
                } else {
                    str = str.append(Component.text("burned to death while battling "));
                    appendLastDamager = true;
                }
                break;

            case MELTING:
                str = str.append(Component.text("melted?"));
                break;

            case LAVA:

                if (lastDamager == null) {
                    str = str.append(Component.text("jumped into lava"));
                } else {
                    str = str.append(Component.text("died to lava while fighting "));
                    appendLastDamager = true;
                }
                break;

            case DROWNING:

                if (lastDamager == null) {
                    str = str.append(Component.text("drowned"));
                } else {
                    str = str.append(Component.text("drowned while battling "));
                    appendLastDamager = true;
                }
                break;

            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:

                if (lastDamager == null) {
                    str = str.append(Component.text("blew up"));
                } else {
                    str = str.append(Component.text("was blown up by "));
                    appendLastDamager = true;
                }
                break;

            case VOID:

                if (lastDamager == null) {
                    str = str.append(Component.text("fell into the abyss"));
                } else {
                    str = str.append(Component.text("was knocked out of the world by "));
                    appendLastDamager = true;
                }
                break;

            case LIGHTNING:
                str = str.append(Component.text("was struck by LIGHTNING?"));
                break;

            case SUICIDE:

                if (lastDamager == null) {
                    str = str.append(Component.text("unalived"));
                } else {
                    str = str.append(Component.text("unalived while fighting "));
                    appendLastDamager = true;
                }
                break;

            case STARVATION:

                if (lastDamager == null) {
                    str = str.append(Component.text("starved to death"));
                } else {
                    str = str.append(Component.text("starved while battling "));
                    appendLastDamager = true;
                }
                break;

            case POISON:

                if (lastDamager == null) {
                    str = str.append(Component.text("died of poisoning"));
                } else {
                    str = str.append(Component.text("died to poison while fighting "));
                    appendLastDamager = true;
                }
                break;

            case MAGIC:

                if (lastDamager == null) {
                    str = str.append(Component.text("was killed by magic"));
                } else {
                    str = str.append(Component.text("was killed with magic by "));
                    appendLastDamager = true;
                }
                break;

            case WITHER:

                if (lastDamager == null) {
                    str = str.append(Component.text("withered away"));
                } else {
                    str = str.append(Component.text("withered away while battling "));
                    appendLastDamager = true;
                }
                break;

            case FALLING_BLOCK:

                if (lastDamager == null) {
                    str = str.append(Component.text("was crushed by a falling block"));
                } else {
                    str = str.append(Component.text("died to a falling block while fighting "));
                    appendLastDamager = true;
                }
                break;

            case THORNS:

                if (lastDamager == null) {
                    str = str.append(Component.text("died to thorns"));
                } else {
                    str = str.append(Component.text("died to thorns while fighting "));
                    appendLastDamager = true;
                }
                break;

            case DRAGON_BREATH:
                str = str.append(Component.text(" was killed by dragon's breath??"));
                break;

            case FLY_INTO_WALL:

                if (lastDamager == null) {
                    str = str.append(Component.text("flew into a wall like a BOZO"));
                } else {
                    str = str.append(Component.text("flew into a wall while fighting "));
                    appendLastDamager = true;
                }
                break;

            case HOT_FLOOR:

                if (lastDamager == null) {
                    str = str.append(Component.text("walked onto a magma block"));
                } else {
                    str = str.append(Component.text("lost \"the floor is lava\" to "));
                    appendLastDamager = true;
                }
                break;

            case CRAMMING:

                if (lastDamager == null) {
                    str = str.append(Component.text("died by cramming"));
                } else {
                    str = str.append(Component.text("got crammed while battling "));
                    appendLastDamager = true;
                }
                break;

            case DRYOUT:
                str = str.append(Component.text("died from being too dry...yikes"));
                break;

            case FREEZE:

                if (lastDamager == null) {
                    str = str.append(Component.text("froze to death"));
                } else {
                    str = str.append(Component.text("froze to death while fighting "));
                    appendLastDamager = true;
                }
                break;

            case SONIC_BOOM:

                if (lastDamager == null) {
                    str = str.append(Component.text("was blasted by a sonic boom"));
                } else {
                    str = str.append(Component.text("died to a sonic boom while battling "));
                    appendLastDamager = true;
                }
                break;

            case CUSTOM:
            default:

                if (lastDamager == null) {
                    str = str.append(Component.text("died in mysterious circumstances"));
                } else {
                    str = str.append(Component.text("died while battling "));
                    appendLastDamager = true;
                }
                break;



        }

        if (appendLastDamager) {
            str = str.append(TextFormat.FormatIgn(lastDamager));
        }

        // account for lastDamager = "Player1's ability" etc (truncate ' and everything after to get username for scoring)
        if (lastDamager != null && lastDamager.contains("'")) {
            int endIndex = lastDamager.indexOf("'");
            lastDamager = lastDamager.substring(0, endIndex);
        }

        UniqueDeathMessage(str, targetIgn, lastDamager, world);

    }

    // For right now, just appends score to relevant player's message
    private static void UniqueDeathMessage(Component component, String targetIgn, String damagerIgn, World world) {

        for (Player player : world.getPlayers()) {

            if (damagerIgn != null) {

                if (damagerIgn.equalsIgnoreCase(player.getName()) && (!targetIgn.equalsIgnoreCase(damagerIgn))) {
                    // append score value if player receiving message is the last damager, but not if they killed themself
                    player.sendMessage(component
                            .append(Component.text(" [+"))
                            .append(Component.text(Games.CURRENT_GAME.pvpKillScore).color(TextFormat.SCORE_COLOR))
                            .append(Component.text("]")));
                } else {
                    player.sendMessage(component);
                }

            } else {
                player.sendMessage(component);
            }

        }

    }

}
