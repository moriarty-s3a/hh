package org.s3a.hh.impl;

import org.s3a.hh.Secret;

public class AdditiveSecret implements Secret {
    @Override
    public Long doSecret(Long input) {
        return input;
    }
}
