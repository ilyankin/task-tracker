package managers;

import utils.Managers;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    private InMemoryTaskManagerTest() {
        super(Managers.getInMemory());
    }
}