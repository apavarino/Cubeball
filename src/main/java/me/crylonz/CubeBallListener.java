package me.crylonz;

import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.util.Vector;

import static java.lang.Double.max;
import static java.lang.Double.min;
import static java.lang.Math.abs;
import static me.crylonz.CubeBall.*;

public class CubeBallListener implements Listener {

    @EventHandler
    public void blockChangeEvent(EntityChangeBlockEvent e) {
        if (e.getTo().equals(cubeBallBlock)) {
            if (e.getEntityType() == EntityType.FALLING_BLOCK) {
                e.setCancelled(true);
                if (ball != null) {
                    Vector velocity = ball.getVelocity();
                    double zVelocity = abs(velocity.getZ()) / 1.5;
                    double xVelocity = abs(velocity.getX()) / 1.5;
                    double maxZX = max(zVelocity, xVelocity);

                    velocity.setY(min(maxZX, 0.5));
                    generateBall(e.getEntity().getLocation());
                    ball.setVelocity(velocity);

//                    if (abs(velocity.getY()) <= 0.5) {
//                        velocity.setY(0);
//                        ball.setVelocity(velocity);
//                    }

                    if (abs(velocity.getX() + velocity.getY() + velocity.getZ()) <= 0.001 || velocity.getY() < 0.025) {
                        ball.setVelocity(ball.getVelocity().zero());
                        ball.setGravity(false);
                    } else {
                        if (abs(velocity.getX() + velocity.getY() + velocity.getZ()) > 0.1) {
                            ball.getWorld().playSound(ball.getLocation(), Sound.BLOCK_WOOL_HIT, 10, 1);
                        }
                    }


                }
            }
        }
    }
}
