package org.s3a.hh.impl;

import org.s3a.hh.Secret;

public class NotAdditiveSecret implements Secret {
    @Override
    public Long doSecret(Long input) {
        Double random = Math.random() * 100;
        return random.longValue();
    }
}
