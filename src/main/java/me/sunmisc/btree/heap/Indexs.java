package me.sunmisc.btree.heap;

import me.sunmisc.btree.index.Index;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class Indexs extends ArrayList<Index> implements Data {

    public Indexs(final Collection<Index> indices) {
        super(indices);
    }

    public Indexs(final Index... indices) {
        super(List.of(indices));

    }

    @Override
    public Indexs subList(int fromIndex, int toIndex) {
        return new Indexs(super.subList(fromIndex, toIndex));
    }

    @Override
    public InputStream delta() throws IOException {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(bytes)) {
            for (Index index : this) {
                dos.writeLong(index.offset());
            }
            return new ByteArrayInputStream(bytes.toByteArray());
        }
    }
}
