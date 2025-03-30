package mc.sunmisc.tree.io;

import mc.sunmisc.tree.io.heap.Indexes;
import mc.sunmisc.tree.io.heap.Nodes;
import mc.sunmisc.tree.io.heap.Table;
import mc.sunmisc.tree.io.index.Index;
import java.util.*;

import static mc.sunmisc.tree.io.BPlusTreeIo.THRESHOLD;

public final class LeafPage implements Page {
    private final Table table;
    private final Nodes nodes;
    private final Indexes keys;

    public LeafPage(Indexes keys, Table table, Nodes nodes) {
        this.table = table;
        this.keys = keys;
        this.nodes = nodes;
    }

    @Override
    public Split tryPushOrSplit(String key, Void dummy) {
        final Index value = table.allocate(key);
        final int index = binSearch(key);
        final Indexes copy = index >= 0
                ? keys.set(index, value)
                : keys.add(-index - 1, value);
        if (copy.size() < THRESHOLD) {
            return new UnarySplit(nodes.allocate(new LeafPage(copy, table, nodes)));
        }
        final int size = copy.size();
        final int mid = size >>> 1;
        final LeafPage left = new LeafPage(
                copy.sub(0, mid),
                table,
                nodes
        );
        final LeafPage right = new LeafPage(
                copy.sub(mid, copy.size()),
                table,
                nodes
        );
        return new RebalanceSplit(
                nodes.allocate(left),
                copy,
                List.of(nodes.allocate(right))
        );
    }

    @Override
    public Optional<Boolean> search(String key) {
        final int index = binSearch(key);
        return index < 0
                ? Optional.empty()
                : Optional.of(true);
    }

    @Override
    public Indexes children() {
        return Indexes.EMPTY;
    }

    @Override
    public Indexes keys() {
        return keys;
    }

    private int binSearch(String key) {
        int low = 0;
        int high = keys.size()-1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            String midVal = table.load(keys.get(mid));
            int cmp = midVal.compareTo(key);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found
    }
}