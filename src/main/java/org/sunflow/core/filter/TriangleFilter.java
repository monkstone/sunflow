package org.sunflow.core.filter;

import org.sunflow.core.Filter;

public class TriangleFilter implements Filter {

    @Override
    public float getSize() {
        return 2.0f;
    }

    @Override
    public float get(float x, float y) {
        return (1.0f - Math.abs(x)) * (1.0f - Math.abs(y));
    }
}