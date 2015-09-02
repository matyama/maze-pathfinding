package cz.matyama.mazepathfinding.agent;

import cz.matyama.mazepathfinding.Coordinate;
import cz.matyama.mazepathfinding.Maze;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author matyama
 */
public class AStarAgent extends Agent {

    private int[][] maze;

    private int width;
    private int height;

    private int gx;
    private int gy;

    private OpenList open;

    @Override
    public List<Coordinate> findPath(int[][] maze, Coordinate init, Coordinate goal) {

        this.maze = maze;
        width = maze.length;
        height = maze[0].length;

        gx = goal.getX();
        gy = goal.getY();

        open = new OpenList(height * width);
        open.add(new Node(init));

        while (!open.isEmpty()) {
            Node n = open.poll();
            if (n.isGoal()) {
                return n.getPath();
            }
            n.expand();
        }

        return null;
    }

    private class Node implements Comparable<Node> {

        private final int x;
        private final int y;

        private final int g;
        private final int h;

        private final Node p; // parent/predecessor

        // open-list (heap) fields
        private List<Node> children;
        private int order;

        private Node(int x, int y, int g, Node p) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.h = Math.abs(x - gx) + Math.abs(y - gy); // Manhattan distance
            this.p = p;
            children = new ArrayList<>();
            order = 0;
        }

        public Node(Coordinate init) {
            this(init.getX(), init.getY(), 0, null);
        }

        public int f() {
            return g + h;
        }

        public boolean isGoal() {
            return h == 0;
        }

        public void expand() {
            maze[x][y] = Maze.WALL; // first mark this state as visited
            if (x + 1 < width && maze[x + 1][y] != Maze.WALL) {
                open.add(new Node(x + 1, y, g + 1, this));
            }
            if (x >= 1 && maze[x - 1][y] != Maze.WALL) {
                open.add(new Node(x - 1, y, g + 1, this));
            }
            if (y + 1 < height && maze[x][y + 1] != Maze.WALL) {
                open.add(new Node(x, y + 1, g + 1, this));
            }
            if (y >= 1 && maze[x][y - 1] != Maze.WALL) {
                open.add(new Node(x, y - 1, g + 1, this));
            }
        }

        public List<Coordinate> getPath() {
            LinkedList<Coordinate> path = new LinkedList<>();
            Node n = this;
            do {
                path.addFirst(new Coordinate(n.x, n.y));
                n = n.p;
            } while (n != null);
            return path;
        }

        @Override
        public int compareTo(Node n) {
            return f() - n.f();
        }

    }

    /**
     * Open List implementation using fixed-size binomial heap
     */
    private class OpenList {

        private Node[] list;
        private Node min;

        @SuppressWarnings("unchecked")
        public OpenList(int capacity) {
            min = null;
            list = new Node[((int) (Math.log(capacity) / Math.log(2))) + 2];
        }

        public void add(Node n) {
            if (list[0] != null) {
                merge(n, list[0]);
            } else {
                list[0] = n;
            }
            if (min == null || min.compareTo(n) > 0) {
                min = n;
            }
        }

        public Node poll() {

            if (min == null) {
                return null;
            }

            Node result = min;
            list[min.order] = null;
            for (Node n : min.children) {
                if (list[n.order] != null) {
                    merge(n, list[n.order]);
                } else {
                    list[n.order] = n;
                }
            }

            min.children.clear();

            Node minNode = null;
            for (Node n : list) {
                if (n != null) {
                    if (minNode == null) {
                        minNode = n;
                    } else if (minNode.compareTo(n) > 0) {
                        minNode = n;
                    }
                }
            }

            min = minNode;
            return result;
        }

        private void merge(Node a, Node b) {
            int tmpOrder = a.order;
            list[tmpOrder] = null;
            Node newRoot = mergeTrees(a, b);
            if (list[tmpOrder + 1] == null) {
                list[tmpOrder + 1] = newRoot;
            } else {
                merge(newRoot, list[tmpOrder + 1]);
            }
        }

        private Node mergeTrees(Node a, Node b) {
            Node newRoot;
            if (a.compareTo(b) < 0) {
                a.children.add(b);
                a.order++;
                newRoot = a;
            } else {
                b.children.add(a);
                b.order++;
                newRoot = b;
            }
            return newRoot;
        }

        public boolean isEmpty() {
            return min == null;
        }

    }

}
