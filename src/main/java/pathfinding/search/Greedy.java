package pathfinding.search;

import pathfinding.GridGraph;

public class Greedy extends SearchAlgorithm {

    public Greedy(GridGraph graph) {
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
