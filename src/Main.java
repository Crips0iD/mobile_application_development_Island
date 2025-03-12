import java.util.*;
import java.util.concurrent.*;

abstract class LivingBeing {
    protected String name;
    protected int speed;
    protected double maxFood;
    protected double weight;
    protected boolean isAlive = true;

    public LivingBeing(String name, int speed, double maxFood, double weight) {
        this.name = name;
        this.speed = speed;
        this.maxFood = maxFood;
        this.weight = weight;
    }

    public String getName() { return name; }
    public abstract void consume(Area area);
    public void move(Island island, int currentX, int currentY) {
        Random rand = new Random();
        int newX = Math.max(0, Math.min(island.getWidth() - 1, currentX + rand.nextInt(3) - 1));
        int newY = Math.max(0, Math.min(island.getHeight() - 1, currentY + rand.nextInt(3) - 1));
        island.relocateBeing(this, currentX, currentY, newX, newY);
    }
    public abstract LivingBeing reproduce();
}

abstract class Predator extends LivingBeing {
    protected final Map<Class<? extends LivingBeing>, Integer> diet;
    private boolean isHungry = false;
    private int hungerLevel = 5;

    public Predator(String name, int speed, int maxFood, double weight, Map<Class<? extends LivingBeing>, Integer> diet) {
        super(name, speed, maxFood, weight);
        this.diet = diet;
    }

    protected void hunt(Area area) {
        Iterator<LivingBeing> iterator = area.beings.iterator();
        boolean hasEaten = false;

        while (iterator.hasNext()) {
            LivingBeing prey = iterator.next();
            if (diet.containsKey(prey.getClass()) && Math.random() * 100 < diet.get(prey.getClass())) {
                iterator.remove();
                isHungry = true;
                hungerLevel = 5;
                System.out.println(name + " съел " + prey.name);
                hasEaten = true;
                break;
            }
        }

        if (!hasEaten) {
            hungerLevel--;
            if (hungerLevel <= 0) {
                System.out.println(name + " умер от голода.");
                area.beings.remove(this);
            }
        }
    }

    @Override
    public LivingBeing reproduce() {
        if (isHungry && hungerLevel > 2 && Math.random() < 0.1) {
            isHungry = false;
            System.out.println(name + " размножился.");
            return createNew();
        }
        return null;
    }

    protected abstract LivingBeing createNew();
}

class Wolf extends Predator {
    private static final Map<Class<? extends LivingBeing>, Integer> diet = Map.of(
            Rabbit.class, 60, Deer.class, 40, Mouse.class, 80, Horse.class, 10,
            Goat.class, 60, Sheep.class, 70, Hog.class, 15, Buffalo.class, 10, Duck.class, 40
    );

    public Wolf() { super("Wolf", 3, 8, 50.0, diet); }

    @Override
    public void consume(Area area) {
        hunt(area);
    }

    @Override
    protected LivingBeing createNew() {
        return new Wolf();
    }

    @Override public LivingBeing reproduce() { return new Wolf(); }
}

class Fox extends Predator {
    private static final Map<Class<? extends LivingBeing>, Integer> diet = Map.of(
            Rabbit.class, 70, Mouse.class, 90, Duck.class, 60, Caterpillar.class, 40
    );

    public Fox() { super("Fox", 2, 2, 8.0, diet); }

    @Override
    public void consume(Area area) {
        hunt(area);
    }

    @Override
    protected LivingBeing createNew() {
        return new Fox();
    }

    @Override public LivingBeing reproduce() { return new Fox(); }
}

class Bear extends Predator {
    private static final Map<Class<? extends LivingBeing>, Integer> diet = Map.of(
            Boa.class, 80, Horse.class, 40, Deer.class, 80, Rabbit.class, 80,
            Mouse.class, 90, Goat.class, 70, Sheep.class, 70, Hog.class, 50, Buffalo.class, 20, Duck.class, 10
    );

    public Bear() { super("Bear", 2, 15, 200.0, diet); }

    @Override
    public void consume(Area area) {
        hunt(area);
    }

    @Override
    protected LivingBeing createNew() {
        return new Bear();
    }

    @Override public LivingBeing reproduce() { return new Bear(); }
}

class Eagle extends Predator {
    private static final Map<Class<? extends LivingBeing>, Integer> diet = Map.of(
            Fox.class, 10, Rabbit.class, 90, Mouse.class, 90, Duck.class, 80
    );

    public Eagle() { super("Eagle", 4, 3, 6.0, diet); }

    @Override
    public void consume(Area area) {
        hunt(area);
    }

    @Override
    protected LivingBeing createNew() {
        return new Eagle();
    }

