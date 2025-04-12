package me.sunmisc.btree.heap;

import java.io.IOException;
import java.io.InputStream;

public interface Data {

    InputStream delta() throws IOException;
}
