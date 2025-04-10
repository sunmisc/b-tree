//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.sunmisc.btree.cow;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import me.sunmisc.btree.Page;
import me.sunmisc.btree.heap.Indexes;
import me.sunmisc.btree.heap.Nodes;
import me.sunmisc.btree.heap.Table;
import me.sunmisc.btree.index.Index;

import static me.sunmisc.btree.cow.Tree.THRESHOLD;

public final class InternalPage implements Page {
    private final Nodes nodes;
    private final Table table;
    private Indexes keys;
    private Indexes children;

    public InternalPage(Table table, Nodes nodes, Indexes data, Indexes children) {
        this.table = table;
        this.nodes = nodes;
        this.keys = data;
        this.children = children;
    }

    public Page.Split tryPushOrSplit(String key, String value) {
        int idx = this.binSearch(key);
        int index = idx < 0 ? -idx - 1 : idx;
        Page.Split split = this.nodes
                .find(this.children.get(index))
                .tryPushOrSplit(key, value);
        this.children = this.children.set(index, split);
        Iterator<Index> rights = split.iterator();
        if (rights.hasNext()) {
            Index[] inc = StreamSupport
                    .stream(split.spliterator(), false)
                    .toArray(Index[]::new);
            this.children = this.children.add(index + 1, inc);
            this.keys = this.keys.add(index, split.median());
        }
        if (keys.size() < THRESHOLD) {
            return new Page.UnarySplit(
                    this.nodes.alloc(
                            new InternalPage(this.table,
                                    this.nodes,
                                    this.keys,
                                    this.children
                            )
                    ), this.keys);
        } else {
            return split();
        }
    }

    private Page.Split split() {
        int mid = this.keys.size() >>> 1;
        Page left = new InternalPage(this.table, this.nodes,
                this.keys.sub(0, mid),
                this.children.sub(0, mid + 1));
        Page right = new InternalPage(this.table, this.nodes,
                this.keys.sub(mid + 1, this.keys.size()),
                this.children.sub(mid + 1, this.children.size())
        );
        return new Page.RebalanceSplit(
                this.nodes.alloc(left),
                this.keys,
                List.of(this.nodes.alloc(right))
        );
    }

    public Optional<String> search(String key) {
        int index = this.binSearch(key);
        index = index >= 0 ? index + 1 : -index - 1;
        return this.nodes.find(this.children.get(index)).search(key);
    }

    public Indexes keys() {
        return this.keys;
    }

    public void traverse(List<String> result) {
        int i;
        for(i = 0; i < this.keys.size(); ++i) {
            Page page = this.nodes.find(this.children.get(i));
            page.traverse(result);
           // result.add(this.table.key(this.keys.get(i)));
        }

        Page page = this.nodes.find(this.children.get(i));
        page.traverse(result);
    }

    public Indexes children() {
        return this.children;
    }

    private int binSearch(String key) {
        int i = 0;
        int xx = Integer.parseInt(key);
        for (; i < keys.size(); ++i) {
            Index index = this.keys.get(i);
            int val = Integer.parseInt(this.table.key(index));
            if (Objects.equals(xx, val)) {
                return i;
            } else if (Integer.parseInt(key) < val) {
                break;
            }
        }
        return -(i + 1);
    }
    private int binSearch1(String key) {
        int low = 0;
        int high = this.keys.size() - 1;
        while (low <= high) {
            int mid = low + high >>> 1;
            String midVal = this.table.key(this.keys.get(mid));
            int cmp = midVal.compareTo(key);
            if (cmp < 0) {
                low = mid + 1;
            } else {
                if (cmp == 0) {
                    return mid;
                }

                high = mid - 1;
            }
        }

        return -(low + 1);
    }
}