    @Override public LivingBeing reproduce() { return new Eagle(); }
}

class Boa extends Predator {
    private static final Map<Class<? extends LivingBeing>, Integer> diet = Map.of(
            Fox.class, 15, Rabbit.class, 20, Mouse.class, 40, Duck.class, 10
    );

    public Boa() { super("Boa", 1, 3, 15.0, diet); }

    @Override
    public void consume(Area area) {
        hunt(area);
    }

    @Override
    protected LivingBeing createNew() {
        return new Boa();
    }

    @Override public LivingBeing reproduce() { return new Boa(); }
}

class Rabbit extends LivingBeing {
    public Rabbit() { super("Rabbit", 2, 4, 2.5); }

    @Override
    public void consume(Area area) {
        if (area.plantCount > 0) {
            area.plantCount--;
            System.out.println(name + " ест траву.");
        }
    }

    @Override public LivingBeing reproduce() { return new Rabbit(); }
}

class Mouse extends LivingBeing {
    private static final Map<Class<? extends LivingBeing>, Integer> diet = Map.of(Caterpillar.class, 90);

    public Mouse() { super("Mouse", 1, 2, 0.5); }

    @Override
    public void consume(Area area) {
        Iterator<LivingBeing> iterator = area.beings.iterator();
        boolean hasEaten = false;

        while (iterator.hasNext()) {
            LivingBeing prey = iterator.next();
            if (diet.containsKey(prey.getClass()) && Math.random() * 100 < diet.get(prey.getClass())) {
                iterator.remove();
                System.out.println(name + " съел " + prey.getName());
                hasEaten = true;
                break;
            }
        }

        if (!hasEaten && area.plantCount > 0) {
            area.plantCount--;
            System.out.println(name + " ест траву.");
        }
    }

    @Override public LivingBeing reproduce() { return new Mouse(); }
}

class Deer extends LivingBeing {
    public Deer() { super("Deer", 3, 6, 80.0); }

    @Override
    public void consume(Area area) {
        if (area.plantCount > 0) {
            area.plantCount--;
            System.out.println(name + " ест траву.");
        }
    }

    @Override public LivingBeing reproduce() { return new Deer(); }
}

class Horse extends LivingBeing {
    public Horse() { super("Horse", 4, 10, 120.0); }

    @Override
    public void consume(Area area) {
        if (area.plantCount > 0) {
            area.plantCount--;
            System.out.println(name + " ест траву.");
        }
    }

    @Override public LivingBeing reproduce() { return new Horse(); }
}

class Goat extends LivingBeing {
    public Goat() { super("Goat", 3, 10, 60.0); }

    @Override
    public void consume(Area area) {
        if (area.plantCount > 0) {
            area.plantCount--;
            System.out.println(name + " ест траву.");
        }
    }

    @Override public LivingBeing reproduce() { return new Goat(); }
}

class Sheep extends LivingBeing {
    public Sheep() { super("Sheep", 3, 15, 70.0); }

    @Override
    public void consume(Area area) {
        if (area.plantCount > 0) {
            area.plantCount--;
            System.out.println(name + " ест траву.");
        }
    }

    @Override public LivingBeing reproduce() { return new Sheep(); }
}

class Hog extends LivingBeing {
    private static final Map<Class<? extends LivingBeing>, Integer> diet = Map.of(Mouse.class, 50, Caterpillar.class, 90);

    public Hog() { super("Hog", 2, 50, 400.0); }

    @Override
    public void consume(Area area) {
        Iterator<LivingBeing> iterator = area.beings.iterator();
        boolean hasEaten = false;

        while (iterator.hasNext()) {
            LivingBeing prey = iterator.next();
            if (diet.containsKey(prey.getClass()) && Math.random() * 100 < diet.get(prey.getClass())) {
                iterator.remove();
                System.out.println(name + " съел " + prey.getName());
                hasEaten = true;
                break;
            }
        }

        if (!hasEaten && area.plantCount > 0) {
            area.plantCount--;
            System.out.println(name + " ест траву.");
        }
    }

    @Override public LivingBeing reproduce() { return new Hog(); }
}

class Buffalo extends LivingBeing {
    public Buffalo() { super("Buffalo", 3, 100, 700.0); }

    @Override
    public void consume(Area area) {
        if (area.plantCount > 0) {
            area.plantCount--;
            System.out.println(name + " ест траву.");
        }
    }

    @Override public LivingBeing reproduce() { return new Buffalo(); }
}

class Duck extends LivingBeing {
    private static final Map<Class<? extends LivingBeing>, Integer> diet = Map.of(Caterpillar.class, 90);

