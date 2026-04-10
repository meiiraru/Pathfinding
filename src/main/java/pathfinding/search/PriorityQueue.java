package pathfinding.search;

import cinnamon.utils.Pair;

import java.util.ArrayList;
import java.util.List;

public class PriorityQueue<T> {

    private final List<Pair<T, Float>> elements = new ArrayList<>();

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public void put(T item, float priority) {
        int low = 0, high = elements.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            float midPriority = elements.get(mid).second();

            if (midPriority == priority) {
                low = mid;
                break;
            }
            //left side
            else if (midPriority < priority) {
                high = mid - 1;
            }
            //right side
            else {
                low = mid + 1;
            }
        }

        elements.add(low, new Pair<>(item, priority));
    }

    public T getItem() {
        return isEmpty() ? null : elements.getLast().first();
    }
}
