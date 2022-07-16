package com.mastercontrol.jmh;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EntityWithResult<E> {
    private E entity;
    private ProcessingResult result;
}
