package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.events.MultipleInlineEvent;

/**
 * An event listener that listens to {@link Event} which are custom made for specific functionalities.
 * See for example {@link MultipleInlineEvent}.
 *
 * @param <T> The subclass of {@link Event} to listen to.
 */
public interface CustomEventListener<T extends Event> extends EventListener<T, Event> { }
