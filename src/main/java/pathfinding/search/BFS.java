package pathfinding.search;

import pathfinding.GridGraph;

public class BFS extends SearchAlgorithm {

    public BFS(GridGraph graph) {
        super(graph);
    }

    @Override
    protected void addToFrontier(SearchNode node) {
        //send to end of list
        frontier.offer(node);
    }

    @Override
    protected SearchNode getFromFrontier() {
        //pop the first
        return frontier.poll();
    }
}
