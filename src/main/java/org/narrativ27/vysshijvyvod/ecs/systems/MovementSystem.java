package org.narrativ27.vysshijvyvod.ecs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.All;
import com.artemis.systems.EntityProcessingSystem;
import org.narrativ27.vysshijvyvod.ecs.components.Position;
import org.narrativ27.vysshijvyvod.ecs.components.Velocity;

public class MovementSystem extends EntityProcessingSystem {
    private ComponentMapper<Position> mPosition;
    private ComponentMapper<Velocity> mVelocity;

    @SuppressWarnings("unchecked")
    public MovementSystem() {
        super(Aspect.all(Position.class, Velocity.class));
    }

    @Override
    protected void process(Entity e) {
        Position pos = mPosition.get(e);
        Velocity vel = mVelocity.get(e);
        float dt = world.getDelta();
        pos.x += vel.dx * dt;
        pos.y += vel.dy * dt;
        pos.z += vel.dz * dt;
        System.out.printf("Entity %d moved to (%.2f, %.2f, %.2f)%n", e.getId(), pos.x, pos.y, pos.z);
    }
}
