/**
 * Copyright (c) 2017 Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package io.pravega.shared.metrics;

import java.util.concurrent.atomic.AtomicReference;

public class GaugeProxy implements Gauge {
    private final AtomicReference<Gauge> instance = new AtomicReference<>();

    GaugeProxy(Gauge gauge) {
        instance.set(gauge);
    }

    void setGauge(Gauge gauge) {
        instance.set(gauge);
    }

    @Override
    public String getName() {
        return instance.get().getName();
    }
}
