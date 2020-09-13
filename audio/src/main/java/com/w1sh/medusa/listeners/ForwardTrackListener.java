package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.events.ForwardTrackEvent;
import com.w1sh.medusa.services.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static com.w1sh.medusa.utils.Reactive.isEmpty;

@Component
@RequiredArgsConstructor
@Slf4j
public final class ForwardTrackListener implements CustomEventListener<ForwardTrackEvent> {

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
    private final AudioConnectionManager audioConnectionManager;
    private final MessageService messageService;

    @PostConstruct
    public void init(){
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public Mono<Void> execute(ForwardTrackEvent event) {
        return Mono.justOrEmpty(event.getArguments().get(0))
                .handle(this::parseTime)
                .zipWith(audioConnectionManager.getAudioConnection(event),
                        (time, ac) -> Mono.fromRunnable(() -> ac.getTrackScheduler().forward(time)))
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to forward track to requested time <{}>", event.getArguments().get(0), t)))
                .transform(isEmpty())
                .flatMap(ignored -> messageService.send(event.getChannel(), MessageEnum.MOVETIME_ERROR))
                .then();
    }

    private void parseTime(String time, SynchronousSink<Long> sink) {
        try {
            long milliseconds = simpleDateFormat.parse(time).getTime();
            sink.next(milliseconds);
        } catch (ParseException e) {
            sink.error(e);
        }
    }
}
