//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.sunmisc.btree.cow;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

import me.sunmisc.btree.Page;
import me.sunmisc.btree.heap.Indexes;
import me.sunmisc.btree.heap.Nodes;
import me.sunmisc.btree.heap.Table;
import me.sunmisc.btree.index.Index;

import static me.sunmisc.btree.cow.Tree.THRESHOLD;

public final class LeafPage implements Page {
    private final Table table;
    private final Nodes nodes;
    private Indexes data;

    public LeafPage(Indexes data, Table table, Nodes nodes) {
        this.table = table;
        this.data = data;
        this.nodes = nodes;
    }

    public Page.Split tryPushOrSplit(String key, String value) {
        Index pos = this.table.alloc(key, value);
        int index = this.binSearch(key);
        data = index >= 0
                ? this.data.set(index, pos)
                : this.data.add(-index - 1, pos);
        return data.size() < THRESHOLD
                ? new Page.UnarySplit(this.nodes.alloc(
                        new LeafPage(data, this.table, this.nodes)), this.data)
                : split();
    }

    public Page.Split split() {
        int size = this.data.size();
        int mid = size >>> 1;
        LeafPage left = new LeafPage(
                this.data.sub(0, mid),
                this.table, this.nodes);
        LeafPage right = new LeafPage(
                this.data.sub(mid, this.data.size()),
                this.table, this.nodes);
        return new Page.RebalanceSplit(
                this.nodes.alloc(left),
                this.data,
                List.of(this.nodes.alloc(right))
        );
    }

    public Optional<String> search(String key) {
        int index = this.binSearch(key);
        if (index < 0) {
            return Optional.empty();
        } else {
            return Optional.of(this.data.get(index))
                    .map(table::entry)
                    .map(Map.Entry::getValue);
        }

    }

    public Indexes children() {
        return Indexes.EMPTY;
    }

    public Indexes keys() {
        return this.data;
    }

    public void traverse(List<String> result) {
        for(int i = 0; i < this.data.size(); ++i) {
            result.add(this.table.key(this.data.get(i)));
        }

    }
    private int binSearch(String key) {
        int i = 0;
        int xx = Integer.parseInt(key);
        for (; i < data.size(); ++i) {
            Index index = this.data.get(i);
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
        int high = this.data.size() - 1;

        while(low <= high) {
            int mid = low + high >>> 1;
            Index index = this.data.get(mid);
            String midVal = this.table.key(index);
            int cmp = midVal.compareTo(key);
            if (cmp < 0) {
                low = mid + 1;
            } else {
                if (cmp <= 0) {
                    return mid;
                }

                high = mid - 1;
            }
        }

        return -(low + 1);
    }
}
