package pathfinding.search;

import pathfinding.GridGraph;

public class Dijkstra extends SearchAlgorithm {

    public Dijkstra(GridGraph graph) {
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
