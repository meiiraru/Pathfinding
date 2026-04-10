package pathfinding.search;

import org.joml.Vector2i;
import pathfinding.GridGraph;
import pathfinding.GridManager;

import java.util.List;

public class Greedy extends SearchAlgorithm {

    private final PriorityQueue<SearchNode> pq = new PriorityQueue<>();

    public Greedy(GridGraph graph) {
        super(graph);
    }

    protected float heuristic(Vector2i a, Vector2i b) {
        //manhattan distance
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    @Override
    public void solve(Vector2i start, Vector2i goal, List<Vector2i> out) {
        frontier.clear();
        visited.clear();
        closedSet.clear();
        pq.clear();
        cancelled = false;

        SearchNode startNode = new SearchNode(start, null, 0f);
        startNode.sethCost(heuristic(start, goal));

        this.addToFrontier(startNode);
        visited.put(start, startNode);

        while (!pq.isEmpty()) {
            if (cancelled) return;

            if (GridManager.delay > 0) {
                try {
                    Thread.sleep(GridManager.delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            SearchNode currentNode = this.getFromFrontier();
            closedSet.add(currentNode.getCell());

            if (currentNode.getCell().equals(goal)) {
                out.addAll(this.reconstructPath(currentNode));
                return;
            }

            for (Vector2i neighbor : graph.getNeighbors(currentNode.getCell())) {
                if (!visited.containsKey(neighbor)) {
                    SearchNode newNode = new SearchNode(neighbor, currentNode, 0f);
                    newNode.sethCost(heuristic(neighbor, goal));

                    visited.put(neighbor, newNode);
                    this.addToFrontier(newNode);
                }
            }
        }
    }

    @Override
    protected void addToFrontier(SearchNode node) {
        pq.put(node, node.gethCost());
    }

    @Override
    protected SearchNode getFromFrontier() {
        return pq.getItem();
    }
}
