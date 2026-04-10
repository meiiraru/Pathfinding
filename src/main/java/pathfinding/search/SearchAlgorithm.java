package pathfinding.search;

import org.joml.Vector2i;
import pathfinding.GridGraph;
import pathfinding.GridManager;

import java.util.*;

public abstract class SearchAlgorithm {

    protected GridGraph graph;
    protected Queue<SearchNode> frontier = new LinkedList<>();
    protected Map<Vector2i, SearchNode> visited = new HashMap<>();
    protected Set<Vector2i> closedSet = new HashSet<>();
    protected boolean cancelled = false;

    public SearchAlgorithm(GridGraph graph) {
        this.graph = graph;
    }

    public void solve(Vector2i start, Vector2i goal, List<Vector2i> out) {
        frontier.clear();
        visited.clear();
        closedSet.clear();
        cancelled = false;

        SearchNode startNode = new SearchNode(start);
        this.addToFrontier(startNode);
        visited.put(start, startNode);

        //BFS generic implementation
        while (!frontier.isEmpty()) {
            if (cancelled) return;

            if (GridManager.delay > 0) {
                try {
                    Thread.sleep(GridManager.delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            //flag processed
            SearchNode currentNode = this.getFromFrontier();
            closedSet.add(currentNode.getCell());

            if (currentNode.getCell().equals(goal)) {
                out.addAll(this.reconstructPath(currentNode));
                return;
            }

            //add neighbors to frontier
            for (Vector2i neighbor : graph.getNeighbors(currentNode.getCell())) {
                if (visited.containsKey(neighbor))
                    continue;

                SearchNode newNode = new SearchNode(neighbor, currentNode);
                visited.put(neighbor, newNode);
                this.addToFrontier(newNode);
            }
        }
    }

    protected List<Vector2i> reconstructPath(SearchNode node) {
        List<Vector2i> path = new ArrayList<>();
        SearchNode current = node;

        while (current != null) {
            path.addFirst(current.getCell());
            current = current.getParent();
        }

        return path;
    }

    protected abstract void addToFrontier(SearchNode node);

    protected abstract SearchNode getFromFrontier();

    public GridGraph getGraph() {
        return graph;
    }

    public Queue<SearchNode> getFrontier() {
        return frontier;
    }

    public Map<Vector2i, SearchNode> getVisited() {
        return visited;
    }

    public Set<Vector2i> getClosedSet() {
        return closedSet;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
