package cz.matyama.mazepathfinding.agent;

import cz.matyama.mazepathfinding.Coordinate;
import cz.matyama.mazepathfinding.ResultCallback;

import java.util.List;

/**
 * @author matyama
 */
public abstract class Agent extends Thread {

    protected int[][] maze;

    protected Coordinate init;
    protected Coordinate goal;

    private ResultCallback callback;

    protected abstract List<Coordinate> findPath(int[][] maze, Coordinate init, Coordinate goal);

    public void setMaze(int[][] maze) {
        this.maze = maze;
    }

    public void setInit(Coordinate init) {
        this.init = init;
    }

    public void setGoal(Coordinate goal) {
        this.goal = goal;
    }

    public void setCallback(ResultCallback callback) {
        this.callback = callback;
    }

    @Override
    public void run() {

        if (maze == null || callback == null) {
            throw new NullPointerException("Maze or result callback are uninitialized.");
        }

        List<Coordinate> path = findPath(maze, init, goal);
        callback.reportResult(path);

        super.run();
    }

}
