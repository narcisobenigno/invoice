package invoice.common.es;

import invoice.common.clock.Clock;

public class EventStreamInMemoryTest implements EventStreamContractTest {
    @Override
    public EventStream createEventStream(Clock streamClock, EventsRegistry eventsRegistry) {
        return new EventStream.InMemory(streamClock);
    }
}
