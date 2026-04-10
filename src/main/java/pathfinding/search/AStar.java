package pathfinding.search;

import pathfinding.GridGraph;

public class AStar extends SearchAlgorithm {

    public AStar(GridGraph graph) {
        super(graph);
    }

    @Override
    protected void addToFrontier(SearchNode node) {

    }

    @Override
    protected SearchNode getFromFrontier() {
        return null;
    }
}
