package pathfinding.search;

import org.joml.Vector2i;

public class SearchNode {

    private final Vector2i cell;
    private final SearchNode parent;
    float gCost, hCost;

    public SearchNode(Vector2i cell) {
        this(cell, null, 0f);
    }

    public SearchNode(Vector2i cell, SearchNode parent) {
        this(cell, parent, 0f);
    }

    public SearchNode(Vector2i cell, SearchNode parent, float gCost) {
        this.cell = cell;
        this.parent = parent;
        this.gCost = gCost;
    }

    public Vector2i getCell() {
        return cell;
    }

    public SearchNode getParent() {
        return parent;
    }

    public float getfCost() {
        return gCost + hCost;
    }

    public float getgCost() {
        return gCost;
    }

    public float gethCost() {
        return hCost;
    }
}
