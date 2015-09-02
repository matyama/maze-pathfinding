Maze Pathfinding
================

Simple maze pathfinding agent implementing A* search algorithm. Project is inspired by seminar work from course 
Introduction to Artificial Intelligence (A4B33ZUI @ OI FEE CTU).

Execution:
----------
- program (main class *Maze*) takes one compulsory (path to the maze) and one optional argument (output path)
- the maze is given in an image where one pixel represents one cell in the maze (black = wall, red = start, blue = goal)
- provided execution script *plan.sh*
- script assumes an executable jar named *maze-pathfinding.jar* in the root of the project

Output (optional):
------------------
- path to which is the resulting path written
- one line contains one location in the path (in ordered fashion)
- location is represented by x (width) and y (height) positions in the grid, separated by one space
