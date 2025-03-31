package me.sunmisc.btree;

import me.sunmisc.btree.heap.ArrayIndexes;
import me.sunmisc.btree.heap.Indexes;
import me.sunmisc.btree.index.Index;
import me.sunmisc.btree.index.LongIndex;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

public final class ArrayIndexesTest {

    @Test
    public void addToHead() {
        final List<Index> indices = LongStream.range(0, 16)
                .mapToObj(i -> (Index)new LongIndex(i))
                .toList();
        final Indexes indexes = new ArrayIndexes().add(0, indices.toArray(Index[]::new));
        MatcherAssert.assertThat(
                "The added indices should match the expected indices",
                indices,
                CoreMatchers.equalTo(StreamSupport
                        .stream(indexes.spliterator(), false)
                        .toList())
        );
    }
    @Test
    public void addToTail() {
        final List<Index> initial = LongStream.range(0, 8)
                .mapToObj(i -> (Index)new LongIndex(i))
                .toList();

        final List<Index> toAdd = LongStream.range(8, 16)
                .mapToObj(i -> (Index)new LongIndex(i))
                .toList();

        final Indexes indexes = new ArrayIndexes()
                .add(0, initial.toArray(Index[]::new))
                .add(initial.size(), toAdd.toArray(Index[]::new));

        final List<Index> expected = LongStream.range(0, 16)
                .mapToObj(i -> (Index)new LongIndex(i))
                .toList();

        MatcherAssert.assertThat(
                "Indices should be correctly added to the tail",
                expected,
                CoreMatchers.equalTo(
                        StreamSupport.stream(indexes.spliterator(), false).toList()
                )
        );
    }
    @Test
    public void addToMiddle() {
        final int pos = 7;
        final List<Index> initial = LongStream.range(0, 16)
                .mapToObj(i -> (Index)new LongIndex(i))
                .toList();

        final List<Index> toAdd = LongStream.range(100, 106)
                .mapToObj(i -> (Index)new LongIndex(i))
                .toList();

        final Indexes indexes = new ArrayIndexes()
                .add(0, initial.toArray(Index[]::new))
                .add(pos, toAdd.toArray(Index[]::new));

        final List<Index> expected = new ArrayList<>(initial);
        expected.addAll(pos, toAdd);
        MatcherAssert.assertThat(
                "Indices should be correctly inserted in the middle",
                expected,
                CoreMatchers.equalTo(
                        StreamSupport.stream(indexes.spliterator(), false).toList()
                )
        );
    }
}
