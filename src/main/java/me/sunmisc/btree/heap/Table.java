package me.sunmisc.btree.heap;

import me.sunmisc.btree.index.Index;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public final class Table {
    private final Versions versions;
    private final Values values;
    private final Nodes nodes;

    public Table(String name) throws IOException {
        File file = new File(name);
        if (!file.exists()) {
            file.mkdirs();
        }
        this.values = new Values(new File(file, "values.dat"));
        this.nodes = new Nodes(new File(file, "indexes.dat"), this);
        this.versions = new Versions(new File(file, "history.dat"));
    }

    public Nodes nodes() {
        return nodes;
    }

    public Values values() {
        return values;
    }

    public Versions versions() {
        return versions;
    }

    public Optional<Index> root() {
        return nodes.tail();
    }

    public void delete() {
        values.delete();
        versions.delete();
        nodes.delete();
    }
}
