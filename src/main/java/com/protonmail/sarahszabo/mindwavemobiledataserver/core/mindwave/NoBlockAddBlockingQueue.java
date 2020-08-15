/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.protonmail.sarahszabo.mindwavemobiledataserver.core.mindwave;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * A blocking queue that only blocks if you're taking something. When adding
 * something, it removes the head (longest in the queue) element.
 *
 * @author Sarah Szabo <SarahSzabo@Protonmail.com>
 * @param <T> The type
 */
public class NoBlockAddBlockingQueue<T> extends ArrayBlockingQueue<T> {

    public NoBlockAddBlockingQueue(int capacity) {
        super(capacity);
    }

    @Override
    public boolean add(T e) {
        if (super.remainingCapacity() == 0) {
            super.remove(super.peek());

        }
        return super.add(e);
    }

}
