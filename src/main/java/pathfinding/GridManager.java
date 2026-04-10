package pathfinding;

import cinnamon.gui.Screen;
import cinnamon.gui.widgets.ContainerGrid;
import cinnamon.gui.widgets.types.Button;
import cinnamon.gui.widgets.types.Slider;
import cinnamon.input.InputManager;
import cinnamon.model.GeometryHelper;
import cinnamon.render.MatrixStack;
import cinnamon.render.batch.VertexConsumer;
import cinnamon.settings.Settings;
import cinnamon.text.Text;
import cinnamon.utils.Alignment;
import cinnamon.utils.Resource;
import org.joml.Vector2i;
import pathfinding.search.*;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class GridManager extends Screen {

    public static final int
            TILE_SIZE = 20,

            COLOR_WALKABLE      = 0xFFE8E8E8,
            COLOR_WALKABLE_LINE = 0xFFF8F8F8,
            COLOR_WALL          = 0xFF555555,
            COLOR_WALL_LINE     = 0xFF888888,
            COLOR_START         = 0xFFFF4444,
            COLOR_GOAL          = 0xFF4444FF,

            COLOR_EXPLORED      = 0x99ADD8E6,
            COLOR_FRONTIER      = 0x99FFFF99,
            COLOR_PATH          = 0xFFFFD700;

    public static final Resource GUI_STYLE = new Resource("pathfinding", "style.json");

    private final Vector2i
            startPos = new Vector2i(2, 2),
            goalPos = new Vector2i(15, 10);

    private final GridGraph graph = new GridGraph(12, 17);
    private final List<Vector2i> path = new ArrayList<>();

    private AlgoType selectedAlgorithm = AlgoType.BFS;
    private SearchAlgorithm currentSearch;
    private boolean isDrawingWalls = true;
    public static int delay = 1;

    public GridManager() {}

    @Override
    public void init() {
        super.init();

        int w = 60, h = 12;

        //add buttons
        ContainerGrid grid = new ContainerGrid(width - 12, 12, 2);
        grid.setAlignment(Alignment.TOP_RIGHT);
        addWidget(grid);

        Button simulate = new Button(0, 0, w, h, Text.of("Simulate!"), butt -> updatePath());
        simulate.setStyle(GUI_STYLE);
        grid.addWidget(simulate);

        Button bfs = new Button(0, 0, w, h, Text.of("BFS"), butt -> selectedAlgorithm = AlgoType.BFS);
        bfs.setStyle(GUI_STYLE);
        grid.addWidget(bfs);

        Button dijkstra = new Button(0, 0, w, h, Text.of("Dijkstra"), butt -> selectedAlgorithm = AlgoType.DIJKSTRA);
        dijkstra.setStyle(GUI_STYLE);
        grid.addWidget(dijkstra);

        Button greedy = new Button(0, 0, w, h, Text.of("Greedy"), butt -> selectedAlgorithm = AlgoType.GREEDY);
        greedy.setStyle(GUI_STYLE);
        grid.addWidget(greedy);

        Button aStar = new Button(0, 0, w, h, Text.of("A*"), butt -> selectedAlgorithm = AlgoType.ASTAR);
        aStar.setStyle(GUI_STYLE);
        grid.addWidget(aStar);

        //spacer
        Button spacer = new Button(0, 0, w, h, Text.of(""), null);
        spacer.setStyle(GUI_STYLE);
        spacer.setInvisible(true);
        spacer.setActive(false);
        grid.addWidget(spacer);

        //delay slider
        Slider slider = new Slider(0, 0, w);
        slider.setMax(4);
        slider.setStepCount(5);
        slider.setValue(delay);
        slider.setChangeListener((f, i) -> delay = (int) Math.pow(10, (i - 1)));
        slider.setTooltipFunction((f, i) -> Text.of("Delay between steps: ").append(Text.of((int) Math.pow(10, (i - 1)) + "ms")));
        slider.setStyle(GUI_STYLE);
        grid.addWidget(slider);

        //zoom buttons
        ContainerGrid zoom = new ContainerGrid(width - 12, height - 12, 4, 2);
        zoom.setAlignment(Alignment.BOTTOM_RIGHT);
        addWidget(zoom);

        Button zoomOut = new Button(0, 0, h, h, Text.of("-"), butt -> {
            float old = client.window.guiScale;
            float newer = Math.max(old - 1, 1);
            if (old != newer) {
                Settings.guiScale.set(newer);
                client.windowResize(client.window.width, client.window.height);
            }
        });
        zoomOut.setStyle(GUI_STYLE);
        zoomOut.setTooltip(Text.of("Zoom out"));
        zoom.addWidget(zoomOut);

        Button zoomIn = new Button(0, 0, h, h, Text.of("+"), butt -> {
            float old = client.window.guiScale;
            float newer = Math.min(old + 1, client.window.maxGuiScale);
            if (old != newer) {
                Settings.guiScale.set(newer);
                client.windowResize(client.window.width, client.window.height);
            }
        });
        zoomIn.setStyle(GUI_STYLE);
        zoomIn.setTooltip(Text.of("Zoom in"));
        zoom.addWidget(zoomIn);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        draw(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    protected void renderBackground(MatrixStack matrices, float delta, int color1, int color2, float size) {
        renderSolidBackground(0xFF4D4D4D);
    }

    public void draw(MatrixStack matrices) {
        //draw cells
        for (int y = 0; y < graph.getRows(); y++) {
            for (int x = 0; x < graph.getCols(); x++) {
                Vector2i currentCell = new Vector2i(x, y);

                int fillColor = COLOR_WALKABLE;
                int lineColor = COLOR_WALKABLE_LINE;

                //if on wall
                if (graph.getWalls().getOrDefault(currentCell, false)) {
                    fillColor = COLOR_WALL;
                    lineColor = COLOR_WALL_LINE;
                } else if (currentSearch != null) {
                    //if fully processed (closed set)
                    if (currentSearch.getClosedSet().contains(currentCell)) {
                        fillColor = COLOR_EXPLORED;
                    }
                    //if found, but not fully processed (frontier)
                    else if (currentSearch.getVisited().containsKey(currentCell)) {
                        fillColor = COLOR_FRONTIER;
                    }
                }

                //draw outline as a full quad below the inner quad
                float lineWidth = 1.5f;
                VertexConsumer.MAIN.consume(
                        GeometryHelper.rectangle(matrices,
                                x * TILE_SIZE, y * TILE_SIZE,
                                (x + 1) * TILE_SIZE, (y + 1) * TILE_SIZE,
                                lineColor
                        )
                );
                VertexConsumer.MAIN.consume(
                        GeometryHelper.rectangle(matrices,
                                x * TILE_SIZE + lineWidth, y * TILE_SIZE + lineWidth,
                                (x + 1) * TILE_SIZE - lineWidth, (y + 1) * TILE_SIZE - lineWidth,
                                fillColor
                        )
                );
            }
        }

        //draw path
        float halfTile = TILE_SIZE / 2f;
        for (int i = 0; i < path.size() - 1; i++) {
            Vector2i from = path.get(i);
            Vector2i to = path.get(i + 1);

            VertexConsumer.MAIN.consume(
                    GeometryHelper.line(matrices,
                            from.x * TILE_SIZE + halfTile, from.y * TILE_SIZE + halfTile,
                            to.x * TILE_SIZE + halfTile, to.y * TILE_SIZE + halfTile,
                            4f, COLOR_PATH
                    )
            );
        }

        //draw player
        VertexConsumer.MAIN.consume(
                GeometryHelper.circle(matrices,
                        startPos.x * TILE_SIZE + halfTile, startPos.y * TILE_SIZE + halfTile,
                        TILE_SIZE * 0.35f, 24, COLOR_START)
        );

        //draw goal
        if (!goalPos.equals(-1, -1)) {
            float margin = TILE_SIZE * 0.25f;
            float tlX = goalPos.x * TILE_SIZE + margin, tlY = goalPos.y * TILE_SIZE + margin;
            float brX = (goalPos.x + 1) * TILE_SIZE - margin, brY = (goalPos.y + 1) * TILE_SIZE - margin;
            VertexConsumer.MAIN.consume(
                    GeometryHelper.line(matrices, tlX, tlY, brX, brY, 3f, COLOR_GOAL)
            );
            VertexConsumer.MAIN.consume(
                    GeometryHelper.line(matrices, tlX, brY, brX, tlY, 3f, COLOR_GOAL)
            );
        }
    }

    public void updatePath() {
        clearVisuals();

        switch (selectedAlgorithm) {
            case BFS      -> currentSearch = new BFS(graph);
            case DIJKSTRA -> currentSearch = new Dijkstra(graph);
            case GREEDY   -> currentSearch = new Greedy(graph);
            case ASTAR    -> currentSearch = new AStar(graph);
        }

        if (goalPos.equals(-1, -1))
            return;

        //run the search in a separate thread
        new Thread(() -> currentSearch.solve(startPos, goalPos, path)).start();
    }

    public void clearVisuals() {
        if (currentSearch != null)
            currentSearch.setCancelled(true);
        path.clear();
    }

    @Override
    public boolean mousePress(int button, int action, int mods) {
        boolean sup = super.mousePress(button, action, mods);
        if (sup) return true;

        if (action != GLFW_PRESS)
            return false;

        boolean mapChanged = false;
        Vector2i gridPos = new Vector2i(
                (int) Math.floor(client.window.mouseX / (float) TILE_SIZE),
                (int) Math.floor(client.window.mouseY / (float) TILE_SIZE)
        );

        if (!graph.inBounds(gridPos))
            return false;

        switch (button) {
            //left button set the goal position
            case GLFW_MOUSE_BUTTON_LEFT -> {
                if (!gridPos.equals(startPos) && !gridPos.equals(goalPos) && !graph.getWalls().getOrDefault(gridPos, false)) {
                    goalPos.set(gridPos);
                    mapChanged = true;
                }
            }
            //right button toggle walls
            case GLFW_MOUSE_BUTTON_RIGHT -> {
                if (graph.getWalls().getOrDefault(gridPos, false)) {
                    isDrawingWalls = false;
                    graph.getWalls().remove(gridPos);
                    mapChanged = true;
                } else if (!gridPos.equals(startPos) && !gridPos.equals(goalPos)) {
                    isDrawingWalls = true;
                    graph.getWalls().put(gridPos, true);
                    mapChanged = true;
                }
            }
        }

        if (mapChanged) {
            clearVisuals();
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseMove(int x, int y) {
        boolean sup = super.mouseMove(x, y);
        if (sup) return true;

        //check for right mouse button
        if (!InputManager.isMousePressed(GLFW_MOUSE_BUTTON_RIGHT))
            return false;

        Vector2i gridPos = new Vector2i(
                (int) Math.floor(client.window.mouseX / (float) TILE_SIZE),
                (int) Math.floor(client.window.mouseY / (float) TILE_SIZE)
        );

        if (!graph.inBounds(gridPos))
            return false;

        //draw or erase walls
        if (isDrawingWalls) {
            if (!gridPos.equals(startPos) && !gridPos.equals(goalPos) && !graph.getWalls().getOrDefault(gridPos, false)) {
                graph.getWalls().put(gridPos, true);
                clearVisuals();
            }
        } else {
            if (graph.getWalls().getOrDefault(gridPos, false)) {
                graph.getWalls().remove(gridPos);
                clearVisuals();
            }
        }

        return true;
    }

    @Override
    public boolean keyPress(int key, int scancode, int action, int mods) {
        boolean sup = super.keyPress(key, scancode, action, mods);
        if (sup) return true;

        //remove all walls
        if (action == GLFW_PRESS && key == GLFW_KEY_C) {
            graph.getWalls().clear();
            clearVisuals();
            return true;
        }

        return false;
    }

    public Vector2i getStartPos() {
        return startPos;
    }

    public void setStartPos(Vector2i startPos) {
        this.setStartPos(startPos.x, startPos.y);
    }

    public void setStartPos(int x, int y) {
        this.startPos.set(x, y);
    }

    public Vector2i getGoalPos() {
        return goalPos;
    }

    public void setGoalPos(Vector2i goalPos) {
        this.setGoalPos(goalPos.x, goalPos.y);
    }

    public void setGoalPos(int x, int y) {
        this.goalPos.set(x, y);
    }
}
