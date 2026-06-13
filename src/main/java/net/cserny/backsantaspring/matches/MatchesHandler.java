package net.cserny.backsantaspring.matches;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class MatchesHandler {

    @Async("eventVirtualThreadExecutor")
    @EventListener
    public void handle(MatchesApi.Events.InitEvent initEvent) {
        log.info("Handling matches event {}", initEvent);
    }

    @Async("eventVirtualThreadExecutor")
    @TransactionalEventListener(classes = MatchesApi.Events.InitEvent.class, condition = "#initEvent.flag == true")
    public void handleFlagOn(MatchesApi.Events.InitEvent initEvent) {
        log.info("Handling postCommit transaction for flagged event {}", initEvent);
    }

    @Async("eventVirtualThreadExecutor")
    @TransactionalEventListener(classes = MatchesApi.Events.InitEvent.class, condition = "#initEvent.flag == false")
    public void handleFlagOff(MatchesApi.Events.InitEvent initEvent) {
        log.info("Handling postCommit transaction for unflagged event {}", initEvent);
    }
}
