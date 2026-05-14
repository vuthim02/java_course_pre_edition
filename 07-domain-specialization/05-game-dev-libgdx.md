# Domain Specialization — Lesson 5: Game Development with LibGDX

> **INTRODUCTORY OVERVIEW** — This section provides a high-level introduction to the domain. Each topic warrants its own dedicated course for professional mastery.

## Why LibGDX?

LibGDX is a cross-platform game development framework for Java. It gives you direct access to OpenGL for 2D and 3D rendering, plus audio, input handling, and physics — all in pure Java.

```
┌─────────────────────────────────────────────────────────────┐
│                   LIBGDX APPLICATION                          │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                    Game Loop                             │ │
│  │                                                          │ │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────┐ │ │
│  │  │ render() │  │  update  │  │  input   │  │ physics│ │ │
│  │  │ (draw)   │  │ (logic)  │  │ (handle) │  │ (step) │ │ │
│  │  └──────────┘  └──────────┘  └──────────┘  └────────┘ │ │
│  │                                                          │ │
│  │  ┌─────────────────────────────────────────────────────┐ │ │
│  │  │ 60 FPS (frames per second)                          │ │ │
│  │  └─────────────────────────────────────────────────────┘ │ │
│  └─────────────────────────────────────────────────────────┘ │
│                                                               │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │               Cross-Platform Output                       │ │
│  │  ┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐        │ │
│  │  │Desktop │  │ Android│  │  iOS   │  │  Web   │        │ │
│  │  │(JVM)   │  │(ART)   │  │(RoboVM)│  │(GWT)   │        │ │
│  │  └────────┘  └────────┘  └────────┘  └────────┘        │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## Setup

```xml
<!-- Gradle with LibGDX plugin -->
buildscript {
    dependencies {
        classpath 'com.badlogicgames.gdx:gdx-tools:1.12.1'
    }
}

allprojects {
    apply plugin: 'java'
    sourceCompatibility = 17

    repositories { mavenCentral() }

    dependencies {
        implementation 'com.badlogicgames.gdx:gdx:1.12.1'
        implementation 'com.badlogicgames.gdx:gdx-backend-lwjgl3:1.12.1'
        implementation 'com.badlogicgames.gdx:gdx-platform:1.12.1:natives-desktop'
    }
}
```

## Main Game Class

```java
public class MyGame extends Game {

    @Override
    public void create() {
        setScreen(new MainMenuScreen(this));
    }
}

// Desktop launcher
public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("My Game");
        config.setWindowedMode(800, 600);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new MyGame(), config);
    }
}
```

## Screen & Game Loop

```java
public class GameScreen implements Screen {

    private final MyGame game;
    private SpriteBatch batch;
    private Texture playerTexture;
    private float playerX, playerY;

    public GameScreen(MyGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        playerTexture = new Texture("player.png");
        playerX = 100;
        playerY = 100;
    }

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Handle input
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))  playerX -= 200 * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) playerX += 200 * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.UP))    playerY += 200 * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))  playerY -= 200 * delta;

        // Draw
        batch.begin();
        batch.draw(playerTexture, playerX, playerY);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        playerTexture.dispose();
    }
}
```

## Sprites & Animation

```java
public class Player {
    private static final int FRAME_COLS = 4;
    private static final int FRAME_ROWS = 4;

    private final Animation<TextureRegion> walkAnimation;
    private final Texture walkSheet;
    private float stateTime;
    private float x, y;

    public Player() {
        walkSheet = new Texture("player_spritesheet.png");
        TextureRegion[][] tmp = TextureRegion.split(walkSheet,
            walkSheet.getWidth() / FRAME_COLS,
            walkSheet.getHeight() / FRAME_ROWS);

        TextureRegion[] frames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
        int index = 0;
        for (int i = 0; i < FRAME_ROWS; i++) {
            for (int j = 0; j < FRAME_COLS; j++) {
                frames[index++] = tmp[i][j];
            }
        }

        walkAnimation = new Animation<>(0.1f, frames);
        stateTime = 0f;
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public TextureRegion getCurrentFrame() {
        return walkAnimation.getKeyFrame(stateTime, true);  // looping
    }
}
```

## Collision Detection

```java
public class CollisionManager {

    public boolean checkCollision(Rectangle a, Rectangle b) {
        return a.overlaps(b);
    }

    public void handleCollisions(Player player, List<Enemy> enemies) {
        Rectangle playerBounds = player.getBounds();

        for (Enemy enemy : enemies) {
            if (playerBounds.overlaps(enemy.getBounds())) {
                player.takeDamage(10);
                enemy.knockback();
            }
        }
    }
}
```

## Box2D Physics

```java
// Create physics world
World world = new World(new Vector2(0, -9.81f), true);  // Gravity

// Create a body
BodyDef bodyDef = new BodyDef();
bodyDef.type = BodyDef.BodyType.DynamicBody;
bodyDef.position.set(100, 100);
Body body = world.createBody(bodyDef);

// Add shape
PolygonShape shape = new PolygonShape();
shape.setAsBox(16, 16);
FixtureDef fixtureDef = new FixtureDef();
fixtureDef.shape = shape;
fixtureDef.density = 1.0f;
fixtureDef.friction = 0.5f;
body.createFixture(fixtureDef);
shape.dispose();

// Step physics in render loop
world.step(1/60f, 6, 2);
```

## Exercises

1. Set up a LibGDX project and display a sprite on screen.
2. Add keyboard controls to move a character.
3. Create a simple animation from a sprite sheet.
4. Implement collision detection between two game objects.
5. Build a simple game (e.g., a platformer or space shooter) with Box2D physics.
