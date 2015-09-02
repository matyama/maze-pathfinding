package cz.matyama.mazepathfinding;

import cz.matyama.mazepathfinding.agent.AStarAgent;
import cz.matyama.mazepathfinding.agent.Agent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author matyama
 */
public class Maze {

    public static final int FREE = 0x00FFFFFF;
    public static final int WALL = 0x00000000;
    public static final int INIT = 0x00FF0000;
    public static final int GOAL = 0x000000FF;

    private int[][] originalMaze;
    private int[][] maze;

    private int height;
    private int width;

    private Coordinate init;
    private Coordinate goal;

    private List<Coordinate> path;

    private long runtime;

    public void init(String inPath) {

        final Lock lock = new ReentrantLock();
        final Condition done = lock.newCondition();

        lock.lock();
        try {

            final BufferedImage img = ImageIO.read(new File(inPath));

            if (img == null) {
                System.err.println("Cannot read given image.");
                System.exit(1);
            }

            width = img.getWidth();
            height = img.getHeight();

            originalMaze = new int[width][height];
            maze = new int[width][height];

            final int nThreads = Runtime.getRuntime().availableProcessors();
            ExecutorService executor = Executors.newFixedThreadPool(nThreads);

            for (int i = 0; i < nThreads; i++) {

                final int n = i;

                executor.execute(new Runnable() {
                    @Override
                    public void run() {

                        int batchSize = width / nThreads;
                        int from = n * batchSize;
                        int to = n == nThreads-1 ? width : from + batchSize;

                        for (int x = from; x < to; x++) {
                            for (int y = 0; y < height; y++) {

                                int rgb = img.getRGB(x, y) & FREE;
                                originalMaze[x][y] = rgb;
                                maze[x][y] = rgb;

                                if (rgb == INIT) {
                                    init = new Coordinate(x, y);
                                }

                                if (rgb == GOAL) {
                                    goal = new Coordinate(x, y);
                                }

                            }
                        }

                        if (n == nThreads-1) {
                            lock.lock();
                            try {
                                done.signal();
                            } finally {
                                lock.unlock();
                            }
                        }

                    }
                });
            }

            executor.shutdown();
            done.await();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            lock.unlock();
        }

    }

    public void simulate() {

        Agent agent = new AStarAgent();

        final Lock lock = new ReentrantLock();
        final Condition done = lock.newCondition();

        agent.setMaze(maze);
        agent.setInit(init);
        agent.setGoal(goal);

        agent.setCallback(new ResultCallback() {
            @Override
            public void reportResult(List<Coordinate> path) {
                lock.lock();
                try {
                    Maze.this.path = path;
                    done.signal();
                } finally {
                    lock.unlock();
                }
            }
        });

        lock.lock();
        try {

            long tic = System.currentTimeMillis();

            agent.start();
            done.await();

            runtime = System.currentTimeMillis() - tic;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

    }

    private String getStatus(List<Coordinate> path) {

        if (path == null) {
            return "The path does not exist?";
        }

        boolean first = true;
        Coordinate prev = null;

        for (Coordinate coord : path) {

            if (coord.getX() < 0 || coord.getX() >= width || coord.getY() < 0 || coord.getY() >= height) {
                return "Path contains coordinate [" + coord.getX() + ", " + coord.getY() + "] out of the map.";
            }

            if (first) {
                if (originalMaze[coord.getX()][coord.getY()] != INIT) {
                    return "Path does not start at the start square.";
                }
                prev = coord;
                first = false;
                continue;
            }

            if (originalMaze[coord.getX()][coord.getY()] == WALL) {
                return "Path goes through wall.";
            }

            if (!neighboring(coord, prev)) {
                return "Path is not continuous.";
            }

            prev = coord;

        }

        if (prev == null) {
            return "Path does not leave starting square.";
        }

        if (originalMaze[prev.getX()][prev.getY()] != GOAL) {
            return "Path does not finish at the goal square.";
        }

        return "Path is valid.";

    }

    private boolean neighboring(Coordinate coord, Coordinate prev) {
        int diffX = coord.getX() - prev.getX();
        int diffY = coord.getY() - prev.getY();
        return (diffX == 0 && Math.abs(diffY) <= 1) || (diffY == 0 && Math.abs(diffX) <= 1);
    }

    public void printReport() {
        String status = getStatus(path);
        System.out.printf(">>> planning: %d [ms] - %s\n", runtime, status);
    }

    public void exportPath(String outPath) {

        try (FileWriter out = new FileWriter(outPath)) {

            if (path != null) {
                for (Coordinate coordinate : path) {
                    out.write(coordinate.toString());
                    out.write("\n");
                }
            } else {
                out.write("NO PATH\n");
            }

            out.flush();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(final String[] args) {

        if (args.length != 1 && args.length != 2) {
            System.err.println("Invalid number of arguments. Expected 1 or 2, give: " + args.length);
            System.exit(1);
        }

        Maze environment = new Maze();
        environment.init(args[0]);

        environment.simulate();

        environment.printReport();
        if (args.length == 2) {
            environment.exportPath(args[1]);
        }

    }

}
