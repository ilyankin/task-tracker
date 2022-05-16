package managers.history;

import models.tasks.AbstractTask;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private final HashMap<Long, LinkedTaskList.NodeTask> nodeMap = new HashMap<>();
    private LinkedTaskList history = new LinkedTaskList();

    @Override
    public void add(AbstractTask task) {
        long id = task.getId();
        remove(id);
        nodeMap.put(id, history.linkLast(task));
    }

    @Override
    public void remove(long id) {
        history.unlinkNode(nodeMap.remove(id));
    }

    @Override
    public List<AbstractTask> getHistory() {
        return history.getTasks();
    }

    @Override
    public void clear() {
        nodeMap.clear();
        history = new LinkedTaskList();
    }

    private static class LinkedTaskList implements Iterable<AbstractTask> {
        NodeTask first;
        NodeTask last;
        int size = 0;

        private static class NodeTask {
            AbstractTask task;
            NodeTask next;
            NodeTask prev;

            public NodeTask(NodeTask prev, AbstractTask task, NodeTask next) {
                this.prev = prev;
                this.task = task;
                this.next = next;
            }
        }

        private NodeTask linkLast(AbstractTask task) {
            final NodeTask l = last;
            final NodeTask newNode = new NodeTask(l, task, null);
            last = newNode;
            if (l == null) {
                first = newNode;
            } else {
                l.next = newNode;
            }
            size++;
            return newNode;
        }

        private AbstractTask unlinkNode(NodeTask node) {
            if (node != null) {
                final AbstractTask nodeTask = node.task;
                final NodeTask next = node.next;
                final NodeTask prev = node.prev;

                if (prev == null) {
                    first = next;
                } else {
                    prev.next = next;
                    node.prev = null;
                }

                if (next == null) {
                    last = prev;
                } else {
                    next.prev = prev;
                    node.next = null;
                }

                node.task = null;
                size--;
                return nodeTask;
            }
            return null;
        }

        private ArrayList<AbstractTask> getTasks() {
            ArrayList<AbstractTask> abstractTasks = new ArrayList<>();
            for (AbstractTask task : this) {
                abstractTasks.add(task);
            }
            return abstractTasks;
        }

        @Override
        public Iterator<AbstractTask> iterator() {
            return new Iterator<>() {
                private NodeTask current = first;

                @Override
                public boolean hasNext() {
                    return current != null;
                }

                @Override
                public AbstractTask next() {
                    if (!hasNext())
                        throw new NoSuchElementException();

                    AbstractTask task = current.task;
                    current = current.next;
                    return task;
                }
            };
        }
    }
}
