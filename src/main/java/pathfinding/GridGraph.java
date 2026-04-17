package pathfinding;

import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridGraph {

    private int rows, cols;
    private final Map<Vector2i, Boolean> walls = new HashMap<>();

    public GridGraph(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
    }

    public List<Vector2i> getNeighbors(Vector2i cell) {
        return this.getNeighbors(cell.x, cell.y);
    }

    public List<Vector2i> getNeighbors(int x, int y) {
        List<Vector2i> list = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            Vector2i neighbor = new Vector2i(x + dir.x, y + dir.y);
            if (this.inBounds(neighbor) && !walls.getOrDefault(neighbor, false))
                list.add(neighbor);
        }

        return list;
    }

    public boolean inBounds(Vector2i cell) {
        return this.inBounds(cell.x, cell.y);
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && x < cols && y >= 0 && y < rows;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public Map<Vector2i, Boolean> getWalls() {
        return walls;
    }

    private enum Direction {
        RIGHT(1, 0), LEFT(-1, 0), UP(0, 1), DOWN(0, -1); //E W N S

        public final int x, y;
        Direction(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