    public Duck() { super("Duck", 4, 0.15, 1.0); }

    @Override
    public void consume(Area area) {
        Iterator<LivingBeing> iterator = area.beings.iterator();
        boolean hasEaten = false;

        while (iterator.hasNext()) {
            LivingBeing prey = iterator.next();
            if (diet.containsKey(prey.getClass()) && Math.random() * 100 < diet.get(prey.getClass())) {
                iterator.remove();
                System.out.println(name + " съел " + prey.getName());
                hasEaten = true;
                break;
            }
        }

        if (!hasEaten && area.plantCount > 0) {
            area.plantCount--;
            System.out.println(name + " ест траву.");
        }
    }

    @Override public LivingBeing reproduce() { return new Duck(); }
}

class Caterpillar extends LivingBeing {
    public Caterpillar() { super("Caterpillar", 0, 0, 0.01); }

    @Override
    public void consume(Area area) {
        if (area.plantCount > 0) {
            area.plantCount--;
            System.out.println(name + " ест траву.");
        }
    }

    @Override public LivingBeing reproduce() { return new Caterpillar(); }
}

class Area {
    List<LivingBeing> beings = new ArrayList<>();
    int plantCount = 5;
}

class Island {
    private final int width;
    private final int height;
    private final Area[][] areas;

    public Island(int width, int height) {
        this.width = width;
        this.height = height;
        this.areas = new Area[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                areas[i][j] = new Area();
            }
        }
        populateIsland();
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    private void populateIsland() {
        Random rand = new Random();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (rand.nextDouble() < 0.4) areas[i][j].beings.add(new Wolf());
                if (rand.nextDouble() < 0.4) areas[i][j].beings.add(new Bear());
                if (rand.nextDouble() < 0.4) areas[i][j].beings.add(new Boa());
                if (rand.nextDouble() < 0.4) areas[i][j].beings.add(new Fox());
                if (rand.nextDouble() < 0.4) areas[i][j].beings.add(new Eagle());
                if (rand.nextDouble() < 0.6) areas[i][j].beings.add(new Deer());
                if (rand.nextDouble() < 0.6) areas[i][j].beings.add(new Horse());
                if (rand.nextDouble() < 0.6) areas[i][j].beings.add(new Rabbit());
                if (rand.nextDouble() < 0.6) areas[i][j].beings.add(new Mouse());
                if (rand.nextDouble() < 0.6) areas[i][j].beings.add(new Buffalo());
                if (rand.nextDouble() < 0.6) areas[i][j].beings.add(new Duck());
                if (rand.nextDouble() < 0.6) areas[i][j].beings.add(new Caterpillar());
                if (rand.nextDouble() < 0.6) areas[i][j].beings.add(new Sheep());
                if (rand.nextDouble() < 0.6) areas[i][j].beings.add(new Goat());
                if (rand.nextDouble() < 0.6) areas[i][j].beings.add(new Hog());
            }
        }
    }

    public void relocateBeing(LivingBeing being, int oldX, int oldY, int newX, int newY) {
        if (areas[oldX][oldY].beings.remove(being)) {
            areas[newX][newY].beings.add(being);
        }
    }

    public void simulate() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
        scheduler.scheduleAtFixedRate(this::updateBeings, 0, 2, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::printStats, 0, 3, TimeUnit.SECONDS);
    }

    private void updateBeings() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                areas[i][j].plantCount = Math.min(10, areas[i][j].plantCount + 1);

                List<LivingBeing> beingsCopy = new ArrayList<>(areas[i][j].beings);
                for (LivingBeing being : beingsCopy) {
                    being.consume(areas[i][j]);
                    being.move(this, i, j);
                    if (Math.random() < 0.1) {
                        areas[i][j].beings.add(being.reproduce());
                    }
                }
            }
        }
    }

    private void printStats() {
        try {
            System.out.println("--- Статистика острова ---");
            boolean hasBeings = false;

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    List<LivingBeing> beingsCopy = new ArrayList<>(areas[i][j].beings);

                    if (!beingsCopy.isEmpty()) hasBeings = true;

                    Map<String, Integer> speciesCount = new HashMap<>();
                    for (LivingBeing being : beingsCopy) {
                        if (being != null) {
                            speciesCount.put(being.getName(), speciesCount.getOrDefault(being.getName(), 0) + 1);
                        }
                    }
                    System.out.println("Зона [" + i + ", " + j + "]: " + speciesCount + ", Растений: " + areas[i][j].plantCount);
                }
            }

            if (!hasBeings) {
                System.out.println("Все существа вымерли. Симуляция завершена.");
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Island island = new Island(100, 20);
        island.simulate();
    }
}