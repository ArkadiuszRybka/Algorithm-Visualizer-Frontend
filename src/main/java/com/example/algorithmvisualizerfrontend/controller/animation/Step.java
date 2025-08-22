package com.example.algorithmvisualizerfrontend.controller.animation;

import java.util.List;

public class Step {
    public enum Type { HIGHLIGHT, COMPARE, SWAP, MARK_SORTED, CLEAR, DONE }
    public enum Role { DEFAULT, ACTIVE, SWAP, SORTED }

    public final Type type;
    public final int i;
    public final int j;
    public final List<Integer> indices;
    public final Role role;
    public final int durationMs;

    private Step(Type type, int i, int j, List<Integer> indices, Role role, int durationMs) {
        this.type = type;
        this.i = i;
        this.j = j;
        this.indices = indices;
        this.role = role;
        this.durationMs = durationMs;
    }

    public static Step highlight(List<Integer> indices, Role role, int ms) {
        return new Step(Type.HIGHLIGHT, -1, -1, indices, role, ms);
    }

    public static Step compare(int i, int j, int ms) {
        return new Step(Type.COMPARE, i, j, null, Role.ACTIVE, ms);
    }

    public static Step swap(int i, int j, int ms) {
        return new Step(Type.SWAP, i, j, null, Role.SWAP, ms);
    }

    public static Step markSorted(List<Integer> indices, int ms) {
        return new Step(Type.MARK_SORTED, -1, -1, indices, Role.SORTED, ms);
    }

    public static Step clear(int ms) {
        return new Step(Type.CLEAR, -1, -1, null, Role.DEFAULT, ms);
    }

    public static Step done(int ms) {
        return new Step(Type.DONE, -1, -1, null, Role.DEFAULT, ms);
    }
}
