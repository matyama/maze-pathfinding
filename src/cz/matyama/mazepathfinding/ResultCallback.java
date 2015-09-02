package cz.matyama.mazepathfinding;

import java.util.List;

/**
 * @author matyama
 */
public interface ResultCallback {

    void reportResult(List<Coordinate> path);

}
