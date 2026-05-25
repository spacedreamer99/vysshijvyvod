package org.narrativ27.vysshijvyvod;

import com.artemis.Entity;
import org.narrativ27.vysshijvyvod.ecs.components.Position;
import org.narrativ27.vysshijvyvod.ecs.components.Velocity;

public class EcsTest {
    public static void main(String[] args) {
        Entity ship = EcsWorld.getInstance().createEntity();
        Position pos = new Position();
        pos.x = 100; pos.y = 200; pos.z = 0;
        Velocity vel = new Velocity();
        vel.dx = 50; vel.dy = 30; vel.dz = 0;
        ship.edit().add(pos).add(vel);

        for (int i = 0; i < 60; i++) {
            EcsWorld.process(1/60f);
        }

        System.out.println("Final position: " + pos.x + ", " + pos.y);
        EcsWorld.dispose();
    }
}
