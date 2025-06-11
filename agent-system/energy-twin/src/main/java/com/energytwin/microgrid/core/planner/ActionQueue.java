package com.energytwin.microgrid.core.planner;

import java.util.LinkedList;

public final class ActionQueue {
    private final LinkedList<Action> q = new LinkedList<>();
    public void clear()                { q.clear(); }
    public void addAll(java.util.Collection<Action> c){ q.addAll(c); }
    public Action pop()                { return q.pollFirst(); }
    public boolean isEmpty()           { return q.isEmpty(); }
}
