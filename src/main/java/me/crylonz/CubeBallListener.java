package me.crylonz;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.util.Vector;

import java.util.concurrent.atomic.AtomicReference;

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

                Ball ballData = fetchBallContacting(e.getBlock().getLocation());

                if (ballData != null) {
                    String ballId = ballData.getId();

                    if (ballData.getBall() != null) {
                        Vector velocity = ballData.getBall().getVelocity();
                        double zVelocity = abs(velocity.getZ()) / 1.5;
                        double xVelocity = abs(velocity.getX()) / 1.5;
                        double maxZX = max(zVelocity, xVelocity);

                        velocity.setY(min(maxZX, 0.5));

                        destroyBall(ballId);
                        generateBall(ballId, e.getEntity().getLocation(), ballData.getLastVelocity());

                        ballData = balls.get(ballId);
                        ballData.getBall().setVelocity(velocity);

                        if (abs(velocity.getX() + velocity.getY() + velocity.getZ()) <= 0.001 || velocity.getY() < 0.025) {
                            ballData.getBall().setVelocity(ballData.getBall().getVelocity().zero());
                            ballData.getBall().setGravity(false);
                        } else {
                            if (abs(velocity.getX() + velocity.getY() + velocity.getZ()) > 0.1) {
                                ballData.getBall().getWorld().playSound(ballData.getBall().getLocation(), Sound.BLOCK_WOOL_HIT, 10, 1);
                            }
                        }
                    }
                }

            }
        }
    }

    private Ball fetchBallContacting(Location location) {

        AtomicReference<Ball> ballTrigger = new AtomicReference<>();
        balls.forEach((id, ball) -> {
            if (ball.getBall().getWorld().equals(location.getWorld()) &&
                    ball.getBall().getLocation().distanceSquared(location) < 2) {
                ballTrigger.set(ball);
            }
        });
        return ballTrigger.get();
    }
}
