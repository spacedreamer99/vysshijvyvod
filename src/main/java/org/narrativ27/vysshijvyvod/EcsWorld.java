package org.narrativ27.vysshijvyvod;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import org.narrativ27.vysshijvyvod.ecs.systems.MovementSystem;

public class EcsWorld {
    private static World world;

    public static World getInstance() {
        if (world == null) {
            WorldConfiguration config = new WorldConfigurationBuilder()
                .with(new MovementSystem())
                .build();
            world = new World(config);
        }
        return world;
    }

    public static void process(float deltaTime) {
        getInstance().setDelta(deltaTime);
        getInstance().process();
    }

    public static void dispose() {
        if (world != null) {
            world.dispose();
            world = null;
        }
    }
}
