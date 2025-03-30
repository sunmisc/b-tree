package mc.sunmisc.tree.io;

import mc.sunmisc.tree.io.heap.Indexes;
import mc.sunmisc.tree.io.heap.Nodes;
import mc.sunmisc.tree.io.heap.Table;
import mc.sunmisc.tree.io.heap.ArrayIndexes;
import mc.sunmisc.tree.io.index.Index;
import java.util.*;
import java.util.stream.Stream;

import static mc.sunmisc.tree.io.BPlusTreeIo.THRESHOLD;

public final class InternalPage implements Page {
    private final Nodes nodes;
    private final Table table;
    private Indexes keys;
    private Indexes children;

    public InternalPage(Table table, Nodes nodes, Indexes keys, Indexes children) {
        this.table = table;
        this.nodes = nodes;
        this.keys = keys;
        this.children = children;
    }

    @Override
    public Split tryPushOrSplit(String key, Void value) {
        final int idx = binSearch(key);
        final int index = idx < 0 ? -idx - 1 : idx;
        Split split = nodes.load(children.get(index)).tryPushOrSplit(key, value);
        children = children.set(index, split);
        Iterator<Index> itr = split.iterator();
        if (itr.hasNext()) {
            children = children.add(index + 1, itr.next());
            keys = keys.add(index, split.median());
        }
        if (keys.size() < THRESHOLD) {
            return new UnarySplit(
                    nodes.allocate(new InternalPage(table, nodes, keys, children))
            );
        }
        return split();
    }

    private Split split() {
        final int mid = keys.size() >>> 1;
        final Index midKey = keys.get(mid);
        final Page left = new InternalPage(table, nodes,
                keys.sub(0, mid),
                children.sub(0, mid + 1)
        );
        final Page right = new InternalPage(
                table,
                nodes,
                keys.sub(mid + 1, keys.size()),
                children.sub(mid + 1, children.size()));
        return new RebalanceSplit(
                nodes.allocate(left),
                keys,
                List.of(nodes.allocate(right))
        );
    }

    @Override
    public Optional<Boolean> search(String key) {
        int index = binSearch(key);
        index = (index >= 0) ? index + 1 : -index - 1;
        return nodes.load(children.get(index)).search(key);
    }

    private int binSearch(final String key) {
        int low = 0;
        int high = keys.size() - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            String midVal = table.load(keys.get(mid));
            int cmp = midVal.compareTo(key);
            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        return -(low + 1);  // key not found
    }

    @Override
    public Indexes keys() {
        return keys;
    }

    @Override
    public Indexes children() {
        return children;
    }
}
